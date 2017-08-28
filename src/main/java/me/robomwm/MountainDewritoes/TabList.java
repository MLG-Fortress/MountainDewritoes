package me.robomwm.MountainDewritoes;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 8/28/2017.
 *
 * @author RoboMWM
 */
public class TabList implements Listener
{
    public TabList(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        event.getPlayer().setPlayerListHeaderFooter(
                TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cM&eL&dG &bFortress")),
                TextComponent.fromLegacyText(ChatColor.AQUA + "IP: MLGFORT.US.TO"));
    }
}
