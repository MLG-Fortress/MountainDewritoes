package me.robomwm.MountainDewritoes.notifications;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Created on 11/4/2018.
 *
 * @author RoboMWM
 */
public abstract class NotificationSender implements Listener
{
    private Notifications notifications;

    public NotificationSender(Notifications notifications, Plugin plugin)
    {
        this.notifications = notifications;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void addEntry(Player player, String category, List<String> lines)
    {
        notifications.addEntry(player, lines, category);
    }
}
