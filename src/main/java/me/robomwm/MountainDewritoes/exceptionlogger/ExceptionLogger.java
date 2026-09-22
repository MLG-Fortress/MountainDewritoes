package me.robomwm.MountainDewritoes.exceptionlogger;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

/**
 * ExceptionLogger - Logs exceptions from any plugin to files in MountainDewritoes' data folder.
 * Only logs one instance of each unique exception per plugin (deduplicated, never expires).
 * File writes run on a dedicated single-threaded executor: never on the server thread,
 * and serialized so concurrent exceptions cannot interleave in the log files.
 * Keeps a rolling buffer of recently executed commands (players and console).
 * Context is only recorded for an exception that recurs with the same recent
 * commands; one-off coincidences are treated as noise and never recorded.
 */
public class ExceptionLogger implements Listener
{
    private final MountainDewritoes plugin;
    private final Map<String, Set<String>> loggedExceptions;

    // Rolling buffer of recently executed commands on the server. Context is
    // only ever recorded for an exception that recurs with the same recent
    // commands; anything else is treated as coincidence and omitted.
    // Entries older than CONTEXT_WINDOW_MILLIS are stale and never used.
    private static final int MAX_CONTEXT_ENTRIES = 5;
    private static final long CONTEXT_WINDOW_MILLIS = 5 * 60 * 1000;
    private final Deque<ContextEntry> recentCommands = new ConcurrentLinkedDeque<>();

    // First-seen context per exception key, used to confirm that the context
    // is stable across occurrences before recording it. The key already
    // includes the plugin name. Compound check-and-update is guarded by
    // contextLock.
    private final Map<String, ContextState> contextStates = new ConcurrentHashMap<>();
    private final Object contextLock = new Object();

    private static class ContextEntry
    {
        final long timestamp;
        final String text;

        ContextEntry(long timestamp, String text)
        {
            this.timestamp = timestamp;
            this.text = text;
        }
    }

    // Single-threaded writer: keeps file I/O off the server thread and
    // serializes appends so concurrent exceptions cannot interleave.
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor(r ->
    {
        Thread thread = new Thread(r, "ExceptionLogger-Writer");
        thread.setDaemon(true);
        return thread;
    });

    public ExceptionLogger(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        this.loggedExceptions = new ConcurrentHashMap<>();

        setupExceptionHandler();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Flush pending writes and stop the writer thread. Call from onDisable.
     */
    public void shutdown()
    {
        logExecutor.shutdown();
        try
        {
            if (!logExecutor.awaitTermination(5, TimeUnit.SECONDS))
                logExecutor.shutdownNow();
        }
        catch (InterruptedException e)
        {
            logExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class ContextState
    {
        final List<ContextEntry> firstContext;
        boolean confirmed;
        boolean invalidated;

        ContextState(List<ContextEntry> firstContext)
        {
            this.firstContext = firstContext;
        }
    }

    /**
     * Record a player command as context for future exceptions.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        addContext("COMMAND by " + event.getPlayer().getName() + ": " + event.getMessage());
    }

    /**
     * Record a non-player command (console, command block, or plugin-dispatched)
     * as context for future exceptions.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event)
    {
        addContext("COMMAND by " + event.getSender().getName() + ": " + event.getCommand());
    }

    /**
     * Add a free-text breadcrumb (e.g. "saving player data") to the context
     * buffer. Only the last few entries are kept; they are printed with
     * exception logs to show what led up to the error.
     */
    public void addContext(String context)
    {
        if (context == null || context.isEmpty())
            return;
        long now = System.currentTimeMillis();
        recentCommands.addLast(new ContextEntry(now, context));
        while (recentCommands.size() > MAX_CONTEXT_ENTRIES)
            recentCommands.pollFirst();
        while (!recentCommands.isEmpty() && now - recentCommands.peekFirst().timestamp > CONTEXT_WINDOW_MILLIS)
            recentCommands.pollFirst();
    }

    /**
     * Set up the global exception handler to catch exceptions from all plugins
     */
    private void setupExceptionHandler()
    {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handleException(thread, throwable);
            if (defaultHandler != null)
            {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });

        // Hook into all loaded plugin loggers
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
        {
            Logger pluginLogger = plugin.getLogger();
            if (pluginLogger != null)
            {
                pluginLogger.addHandler(new ExceptionLoggingHandler(plugin.getName(), this));
            }
        }

        // Also hook into Bukkit's logger
        Logger bukkitLogger = Bukkit.getLogger();
        if (bukkitLogger != null)
        {
            bukkitLogger.addHandler(new ExceptionLoggingHandler(null, this));
        }
    }

    /**
     * Handle an exception that was caught
     */
    public void handleException(Thread thread, Throwable throwable)
    {
        handleException(thread, throwable, null);
    }

    public void handleException(Thread thread, Throwable throwable, String pluginName)
    {
        try
        {
            if (pluginName == null || pluginName.isEmpty())
            {
                pluginName = findPluginFromThread(thread, throwable);
            }

            if (pluginName == null || pluginName.isEmpty())
            {
                pluginName = "unknown";
            }

            // Create a unique key for this exception
            String exceptionKey = createExceptionKey(throwable, pluginName);
            List<ContextEntry> contextSnapshot = captureContextSnapshot();

            boolean firstSeen;
            synchronized (contextLock)
            {
                // Skip duplicates; the add is atomic so concurrent identical
                // exceptions cannot both slip through
                firstSeen = addLoggedException(pluginName, exceptionKey);
                if (firstSeen)
                {
                    // First occurrence: remember the context, but don't record
                    // it yet. It is only kept if the exception recurs with the
                    // same context.
                    contextStates.put(exceptionKey, new ContextState(contextSnapshot));
                }
                else
                {
                    maybeConfirmContext(pluginName, exceptionKey, throwable, contextSnapshot);
                }
            }

            if (!firstSeen)
            {
                return;
            }

            // Write off-thread; the single-threaded executor serializes appends
            final String loggedPluginName = pluginName;
            logExecutor.submit(() -> writeSafely(loggedPluginName, throwable, thread));

        } catch (Exception e)
        {
            // Don't let our exception handler cause more exceptions
            plugin.getLogger().warning("ExceptionLogger failed to log exception: " + e.getMessage());
        }
    }

    /**
     * Handle an exception directly (for cases where we catch exceptions explicitly)
     */
    public void handleException(Plugin plugin, Throwable throwable)
    {
        try
        {
            if (plugin == null || throwable == null)
                return;

            String pluginName = plugin.getName();
            if (pluginName == null || pluginName.isEmpty())
                return;

            String exceptionKey = createExceptionKey(throwable, pluginName);
            List<ContextEntry> contextSnapshot = captureContextSnapshot();

            boolean firstSeen;
            synchronized (contextLock)
            {
                firstSeen = addLoggedException(pluginName, exceptionKey);
                if (firstSeen)
                {
                    contextStates.put(exceptionKey, new ContextState(contextSnapshot));
                }
                else
                {
                    maybeConfirmContext(pluginName, exceptionKey, throwable, contextSnapshot);
                }
            }

            if (!firstSeen)
            {
                return;
            }

            Thread thread = Thread.currentThread();
            logExecutor.submit(() -> writeSafely(pluginName, throwable, thread));
        }
        catch (Exception e)
        {
            // Don't let our exception handler cause more exceptions
            this.plugin.getLogger().warning("ExceptionLogger failed to log exception: " + e.getMessage());
        }
    }

    /**
     * Invoke logExceptionToFile without ever routing writer failures back
     * through the exception handler (that would recurse).
     */
    private void writeSafely(String pluginName, Throwable throwable, Thread thread)
    {
        try
        {
            logExceptionToFile(pluginName, throwable, thread);
        }
        catch (Throwable t)
        {
            plugin.getLogger().warning("ExceptionLogger failed to write exception log: " + t.getMessage());
        }
    }

    /**
     * Find which plugin caused the exception based on thread and stack trace
     */
    private String findPluginFromThread(Thread thread, Throwable throwable)
    {
        // First, check if we can identify the plugin from the thread name
        String threadName = thread.getName();

        // Common patterns: "PluginName-1", "Craft Scheduler for PluginName", etc.
        if (threadName.contains("-") && !threadName.startsWith("main") && !threadName.startsWith("Server thread"))
        {
            String[] parts = threadName.split("-");
            if (parts.length > 0)
            {
                String potentialPlugin = parts[0].trim();
                Plugin plugin = Bukkit.getPluginManager().getPlugin(potentialPlugin);
                if (plugin != null)
                    return plugin.getName();
            }
        }

        if (threadName.contains(" for "))
        {
            int forIndex = threadName.indexOf(" for ");
            if (forIndex > 0)
            {
                String potentialPlugin = threadName.substring(forIndex + 5).trim();
                // Remove trailing ] if present
                int endIndex = potentialPlugin.indexOf(']');
                if (endIndex > 0)
                    potentialPlugin = potentialPlugin.substring(0, endIndex);

                Plugin plugin = Bukkit.getPluginManager().getPlugin(potentialPlugin);
                if (plugin != null)
                    return plugin.getName();
            }
        }

        // Check the stack trace for plugin classes
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement element : stackTrace)
        {
            String className = element.getClassName();

            // Look for plugin package patterns
            if (className.startsWith("org.bukkit") || className.startsWith("net.minecraft"))
                continue;

            // Check if this class belongs to a loaded plugin by package
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            {
                try {
                    String pluginPackage = plugin.getClass().getPackage().getName();
                    if (className.startsWith(pluginPackage) || className.startsWith(plugin.getName()))
                    {
                        return plugin.getName();
                    }
                } catch (Exception ignored) {}
            }
        }

        // If we're in MountainDewritoes itself, return our name
        for (StackTraceElement element : throwable.getStackTrace())
        {
            if (element.getClassName().startsWith("me.robomwm.MountainDewritoes"))
                return plugin.getName();
        }

        return null;
    }

    /**
     * Sanitize plugin name to be safe for use as a filename
     */
    private String sanitizeFileName(String pluginName)
    {
        if (pluginName == null || pluginName.isEmpty())
            return "unknown";

        StringBuilder sb = new StringBuilder();
        for (char c : pluginName.toCharArray())
        {
            if (c >= 'a' && c <= 'z')
                sb.append(c);
            else if (c >= 'A' && c <= 'Z')
                sb.append(c);
            else if (c >= '0' && c <= '9')
                sb.append(c);
            else if (c == '.' || c == '-' || c == '_')
                sb.append(c);
            else
                sb.append('_');
        }

        // Remove leading/trailing dots and underscores
        String result = sb.toString();
        result = result.replaceAll("^[._]+", "");
        result = result.replaceAll("[._]+$", "");
        result = result.replaceAll("__+", "_");

        return result.isEmpty() ? "unknown" : result;
    }

    /**
     * Create a unique key for an exception to identify duplicates
     */
    private String createExceptionKey(Throwable throwable, String pluginName)
    {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(pluginName).append(":");

        // Include the exception class name
        keyBuilder.append(throwable.getClass().getName());

        // Include the message (first 100 chars to avoid overly long keys)
        String message = throwable.getMessage();
        if (message != null)
        {
            keyBuilder.append(":").append(message.length() > 100 ? message.substring(0, 100) : message);
        }

        // Include the first few stack trace elements (most significant ones)
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (int i = 0; i < Math.min(3, stackTrace.length); i++)
        {
            keyBuilder.append(":").append(stackTrace[i].getClassName()).append("#").append(stackTrace[i].getMethodName());
        }

        return keyBuilder.toString();
    }

    /**
     * Mark an exception as logged for a plugin.
     * @return true if this is the first time seeing it, false if already logged
     */
    private boolean addLoggedException(String pluginName, String exceptionKey)
    {
        return loggedExceptions.computeIfAbsent(pluginName, k -> ConcurrentHashMap.newKeySet()).add(exceptionKey);
    }

    /**
     * Snapshot the current recent-command buffer, excluding stale entries.
     * Timestamps are kept for display; confirmation compares command texts.
     */
    private List<ContextEntry> captureContextSnapshot()
    {
        long cutoff = System.currentTimeMillis() - CONTEXT_WINDOW_MILLIS;
        List<ContextEntry> snapshot = new ArrayList<>();
        for (ContextEntry entry : recentCommands)
        {
            if (entry.timestamp >= cutoff)
                snapshot.add(entry);
        }
        return snapshot;
    }

    private static List<String> contextTexts(List<ContextEntry> entries)
    {
        List<String> texts = new ArrayList<>(entries.size());
        for (ContextEntry entry : entries)
            texts.add(entry.text);
        return texts;
    }

    /**
     * Called under contextLock when an already-logged exception recurs.
     * Records the context only if this occurrence has the same recent
     * commands as the first one; otherwise the context is deemed
     * coincidental and never recorded for this exception.
     */
    private void maybeConfirmContext(String pluginName, String exceptionKey, Throwable throwable, List<ContextEntry> snapshot)
    {
        ContextState state = contextStates.get(exceptionKey);
        if (state == null || state.confirmed || state.invalidated)
            return;

        if (!snapshot.isEmpty() && contextTexts(snapshot).equals(contextTexts(state.firstContext)))
        {
            state.confirmed = true;
            logExecutor.submit(() -> writeConfirmedContextSafely(pluginName, throwable, snapshot));
        }
        else
        {
            state.invalidated = true;
        }
    }

    /**
     * Append a confirmed-context block to the plugin's exception log.
     * Runs on the writer executor, serialized after the original entry.
     * Writer failures go to the plugin logger, never back through the
     * exception handler (that would recurse).
     */
    private void writeConfirmedContextSafely(String pluginName, Throwable throwable, List<ContextEntry> context)
    {
        try
        {
            File pluginDataFolder = plugin.getDataFolder();
            if (!pluginDataFolder.exists())
            {
                pluginDataFolder.mkdirs();
            }

            File logFile = new File(pluginDataFolder, sanitizeFileName(pluginName) + "_exceptions.log");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true)))
            {
                writer.write("\n");
                writer.write("================================================================================");
                writer.write("\n");
                writer.write("CONFIRMED CONTEXT LOGGED AT: " + new Date().toString());
                writer.write("\n");
                writer.write("FOR EXCEPTION: " + throwable.getClass().getName() + ": "
                        + (throwable.getMessage() != null ? throwable.getMessage() : "null"));
                writer.write("\n");
                writer.write("(Recurred with the same recent commands, so this context is likely related.)");
                writer.write("\n");
                writer.write("CONTEXT (Recent operations before exception):");
                writer.write("\n");
                for (ContextEntry entry : context)
                {
                    writer.write("  [" + new Date(entry.timestamp) + "] " + entry.text);
                    writer.write("\n");
                }
                writer.write("================================================================================");
                writer.write("\n");
            }
        }
        catch (Throwable t)
        {
            plugin.getLogger().warning("ExceptionLogger failed to write confirmed context: " + t.getMessage());
        }
    }

    /**
     * Log exception to a file in MountainDewritoes' data folder, one file per plugin.
     * Always runs on the single-threaded writer executor, never the server thread.
     */
    private void logExceptionToFile(String pluginName, Throwable throwable, Thread thread)
    {
        // All exception logs go directly to MountainDewritoes' data folder
        File pluginDataFolder = plugin.getDataFolder();

        // Create the directory if it doesn't exist
        if (!pluginDataFolder.exists())
        {
            pluginDataFolder.mkdirs();
        }

        // Create a log file for this plugin (one file per plugin)
        File logFile = new File(pluginDataFolder, sanitizeFileName(pluginName) + "_exceptions.log");

        try
        {
            // Create the file if it doesn't exist
            if (!logFile.exists())
            {
                logFile.createNewFile();
            }

            // Write the exception details (append mode)
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true)))
            {
                // Write separator
                bufferedWriter.write("\n");
                bufferedWriter.write("================================================================================");
                bufferedWriter.write("\n");
                bufferedWriter.write("EXCEPTION LOGGED AT: " + new Date().toString());
                bufferedWriter.write("\n");
                bufferedWriter.write("PLUGIN: " + pluginName);
                bufferedWriter.write("\n");
                bufferedWriter.write("THREAD: " + thread.getName());
                bufferedWriter.write("\n");
                bufferedWriter.write("================================================================================");
                bufferedWriter.write("\n");

                // Write the exception details
                bufferedWriter.write("EXCEPTION TYPE: " + throwable.getClass().getName());
                bufferedWriter.write("\n");
                bufferedWriter.write("MESSAGE: " + (throwable.getMessage() != null ? throwable.getMessage() : "null"));
                bufferedWriter.write("\n");
                bufferedWriter.write("\n");

                // Write stack trace
                bufferedWriter.write("STACK TRACE:");
                bufferedWriter.write("\n");

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                bufferedWriter.write(sw.toString());
                bufferedWriter.write("\n");

                // Write cause if available
                Throwable cause = throwable.getCause();
                if (cause != null)
                {
                    bufferedWriter.write("CAUSED BY:");
                    bufferedWriter.write("\n");
                    StringWriter causeSw = new StringWriter();
                    PrintWriter causePw = new PrintWriter(causeSw);
                    cause.printStackTrace(causePw);
                    bufferedWriter.write(causeSw.toString());
                    bufferedWriter.write("\n");
                }

                bufferedWriter.write("================================================================================");
                bufferedWriter.write("\n");
            }

            plugin.getLogger().info("Logged exception from plugin " + pluginName + " to: " + logFile.getPath());

        } catch (IOException e)
        {
            plugin.getLogger().warning("Failed to write exception log for plugin " + pluginName + ": " + e.getMessage());
        }
    }

    /**
     * Custom Handler for catching exceptions from Bukkit's logger
     */
    private static class ExceptionLoggingHandler extends Handler
    {
        private final String pluginName;
        private final ExceptionLogger exceptionLogger;

        public ExceptionLoggingHandler(String pluginName, ExceptionLogger exceptionLogger)
        {
            this.pluginName = pluginName;
            this.exceptionLogger = exceptionLogger;
        }

        @Override
        public void publish(LogRecord record)
        {
            if (record.getThrown() != null)
            {
                String name = pluginName;
                if (name == null)
                {
                    name = extractPluginByThrowable(record.getThrown());
                }
                if (name == null)
                {
                    name = "unknown";
                }
                exceptionLogger.handleException(Thread.currentThread(), record.getThrown(), name);
            }
        }

        private String extractPluginByThrowable(Throwable throwable)
        {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement element : stackTrace)
            {
                String className = element.getClassName();
                if (className.startsWith("org.bukkit") || className.startsWith("net.minecraft"))
                    continue;

                for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
                {
                    try {
                        String pluginPackage = plugin.getClass().getPackage().getName();
                        if (className.startsWith(pluginPackage) || className.startsWith(plugin.getName()))
                        {
                            return plugin.getName();
                        }
                    } catch (Exception ignored) {}
                }
            }
            // Check if it's from MountainDewritoes itself
            for (StackTraceElement element : stackTrace)
            {
                if (element.getClassName().startsWith("me.robomwm.MountainDewritoes"))
                    return exceptionLogger.plugin.getName();
            }
            return null;
        }

         @Override
         public void flush()
         {
             // No-op
         }

         @Override
         public void close() throws SecurityException
         {
             // No-op
         }
    }
}
