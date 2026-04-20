package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataStore
{
    private final NamespacedKey nameColorKey;
    private final NamespacedKey expLevelKey;

    public PlayerDataStore(JavaPlugin plugin)
    {
        this.nameColorKey = new NamespacedKey(plugin, "name_color");
        this.expLevelKey = new NamespacedKey(plugin, "exp_level");
    }

    public ChatColor getNameColor(Player player)
    {
        String color = player.getPersistentDataContainer().get(nameColorKey, PersistentDataType.STRING);
        if (color != null)
            return ChatColor.valueOf(color);

        int colorCode = Math.abs(player.getUniqueId().hashCode());
        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e".split(",");
        colorCode = colorCode % acceptableColors.length;
        return ChatColor.getByChar(acceptableColors[colorCode]);
    }

    public void setNameColor(Player player, ChatColor color)
    {
        player.getPersistentDataContainer().set(nameColorKey, PersistentDataType.STRING, color.name());
    }

    public int getStoredExpLevel(Player player)
    {
        Integer level = player.getPersistentDataContainer().get(expLevelKey, PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    public void setStoredExpLevel(Player player, int level)
    {
        player.getPersistentDataContainer().set(expLevelKey, PersistentDataType.INTEGER, level);
    }
}
