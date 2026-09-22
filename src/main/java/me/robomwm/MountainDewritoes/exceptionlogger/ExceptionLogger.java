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
 * Writes exceptions from any plugin to files in MountainDewritoes' data folder.
 * Each unique exception is only logged once per plugin, no matter how often it happens.
 * Writing happens on its own background thread, so it never slows down the server,
 * and two writes can't get tangled up when exceptions happen at the same time.
 * Remembers the last few commands that were run (by players and by the server).
 * Those commands are only saved with an exception if the same exception keeps
 * happening after the same commands; a one-time coincidence is ignored.
 */
public class ExceptionLogger implements Listener
{
    private final MountainDewritoes plugin;
    private final Map<String, Set<String>> loggedExceptions;

    // The last few commands run on the server. Commands older than
    // CONTEXT_WINDOW_MILLIS don't count; they're too old to have caused anything.
    private static final int MAX_CONTEXT_ENTRIES = 5;
    private static final long CONTEXT_WINDOW_MILLIS = 5 * 60 * 1000;
    private final Deque<ContextEntry> recentCommands = new ConcurrentLinkedDeque<>();

    // Remembers what the recent commands were the first time each exception
    // happened, so we can check if they're the same when it happens again.
    // The lock keeps two threads from checking and updating this at once.
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

    // One background thread does all the file writing, so it never runs on the
    // server thread and two writes can't get tangled together.
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
     * Remember a player's command in case an exception happens soon after.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        addContext("COMMAND by " + event.getPlayer().getName() + ": " + event.getMessage());
    }

    /**
     * Remember a command from the console, a command block, or another plugin
     * in case an exception happens soon after.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event)
    {
        addContext("COMMAND by " + event.getSender().getName() + ": " + event.getCommand());
    }

    /**
     * Save a short note (e.g. "saving player data") with the recent commands.
     * Only the last few are kept. For plugins to note what they were doing,
     * in case an exception follows.
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

            // Build an ID for this exception so we can tell if we've seen it before
            String exceptionKey = createExceptionKey(throwable, pluginName);
            List<ContextEntry> contextSnapshot = captureContextSnapshot();

            boolean firstSeen;
            synchronized (contextLock)
            {
                // Have we logged this exact exception before? (Safe to ask
                // from several threads at once.)
                firstSeen = addLoggedException(pluginName, exceptionKey);
                if (firstSeen)
                {
                    // First time seeing it: remember what commands were just run,
                    // but don't save them yet. They only get saved if this
                    // exception happens again after the same commands.
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

            // Hand the file writing to the background thread
            final String loggedPluginName = pluginName;
            logExecutor.submit(() -> writeSafely(loggedPluginName, throwable, thread));

        } catch (Exception e)
        {
            // Don't throw another exception while handling one
            plugin.getLogger().warning("ExceptionLogger failed to log exception: " + e.getMessage());
        }
    }

    /**
     * Log an exception we caught ourselves, rather than through the handler.
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
            // Don't throw another exception while handling one
            this.plugin.getLogger().warning("ExceptionLogger failed to log exception: " + e.getMessage());
        }
    }

    /**
     * Write the log file, but if writing fails, don't feed that failure back
     * into the exception handler or it would loop forever.
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
     * Figure out which plugin caused the exception from the thread name and stack trace
     */
    private String findPluginFromThread(Thread thread, Throwable throwable)
    {
        // Try the thread name first
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

        // Try the stack trace next
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement element : stackTrace)
        {
            String className = element.getClassName();

            // Skip Bukkit's and Minecraft's own classes
            if (className.startsWith("org.bukkit") || className.startsWith("net.minecraft"))
                continue;

            // See if this class belongs to one of the loaded plugins
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

        // Or maybe the exception came from us
        for (StackTraceElement element : throwable.getStackTrace())
        {
            if (element.getClassName().startsWith("me.robomwm.MountainDewritoes"))
                return plugin.getName();
        }

        return null;
    }

    /**
     * Clean up a plugin name so it's safe to use as a file name
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
     * Build an ID for an exception so we can spot repeats
     */
    private String createExceptionKey(Throwable throwable, String pluginName)
    {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(pluginName).append(":");

        // What kind of exception it was
        keyBuilder.append(throwable.getClass().getName());

        // What it said (first 100 characters, so the ID doesn't get huge)
        String message = throwable.getMessage();
        if (message != null)
        {
            keyBuilder.append(":").append(message.length() > 100 ? message.substring(0, 100) : message);
        }

        // Where it happened (top of the stack trace)
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (int i = 0; i < Math.min(3, stackTrace.length); i++)
        {
            keyBuilder.append(":").append(stackTrace[i].getClassName()).append("#").append(stackTrace[i].getMethodName());
        }

        return keyBuilder.toString();
    }

    /**
     * Remember that we logged this exception.
     * @return true if this is the first time we've seen it
     */
    private boolean addLoggedException(String pluginName, String exceptionKey)
    {
        return loggedExceptions.computeIfAbsent(pluginName, k -> ConcurrentHashMap.newKeySet()).add(exceptionKey);
    }

    /**
     * Copy the recent commands, leaving out the old ones. Times are kept so
     * they can be shown later; when comparing, only the command text matters.
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
     * Runs when an exception we've already logged happens again (while holding
     * contextLock). Saves the recent commands only if they're the same as last
     * time; if they changed, it was probably a coincidence, so we save nothing.
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
     * Add the confirmed commands to the plugin's log file, right after the
     * original exception entry. If writing fails, report it normally; don't
     * send it back through the exception handler or it would loop forever.
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
     * Write an exception to the plugin's log file. Always runs on the
     * background writer thread, never the server thread.
     */
    private void logExceptionToFile(String pluginName, Throwable throwable, Thread thread)
    {
        // Everything goes in MountainDewritoes' data folder
        File pluginDataFolder = plugin.getDataFolder();

        // Create the directory if it doesn't exist
        if (!pluginDataFolder.exists())
        {
            pluginDataFolder.mkdirs();
        }

        // One log file per plugin
        File logFile = new File(pluginDataFolder, sanitizeFileName(pluginName) + "_exceptions.log");

        try
        {
            // Create the file if it doesn't exist
            if (!logFile.exists())
            {
                logFile.createNewFile();
            }

            // Add to the end of the file
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
     * Catches exceptions that plugins log through Bukkit's logger
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
            // Or maybe it's from us
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
