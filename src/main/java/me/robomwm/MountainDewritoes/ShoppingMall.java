package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by RoboMWM on 6/1/2016.
 * All things related to shopping in da memetastic mall
 */
public class ShoppingMall implements Listener
{
    /**
     * Set walking speed when entering or leaving mall
     * @param event
     */
    World mallWorld = Bukkit.getWorld("mall");
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onWorldChange(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        //Reset speed when leaving mall
        if (event.getFrom().equals(mallWorld))
        {
            player.setWalkSpeed(0.2f);
            return;
        }

        //Increase speed when entering mall
        if (player.getWorld().equals(mallWorld))
        {
            player.setWalkSpeed(0.6f);
            //TODO: play fitting music for mall
        }
    }

    /**
     * Set walking speed if player joins inside mall
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerJoinInMall(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (player.getWorld().equals(mallWorld))
            player.setWalkSpeed(0.6f);
    }
}
