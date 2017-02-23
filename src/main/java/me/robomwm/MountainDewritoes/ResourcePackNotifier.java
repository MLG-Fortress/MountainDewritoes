package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by RoboMWM on 1/28/2017.
 */
public class ResourcePackNotifier implements Listener
{
    MountainDewritoes instance;

    ResourcePackNotifier(MountainDewritoes instance)
    {
        this.instance = instance;
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                if (!event.getPlayer().isOnline())
                    this.cancel();
                else if (!event.getPlayer().isOnGround())
                    return;
                else
                {
                    event.getPlayer().setResourcePack("https://github.com/MLG-Fortress/MLG-Pack-2.1/releases/download/alpha/MLG-Pack-2.1-alpha.zip");
                    this.cancel();
                }
            }
        }.runTaskTimer(instance, 200L, 100L);
    }
    @EventHandler
    void statusOfPack(PlayerResourcePackStatusEvent event)
    {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED)
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "irc say samplebot #MLG " + event.getPlayer().getName() + " denied da meme pack.");

            new BukkitRunnable()
            {
                public void run()
                {
                    if (event.getPlayer().isOnline())
                        event.getPlayer().sendMessage(ChatColor.GOLD + "Ayyy, we noticed u denied our meme resource pack. Please enable it by editing the server in your servers list.");
                }
            }.runTaskLater(instance, 600L);
        }
    }
}
