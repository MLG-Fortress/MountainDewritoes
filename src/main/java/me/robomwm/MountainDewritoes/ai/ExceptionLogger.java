package me.robomwm.MountainDewritoes.ai;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

/**
 * Writes exceptions from any plugin to files in MountainDewritoes' data folder.
 * Each unique exception is only logged once per plugin, no matter how often it happens.
 * Writing happens on its own background thread, so it never slows down the server,
 * and two writes can't get tangled up when exceptions happen at the same time.
 * Remembers the last command that was run (by a player or the server).
 * That command is only saved with an exception if the same exception keeps
 * happening after the same command; a one-time coincidence is ignored.
 */
public class ExceptionLogger implements Listener
{
    private final MountainDewritoes plugin;
    private final Map<String, Set<String>> loggedExceptions;

    // The last command run on the server. A command older than
    // CONTEXT_WINDOW_MILLIS doesn't count; it's too old to have caused anything.
    private static final long CONTEXT_WINDOW_MILLIS = 10 * 1000;
    private volatile ContextEntry lastCommand;

    // Remembers what the last command was the first time each exception
    // happened, so we can check if it's the same when it happens again.
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
        final ContextEntry firstCommand;
        final String entryId;
        boolean confirmed;
        boolean invalidated;

        ContextState(ContextEntry firstCommand, String entryId)
        {
            this.firstCommand = firstCommand;
            this.entryId = entryId;
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
     * Save a short note (e.g. "saving player data") as the last thing that happened.
     * For plugins to note what they were doing, in case an exception follows.
     */
    public void addContext(String context)
    {
        if (context == null || context.isEmpty())
            return;
        lastCommand = new ContextEntry(System.currentTimeMillis(), context);
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
            ContextEntry command = captureLastCommand();

            boolean firstSeen;
            String entryId = null;
            synchronized (contextLock)
            {
                // Have we logged this exact exception before? (Safe to ask
                // from several threads at once.)
                firstSeen = addLoggedException(pluginName, exceptionKey);
                if (firstSeen)
                {
                    // First time seeing it: remember what command was just run,
                    // but don't save it yet. It only gets saved if this
                    // exception happens again after the same command.
                    entryId = UUID.randomUUID().toString();
                    contextStates.put(exceptionKey, new ContextState(command, entryId));
                }
                else
                {
                    maybeConfirmContext(pluginName, exceptionKey, command);
                }
            }

            if (!firstSeen)
            {
                return;
            }

            // Hand the file writing to the background thread
            final String loggedPluginName = pluginName;
            final String loggedEntryId = entryId;
            logExecutor.submit(() -> writeSafely(loggedPluginName, throwable, thread, loggedEntryId));

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
            ContextEntry command = captureLastCommand();

            boolean firstSeen;
            String entryId = null;
            synchronized (contextLock)
            {
                firstSeen = addLoggedException(pluginName, exceptionKey);
                if (firstSeen)
                {
                    entryId = UUID.randomUUID().toString();
                    contextStates.put(exceptionKey, new ContextState(command, entryId));
                }
                else
                {
                    maybeConfirmContext(pluginName, exceptionKey, command);
                }
            }

            if (!firstSeen)
            {
                return;
            }

            Thread thread = Thread.currentThread();
            final String loggedEntryId = entryId;
            logExecutor.submit(() -> writeSafely(pluginName, throwable, thread, loggedEntryId));
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
    private void writeSafely(String pluginName, Throwable throwable, Thread thread, String entryId)
    {
        try
        {
            logExceptionToFile(pluginName, throwable, thread, entryId);
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
     * Get the last command, or null if there wasn't one recently.
     * The timestamp is kept so it can be shown later.
     */
    private ContextEntry captureLastCommand()
    {
        ContextEntry entry = lastCommand;
        if (entry == null || System.currentTimeMillis() - entry.timestamp > CONTEXT_WINDOW_MILLIS)
            return null;
        return entry;
    }

    /**
     * Runs when an exception we've already logged happens again (while holding
     * contextLock). Saves the command only if it's the same one as last time;
     * if it changed, it was probably a coincidence, so we save nothing.
     */
    private void maybeConfirmContext(String pluginName, String exceptionKey, ContextEntry command)
    {
        ContextState state = contextStates.get(exceptionKey);
        if (state == null || state.confirmed || state.invalidated)
            return;

        if (command != null && state.firstCommand != null
                && command.text.equals(state.firstCommand.text))
        {
            state.confirmed = true;
            final String confirmedEntryId = state.entryId;
            logExecutor.submit(() -> writeConfirmedContextSafely(pluginName, confirmedEntryId, command));
        }
        else
        {
            state.invalidated = true;
        }
    }

    /**
     * Merge the confirmed command into the exception's original log entry.
     * Runs on the writer thread after the entry was written, so the entry
     * is always there first. If writing fails, report it normally; don't
     * send it back through the exception handler or it would loop forever.
     */
    private void writeConfirmedContextSafely(String pluginName, String entryId, ContextEntry command)
    {
        try
        {
            File logFile = new File(plugin.getDataFolder(), sanitizeFileName(pluginName) + "_exceptions.log");
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                    lines.add(line);
            }

            int entryLine = -1;
            for (int i = 0; i < lines.size(); i++)
            {
                if (lines.get(i).equals("ENTRY ID: " + entryId))
                {
                    entryLine = i;
                    break;
                }
            }
            if (entryLine < 0)
            {
                plugin.getLogger().warning("ExceptionLogger could not find log entry " + entryId + " to add confirmed context");
                return;
            }

            // The entry ends at the second separator after the ENTRY ID line
            // (the first one closes the header).
            int separatorsSeen = 0;
            int insertAt = -1;
            for (int i = entryLine; i < lines.size(); i++)
            {
                if (lines.get(i).startsWith("=========="))
                {
                    separatorsSeen++;
                    if (separatorsSeen == 2)
                    {
                        insertAt = i;
                        break;
                    }
                }
            }
            if (insertAt < 0)
            {
                plugin.getLogger().warning("ExceptionLogger could not find end of log entry " + entryId);
                return;
            }

            lines.addAll(insertAt, Arrays.asList(
                    "CONFIRMED CONTEXT:",
                    "(This exception keeps happening after the same command, so it's likely related.)",
                    "COMMAND: [" + new Date(command.timestamp) + "] " + command.text,
                    ""));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, false)))
            {
                for (String line : lines)
                {
                    writer.write(line);
                    writer.newLine();
                }
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
    private void logExceptionToFile(String pluginName, Throwable throwable, Thread thread, String entryId)
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
                bufferedWriter.write("ENTRY ID: " + entryId);
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
