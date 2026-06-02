package me.robomwm.MountainDewritoes.adminai;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

class AdminAiConfig
{
    private final MountainDewritoes plugin;

    AdminAiConfig(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        installDefaults();
    }

    void reload()
    {
        plugin.reloadConfig();
        installDefaults();
    }

    private void installDefaults()
    {
        FileConfiguration config = plugin.getConfig();
        config.addDefault("admin-ai|enabled", false);
        config.addDefault("admin-ai|interactive", true);
        config.addDefault("admin-ai|max-iterations", 12);
        config.addDefault("admin-ai|max-command-seconds", 300);
        config.addDefault("admin-ai|max-file-bytes", 65536);
        config.addDefault("admin-ai|log-tail-lines", 200);
        config.addDefault("admin-ai|provider-order", List.of("relayfree", "ollama-proxy", "chat-api"));

        config.addDefault("admin-ai|providers|relayfree|enabled", false);
        config.addDefault("admin-ai|providers|relayfree|protocol", "openai-chat-completions");
        config.addDefault("admin-ai|providers|relayfree|endpoint", "http://127.0.0.1:8000/v1/chat/completions");
        config.addDefault("admin-ai|providers|relayfree|model", "meta-model");
        config.addDefault("admin-ai|providers|relayfree|api-key", "relay-free");
        config.addDefault("admin-ai|providers|relayfree|timeout-seconds", 90);

        config.addDefault("admin-ai|providers|ollama-proxy|enabled", false);
        config.addDefault("admin-ai|providers|ollama-proxy|protocol", "openai-chat-completions");
        config.addDefault("admin-ai|providers|ollama-proxy|endpoint", "http://127.0.0.1:11434/v1/chat/completions");
        config.addDefault("admin-ai|providers|ollama-proxy|model", "qwen2.5-coder:latest");
        config.addDefault("admin-ai|providers|ollama-proxy|api-key", "ollama");
        config.addDefault("admin-ai|providers|ollama-proxy|timeout-seconds", 90);

        config.addDefault("admin-ai|providers|chat-api|enabled", false);
        config.addDefault("admin-ai|providers|chat-api|protocol", "simple-chat-api");
        config.addDefault("admin-ai|providers|chat-api|endpoint", "http://127.0.0.1:11434/chat/api");
        config.addDefault("admin-ai|providers|chat-api|model", "qwen2.5-coder:latest");
        config.addDefault("admin-ai|providers|chat-api|api-key", "");
        config.addDefault("admin-ai|providers|chat-api|timeout-seconds", 90);

        config.addDefault("admin-ai|actions|working-directory", "/home/robo/buildupdates/MountainDewritoes");
        config.addDefault("admin-ai|actions|source-roots", List.of("/home/robo/buildupdates/MountainDewritoes"));
        config.addDefault("admin-ai|actions|log-files", List.of("/home/robo/a/logs/latest.log", "/home/robo/a/plugins/ErrorSink/errors.log"));
        config.addDefault("admin-ai|actions|allowed-command-prefixes", List.of(
                "git status",
                "git diff",
                "git add",
                "git commit",
                "git push",
                "mvn -B --no-transfer-progress compile",
                "mvn -B --no-transfer-progress test",
                "mvn -B --no-transfer-progress package",
                "mvn -B --no-transfer-progress clean package",
                "mvn -B --no-transfer-progress clean install",
                "/home/robo/a/updatething.sh",
                "/home/robo/a/updatething.sh --check-keywords"
        ));
        config.addDefault("admin-ai|actions|denied-command-contains", List.of(
                " rm ", " rm -", " reset --hard", " checkout --", " clean -fd", " clean -fx", " rebase ", " push --force",
                " --force", " --amend", " chmod ", " chown "
        ));
        config.options().copyDefaults(true);
        plugin.getDataFolder().mkdirs();
        plugin.saveConfig();
    }

    boolean isEnabled()
    {
        return plugin.getConfig().getBoolean("admin-ai|enabled", false);
    }

    void setEnabled(boolean enabled)
    {
        plugin.getConfig().set("admin-ai|enabled", enabled);
        plugin.saveConfig();
    }

    boolean isInteractive()
    {
        return plugin.getConfig().getBoolean("admin-ai|interactive", true);
    }

    void setInteractive(boolean interactive)
    {
        plugin.getConfig().set("admin-ai|interactive", interactive);
        plugin.saveConfig();
    }

    int getInt(String path)
    {
        return plugin.getConfig().getInt(path);
    }

    List<String> getStringList(String path)
    {
        return plugin.getConfig().getStringList(path);
    }

    String getString(String path)
    {
        return plugin.getConfig().getString(path, "");
    }

    List<AiProvider> getProviders()
    {
        List<AiProvider> providers = new ArrayList<>();
        for (String name : plugin.getConfig().getStringList("admin-ai|provider-order"))
        {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("admin-ai|providers|" + name);
            if (section == null || !section.getBoolean("enabled"))
                continue;
            providers.add(new AiProvider(
                    name,
                    section.getString("protocol", "openai-chat-completions"),
                    section.getString("endpoint", ""),
                    section.getString("model", ""),
                    section.getString("api-key", ""),
                    section.getInt("timeout-seconds", 90)
            ));
        }
        return providers;
    }
}
