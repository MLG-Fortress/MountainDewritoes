package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by RoboMWM on 1/28/2017.
 */
public class ResourcePackNotifier implements Listener
{
    MountainDewritoes instance;
    String pack;
    Set<UUID> ignoredUUIDs = new HashSet<>();

    ResourcePackNotifier(MountainDewritoes instance)
    {
        this.instance = instance;
        ignoredUUIDs.add(UUID.fromString("a1a23a3f-ab44-45c9-b484-76c99ae8fba8"));
        pack = instance.getConfig().getString("pack");
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        if (pack == null || pack.isEmpty())
            return;
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
                    event.getPlayer().setResourcePack(pack);
                    this.cancel();
                }
            }
        }.runTaskTimer(instance, 40L, 100L);
    }
    @EventHandler
    void statusOfPack(PlayerResourcePackStatusEvent event)
    {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED)
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "irc say samplebot #MLG " + event.getPlayer().getName() + " denied da meme pack.");

            if (ignoredUUIDs.contains(event.getPlayer().getUniqueId()))
                return;

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
