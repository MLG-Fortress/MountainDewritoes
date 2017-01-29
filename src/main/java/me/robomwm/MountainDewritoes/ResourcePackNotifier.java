package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

/**
 * Created by RoboMWM on 1/28/2017.
 */
public class ResourcePackNotifier implements Listener
{
    @EventHandler(ignoreCancelled = true)
    void statusOfPack(PlayerResourcePackStatusEvent event)
    {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "say " + event.getPlayer() + " denied da meme pack.");
    }
}
