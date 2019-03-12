package me.robomwm.MountainDewritoes.notifications;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * Created on 11/4/2018.
 *
 * @author RoboMWM
 */
public abstract class NotificationSender implements Listener
{
    private Notifications notifications;
    protected MountainDewritoes plugin;

    public NotificationSender(Notifications notifications, MountainDewritoes plugin)
    {
        this.notifications = notifications;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void addEntry(Player player, String category, List<String> lines)
    {
        notifications.addEntry(player, lines, category);
    }

    public void removeEntry(Player player, String category)
    {
        notifications.removeEntry(player, category);
    }
}
