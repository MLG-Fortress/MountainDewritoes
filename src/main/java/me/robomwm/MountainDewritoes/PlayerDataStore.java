package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataStore
{
    private final JavaPlugin plugin;
    private final Map<UUID, YamlConfiguration> cache = new HashMap<>();
    private final File folder;

    public PlayerDataStore(JavaPlugin plugin)
    {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "grandPlayers");
        folder.mkdirs();
    }

    private YamlConfiguration getYaml(OfflinePlayer player)
    {
        UUID uuid = player.getUniqueId();
        if (!cache.containsKey(uuid))
        {
            File file = new File(folder, uuid + ".yml");
            cache.put(uuid, YamlConfiguration.loadConfiguration(file));
        }
        return cache.get(uuid);
    }

    private void save(OfflinePlayer player)
    {
        try
        {
            getYaml(player).save(new File(folder, player.getUniqueId() + ".yml"));
        }
        catch (IOException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + player.getUniqueId(), e);
        }
    }

    public ChatColor getNameColor(OfflinePlayer player)
    {
        String color = getYaml(player).getString("nameColor");
        if (color != null)
            return ChatColor.valueOf(color);

        int colorCode = Math.abs(player.getUniqueId().hashCode());
        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e".split(",");
        colorCode = colorCode % acceptableColors.length;
        return ChatColor.getByChar(acceptableColors[colorCode]);
    }

    public void setNameColor(OfflinePlayer player, ChatColor color)
    {
        getYaml(player).set("nameColor", color.name());
        save(player);
    }

    public int getExpLevel(OfflinePlayer player)
    {
        return getYaml(player).getInt("expLevel");
    }

    public void setExpLevel(OfflinePlayer player, int level)
    {
        getYaml(player).set("expLevel", level);
        save(player);
    }
}
