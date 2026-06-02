package me.robomwm.MountainDewritoes.adminai;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class AdminAiService
{
    private final MountainDewritoes plugin;
    private final AdminAiConfig config;
    private final OpenAiCompatibleClient client = new OpenAiCompatibleClient();
    private final Gson gson = new Gson();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicReference<Process> currentProcess = new AtomicReference<>();
    private final AtomicReference<CompletableFuture<Boolean>> approvalFuture = new AtomicReference<>();
    private CompletableFuture<?> currentTask;

    AdminAiService(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        this.config = new AdminAiConfig(plugin);
        scheduleMaintenanceCheck();
    }

    private void scheduleMaintenanceCheck()
    {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (config.isEnabled())
                run(plugin.getServer().getConsoleSender(), "Perform a routine maintenance check. Look for errors in the logs and ensure all plugins are up to date.", true);
        }, 1200L); // 1 minute after startup
    }

    String getStatus()
    {
        return (config.isEnabled() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled")
                + (config.isInteractive() ? ChatColor.YELLOW + " (interactive)" : ChatColor.GRAY + " (autonomous)")
                + ChatColor.GOLD + ", task=" + (isRunning() ? "running" : "idle")
                + ", providers=" + config.getProviders().size();
    }

    void setEnabled(boolean enabled)
    {
        config.setEnabled(enabled);
        if (!enabled)
            abortCurrentTask();
    }

    void setInteractive(boolean interactive)
    {
        config.setInteractive(interactive);
    }

    void approve(boolean approved)
    {
        CompletableFuture<Boolean> future = approvalFuture.getAndSet(null);
        if (future != null)
            future.complete(approved);
    }

    void reload()
    {
        config.reload();
    }

    void run(CommandSender sender, String prompt)
    {
        run(sender, prompt, false);
    }

    void run(CommandSender sender, String prompt, boolean proactive)
    {
        if (!config.isEnabled())
        {
            if (!proactive)
                sender.sendMessage(ChatColor.RED + "Admin AI is disabled. Use /adminai on first.");
            return;
        }
        if (isRunning())
        {
            if (!proactive)
                sender.sendMessage(ChatColor.RED + "Admin AI task already running.");
            return;
        }
        if (config.getProviders().isEmpty())
        {
            if (!proactive)
                sender.sendMessage(ChatColor.RED + "No enabled admin-ai providers in config.yml.");
            return;
        }

        currentTask = CompletableFuture.runAsync(() -> runAgent(sender, prompt, proactive), executor);
        if (!proactive)
            sender.sendMessage(ChatColor.GREEN + "Admin AI task started.");
    }

    void abortCurrentTask()
    {
        Process process = currentProcess.getAndSet(null);
        if (process != null)
            process.destroyForcibly();
        CompletableFuture<Boolean> approval = approvalFuture.getAndSet(null);
        if (approval != null)
            approval.complete(false);
        if (currentTask != null)
            currentTask.cancel(true);
    }

    void shutdown()
    {
        abortCurrentTask();
        executor.shutdownNow();
    }

    private boolean isRunning()
    {
        return currentTask != null && !currentTask.isDone();
    }

    private void runAgent(CommandSender sender, String userPrompt, boolean proactive)
    {
        List<AiMessage> messages = new ArrayList<>();
        messages.add(new AiMessage("system", systemPrompt()));
        messages.add(new AiMessage("user", buildInitialPrompt(userPrompt)));

        try
        {
            for (int i = 0; i < config.getInt("admin-ai|max-iterations"); i++)
            {
                ensureStillEnabled();
                String response = completeWithFallback(messages);
                messages.add(new AiMessage("assistant", response));
                AiAction action = parseAction(response);
                String result = executeAction(action, proactive);
                messages.add(new AiMessage("user", result));
                if ("finish".equalsIgnoreCase(action.action))
                {
                    send(sender, ChatColor.GREEN + "Admin AI done: " + nullToEmpty(action.message));
                    return;
                }
            }
            send(sender, ChatColor.YELLOW + "Admin AI stopped: iteration limit reached.");
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            send(sender, ChatColor.RED + "Admin AI aborted.");
        }
        catch (Exception e)
        {
            plugin.getLogger().warning("Admin AI failed: " + e.getMessage());
            send(sender, ChatColor.RED + "Admin AI failed: " + e.getMessage());
        }
        finally
        {
            currentProcess.set(null);
        }
    }

    private String completeWithFallback(List<AiMessage> messages) throws IOException, InterruptedException
    {
        IOException last = null;
        for (AiProvider provider : config.getProviders())
        {
            try
            {
                return client.complete(provider, messages);
            }
            catch (IOException e)
            {
                last = e;
                plugin.getLogger().warning("Admin AI provider " + provider.name() + " failed: " + e.getMessage());
            }
        }
        throw last == null ? new IOException("No enabled providers.") : last;
    }

    private String executeAction(AiAction action, boolean proactive) throws IOException, InterruptedException
    {
        ensureStillEnabled();
        String actionName = action.action == null ? "" : action.action.toLowerCase(Locale.ROOT);

        if ((config.isInteractive() || proactive) && isDestructive(actionName))
        {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            approvalFuture.set(future);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().broadcast(ChatColor.GOLD + "[Admin AI" + (proactive ? " PROACTIVE" : "") + "] Pending Action: " + actionName, "mlg.admin");
                if ("write_file".equals(actionName))
                    plugin.getServer().broadcast(ChatColor.GRAY + "Path: " + action.path, "mlg.admin");
                if ("run_command".equals(actionName))
                    plugin.getServer().broadcast(ChatColor.GRAY + "Command: " + action.command, "mlg.admin");
                plugin.getServer().broadcast(ChatColor.YELLOW + "Use /adminai approve or /adminai deny", "mlg.admin");
            });

            try
            {
                if (!future.get())
                    return "RESULT error\nAction denied by administrator.";
            }
            catch (Exception e)
            {
                return "RESULT error\nApproval process interrupted.";
            }
        }

        return switch (actionName)
        {
            case "read_log" -> "RESULT read_log\n" + readAllowedFile(action.path, true);
            case "read_file" -> "RESULT read_file\n" + readAllowedFile(action.path, false);
            case "write_file" -> "RESULT write_file\n" + writeAllowedFile(action.path, action.content);
            case "run_command" -> "RESULT run_command\n" + runCommand(action.command);
            case "finish" -> "RESULT finish accepted";
            default -> "RESULT error\nUnknown action. Use read_log, read_file, write_file, run_command, finish.";
        };
    }

    private String runCommand(String command) throws IOException, InterruptedException
    {
        if (command == null || command.isBlank())
            return "No command.";
        if (!isCommandAllowed(command))
            return "Command blocked by admin-ai allowlist/denylist: " + command;

        List<String> tokens = CommandLine.split(command);
        Path outputFile = Files.createTempFile("mountaindewritoes-adminai-", ".log");
        ProcessBuilder builder = new ProcessBuilder(tokens);
        builder.directory(Path.of(config.getString("admin-ai|actions|working-directory")).toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(outputFile.toFile());
        Process process = builder.start();
        currentProcess.set(process);
        boolean done = process.waitFor(config.getInt("admin-ai|max-command-seconds"), TimeUnit.SECONDS);
        currentProcess.compareAndSet(process, null);
        if (!done)
        {
            process.destroyForcibly();
            Files.deleteIfExists(outputFile);
            return "Command timed out: " + command;
        }
        String output = Files.readString(outputFile, StandardCharsets.UTF_8);
        Files.deleteIfExists(outputFile);
        return "exit=" + process.exitValue() + "\n" + truncate(output, 16000);
    }

    private boolean isCommandAllowed(String command)
    {
        String padded = " " + command.toLowerCase(Locale.ROOT) + " ";
        for (String denied : config.getStringList("admin-ai|actions|denied-command-contains"))
            if (padded.contains(denied.toLowerCase(Locale.ROOT)))
                return false;

        List<String> commandTokens = CommandLine.split(command);
        for (String prefix : config.getStringList("admin-ai|actions|allowed-command-prefixes"))
        {
            List<String> prefixTokens = CommandLine.split(prefix);
            if (commandTokens.size() < prefixTokens.size())
                continue;
            boolean matches = true;
            for (int i = 0; i < prefixTokens.size(); i++)
                if (!commandTokens.get(i).equals(prefixTokens.get(i)))
                    matches = false;
            if (matches)
                return true;
        }
        return false;
    }

    private String readAllowedFile(String path, boolean logOnly) throws IOException
    {
        Path resolved = resolveAllowedPath(path, logOnly);
        if (!Files.exists(resolved))
            return "File does not exist: " + resolved;
        int maxBytes = config.getInt("admin-ai|max-file-bytes");
        if (logOnly)
            return tail(resolved, config.getInt("admin-ai|log-tail-lines"), maxBytes);
        byte[] bytes = Files.readAllBytes(resolved);
        return truncate(new String(bytes, StandardCharsets.UTF_8), maxBytes);
    }

    private String writeAllowedFile(String path, String content) throws IOException
    {
        if (content == null)
            content = "";
        if (content.getBytes(StandardCharsets.UTF_8).length > config.getInt("admin-ai|max-file-bytes"))
            return "Write blocked: content exceeds max-file-bytes.";
        Path resolved = resolveAllowedPath(path, false);
        Files.createDirectories(resolved.getParent());
        Files.writeString(resolved, content, StandardCharsets.UTF_8);
        return "Wrote " + resolved;
    }

    private Path resolveAllowedPath(String path, boolean logOnly) throws IOException
    {
        Path raw = Path.of(path);
        if (!raw.isAbsolute())
            raw = Path.of(config.getString("admin-ai|actions|working-directory")).resolve(raw);
        Path resolved = Files.exists(raw) ? raw.toRealPath().normalize() : raw.toAbsolutePath().normalize();

        List<String> roots = logOnly ? config.getStringList("admin-ai|actions|log-files") : config.getStringList("admin-ai|actions|source-roots");
        for (String root : roots)
        {
            Path allowed = Path.of(root).toAbsolutePath().normalize();
            if (logOnly)
            {
                if (resolved.equals(allowed))
                    return resolved;
            }
            else if (resolved.startsWith(allowed))
                return resolved;
        }
        throw new IOException("Path blocked by admin-ai path policy: " + path);
    }

    private String tail(Path path, int lines, int maxBytes) throws IOException
    {
        ArrayList<String> allLines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                allLines.add(line);
                if (allLines.size() > lines)
                    allLines.remove(0);
            }
        }
        return truncate(String.join("\n", allLines), maxBytes);
    }

    private AiAction parseAction(String response)
    {
        String json = response.trim();
        if (json.startsWith("```"))
        {
            int firstNewline = json.indexOf('\n');
            int lastFence = json.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline)
                json = json.substring(firstNewline + 1, lastFence).trim();
        }
        try
        {
            return gson.fromJson(json, AiAction.class);
        }
        catch (JsonSyntaxException e)
        {
            AiAction action = new AiAction();
            action.action = "finish";
            action.message = "Provider returned non-JSON response: " + truncate(response, 300);
            return action;
        }
    }

    private String buildInitialPrompt(String userPrompt)
    {
        return "Task requested at " + Instant.now() + " UTC:\n" + userPrompt + "\n\nConfigured log files:\n"
                + String.join("\n", config.getStringList("admin-ai|actions|log-files"));
    }

    private String systemPrompt()
    {
        return """
                You are an autonomous Minecraft server admin maintenance agent running inside a Bukkit plugin.
                Your job: inspect logs/source, run allowed commands, fix source files only when needed, commit and push only when the task requires it.
                You must respond with exactly one JSON object and no prose.
                Valid actions:
                {"action":"read_log","path":"/absolute/log/path"}
                {"action":"read_file","path":"relative/or/absolute/path"}
                {"action":"write_file","path":"relative/or/absolute/path","content":"full new file content"}
                {"action":"run_command","command":"allowed command"}
                {"action":"finish","message":"summary"}
                Safety & Tools:
                - Use `~/a/updatething.sh` to pull and build updates for plugins instead of manual git/maven commands when a general update is requested.
                - Never request destructive commands.
                - Use read_file before write_file.
                - Prefer git diff/status before commit.
                - Commit messages must be concise and normal English.
                - If blocked by policy, explain in finish message.
                """;
    }

    private void ensureStillEnabled() throws InterruptedException
    {
        if (!config.isEnabled() || Thread.currentThread().isInterrupted())
            throw new InterruptedException("Admin AI disabled or interrupted.");
    }

    private void send(CommandSender sender, String message)
    {
        plugin.getServer().getScheduler().runTask(plugin, () -> sender.sendMessage(message));
    }

    private boolean isDestructive(String actionName)
    {
        return "write_file".equals(actionName) || "run_command".equals(actionName);
    }

    private String truncate(String input, int max)
    {
        if (input == null || input.length() <= max)
            return input;
        return input.substring(0, max);
    }

    private String nullToEmpty(String input)
    {
        return input == null ? "" : input;
    }
}
