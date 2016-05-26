package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by RoboMWM on 5/25/2016.
 * Reimplements sound effect on death and other, future miscellaneous stuff
 */
public class DeathListener implements Listener
{
    Main instance;
    DeathListener(Main iKnowIShouldntCallItMain)
    {
        instance = iKnowIShouldntCallItMain;
    }

    @EventHandler
    void onPlayerSadness(PlayerDeathEvent event)
    {
        final Player player = event.getEntity();
        //Believe it or not, the Minecraft client does not even trigger this sound on player death,
        //it just plays player_hurt, so yea...
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);

        //Auto-respawn player if they haven't clicked respawn within the last 20 seconds
        //Helps prevent weird client problems like client-side entity buildup or whatever,
        //thus freezing the client or idk that's what happened to me.
        new BukkitRunnable()
        {
            public void run()
            {
                player.spigot().respawn();
            }
        }.runTaskLater(instance, 400L);
    }
}
