package me.robomwm.MountainDewritoes.exceptionlogger;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.*;

/**
 * ExceptionLogger - Logs exceptions from any plugin to files in their respective data folders.
 * Only logs one instance of each unique exception per plugin.
 */
public class ExceptionLogger
{
    private final MountainDewritoes plugin;
    private final Map<String, Set<String>> loggedExceptions;
    private final Map<String, List<String>> pluginContextBuffer;
    
    // Track recent commands/operations as context
    private static final int MAX_CONTEXT_ENTRIES = 5;
    
    public ExceptionLogger(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        this.loggedExceptions = new ConcurrentHashMap<>();
        this.pluginContextBuffer = new ConcurrentHashMap<>();
        
        setupExceptionHandler();
    }
    
    /**
     * Set up the global exception handler to catch exceptions from all plugins
     */
    private void setupExceptionHandler()
    {
        // Get the current default uncaught exception handler
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        
        // Set our custom handler that wraps the default
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handleException(thread, throwable);
            if (defaultHandler != null)
            {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
        
        // Also hook into Bukkit's logger to catch exceptions
        Logger bukkitLogger = Bukkit.getLogger();
        if (bukkitLogger != null)
        {
            // Wrap the logger's handlers
            for (Handler handler : bukkitLogger.getHandlers())
            {
                if (handler instanceof ConsoleHandler || handler instanceof FileHandler)
                {
                    // We'll monitor the logger by adding our own handler
                    break;
                }
            }
            
            // Add our custom handler to Bukkit's logger
            bukkitLogger.addHandler(new ExceptionLoggingHandler(this));
        }
        
        // Start a task to periodically clean up old context data
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                cleanupOldContextData();
            }
        }.runTaskTimer(plugin, 20 * 60 * 60L, 20 * 60 * 60L); // Clean up every hour
    }
    
    /**
     * Handle an exception that was caught
     */
    public void handleException(Thread thread, Throwable throwable)
    {
        try
        {
            // Find which plugin caused this exception
            String pluginName = findPluginFromThread(thread, throwable);
            
            if (pluginName == null || pluginName.isEmpty())
            {
                pluginName = "unknown";
            }
            
            // Create a unique key for this exception
            String exceptionKey = createExceptionKey(throwable, pluginName);
            
            // Check if we've already logged this exception for this plugin
            if (hasAlreadyLoggedException(pluginName, exceptionKey))
            {
                return; // Skip duplicate
            }
            
            // Mark as logged
            addLoggedException(pluginName, exceptionKey);
            
            // Get context information
            List<String> context = getContextForPlugin(pluginName);
            
            // Log the exception to the plugin's data folder
            logExceptionToFile(pluginName, throwable, context, thread);
            
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
        if (plugin == null || throwable == null)
            return;
            
        String pluginName = plugin.getName();
        if (pluginName == null || pluginName.isEmpty())
            return;
            
        String exceptionKey = createExceptionKey(throwable, pluginName);
        
        if (hasAlreadyLoggedException(pluginName, exceptionKey))
            return;
            
        addLoggedException(pluginName, exceptionKey);
        List<String> context = getContextForPlugin(pluginName);
        
        Thread thread = Thread.currentThread();
        logExceptionToFile(pluginName, throwable, context, thread);
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
                continue; // Skip Bukkit/Spigot/NMS classes
                
            // Check if this class belongs to a loaded plugin
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            {
                String pluginClassPath = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                if (pluginClassPath != null && className.startsWith(plugin.getName()))
                {
                    return plugin.getName();
                }
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
     * Check if we've already logged this exception for this plugin
     */
    private boolean hasAlreadyLoggedException(String pluginName, String exceptionKey)
    {
        Set<String> exceptions = loggedExceptions.get(pluginName);
        if (exceptions == null)
            return false;
        return exceptions.contains(exceptionKey);
    }
    
    /**
     * Mark an exception as logged for a plugin
     */
    private void addLoggedException(String pluginName, String exceptionKey)
    {
        loggedExceptions.computeIfAbsent(pluginName, k -> ConcurrentHashMap.newKeySet()).add(exceptionKey);
    }
    
    /**
     * Get context information for a plugin (recent commands, operations, etc.)
     */
    private List<String> getContextForPlugin(String pluginName)
    {
        return pluginContextBuffer.getOrDefault(pluginName, new CopyOnWriteArrayList<>());
    }
    
    /**
     * Add context information for a plugin
     */
    public void addContext(String pluginName, String context)
    {
        if (pluginName == null || context == null)
            return;
            
        pluginContextBuffer.computeIfAbsent(pluginName, k -> new CopyOnWriteArrayList<>());
        List<String> contextList = pluginContextBuffer.get(pluginName);
        
        // Add timestamp
        String timestampedContext = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + context;
        contextList.add(timestampedContext);
        
        // Keep only the most recent entries
        while (contextList.size() > MAX_CONTEXT_ENTRIES)
        {
            contextList.remove(0);
        }
    }
    
    /**
     * Log exception to a file in the plugin's data folder
     */
    private void logExceptionToFile(String pluginName, Throwable throwable, List<String> context, Thread thread)
    {
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        File pluginDataFolder = null;
        
        if (targetPlugin != null)
        {
            pluginDataFolder = targetPlugin.getDataFolder();
        }
        else if (pluginName.equals(plugin.getName()))
        {
            pluginDataFolder = plugin.getDataFolder();
        }
        
        // Fallback: use MountainDewritoes data folder with subdirectory for the plugin
        if (pluginDataFolder == null)
        {
            pluginDataFolder = new File(plugin.getDataFolder(), "plugin_exceptions" + File.separator + pluginName);
        }
        
        // Create the directory if it doesn't exist
        if (!pluginDataFolder.exists())
        {
            pluginDataFolder.mkdirs();
        }
        
        // Create a log file for this plugin
        File logFile = new File(pluginDataFolder, "exceptions.log");
        
        try
        {
            // Create the file if it doesn't exist
            if (!logFile.exists())
            {
                logFile.createNewFile();
            }
            
            // Write the exception details
            FileWriter writer = new FileWriter(logFile, true); // Append mode
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            
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
            
            // Write context if available
            if (context != null && !context.isEmpty())
            {
                bufferedWriter.write("CONTEXT (Recent operations before exception):");
                bufferedWriter.write("\n");
                for (String contextEntry : context)
                {
                    bufferedWriter.write("  - " + contextEntry);
                    bufferedWriter.write("\n");
                }
                bufferedWriter.write("\n");
            }
            
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
            
            bufferedWriter.close();
            
            plugin.getLogger().info("Logged exception from plugin " + pluginName + " to: " + logFile.getAbsolutePath());
            
        } catch (IOException e)
        {
            plugin.getLogger().warning("Failed to write exception log for plugin " + pluginName + ": " + e.getMessage());
        }
    }
    
    /**
     * Clean up old context data to prevent memory leaks
     */
    private void cleanupOldContextData()
    {
        // Clear context buffers for plugins that are no longer loaded
        Set<String> loadedPluginNames = new HashSet<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
        {
            loadedPluginNames.add(plugin.getName());
        }
        
        pluginContextBuffer.keySet().removeIf(pluginName -> !loadedPluginNames.contains(pluginName));
    }
    
    /**
     * Add context for a command execution
     */
    public void logCommandContext(String pluginName, Player player, String command)
    {
        String context = "COMMAND by " + (player != null ? player.getName() : "console") + ": " + command;
        addContext(pluginName, context);
    }
    
    /**
     * Add context for a general operation
     */
    public void logOperationContext(String pluginName, String operation)
    {
        addContext(pluginName, "OPERATION: " + operation);
    }
    
    /**
     * Clear context for a plugin
     */
    public void clearContext(String pluginName)
    {
        pluginContextBuffer.remove(pluginName);
    }
    
    /**
     * Custom Handler for catching exceptions from Bukkit's logger
     */
    private static class ExceptionLoggingHandler extends Handler
    {
        private final ExceptionLogger exceptionLogger;
        
        public ExceptionLoggingHandler(ExceptionLogger exceptionLogger)
        {
            this.exceptionLogger = exceptionLogger;
        }
        
        @Override
        public void publish(LogRecord record)
        {
            if (record.getThrown() != null)
            {
                // Find the plugin from the logger name
                String loggerName = record.getLoggerName();
                String pluginName = extractPluginName(loggerName);
                
                if (pluginName != null)
                {
                    exceptionLogger.handleException(Thread.currentThread(), record.getThrown());
                }
            }
        }
        
        private String extractPluginName(String loggerName)
        {
            // Common patterns: org.bukkit.plugin.PluginName, me.author.PluginName, etc.
            if (loggerName.startsWith("org.bukkit") || loggerName.startsWith("net.minecraft"))
                return null;
                
            // Try to find a loaded plugin that matches
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            {
                if (loggerName.startsWith(plugin.getName()) || loggerName.contains("." + plugin.getName()))
                {
                    return plugin.getName();
                }
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
