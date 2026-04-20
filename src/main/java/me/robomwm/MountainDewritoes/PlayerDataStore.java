package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
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

    private Player asOnlinePlayer(OfflinePlayer player)
    {
        return player.isOnline() ? (Player) player : null;
    }

    public ChatColor getNameColor(OfflinePlayer player)
    {
        Player onlinePlayer = asOnlinePlayer(player);
        String color = null;
        if (onlinePlayer != null)
        {
            PersistentDataContainer container = onlinePlayer.getPersistentDataContainer();
            color = container.get(nameColorKey, PersistentDataType.STRING);
        }
        if (color != null)
            return ChatColor.valueOf(color);

        int colorCode = Math.abs(player.getUniqueId().hashCode());
        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e".split(",");
        colorCode = colorCode % acceptableColors.length;
        return ChatColor.getByChar(acceptableColors[colorCode]);
    }

    public void setNameColor(OfflinePlayer player, ChatColor color)
    {
        Player onlinePlayer = asOnlinePlayer(player);
        if (onlinePlayer == null)
            return;
        onlinePlayer.getPersistentDataContainer().set(nameColorKey, PersistentDataType.STRING, color.name());
    }

    public int getExpLevel(OfflinePlayer player)
    {
        Player onlinePlayer = asOnlinePlayer(player);
        if (onlinePlayer == null)
            return 0;
        Integer level = onlinePlayer.getPersistentDataContainer().get(expLevelKey, PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    public void setExpLevel(OfflinePlayer player, int level)
    {
        Player onlinePlayer = asOnlinePlayer(player);
        if (onlinePlayer == null)
            return;
        onlinePlayer.getPersistentDataContainer().set(expLevelKey, PersistentDataType.INTEGER, level);
    }
}
