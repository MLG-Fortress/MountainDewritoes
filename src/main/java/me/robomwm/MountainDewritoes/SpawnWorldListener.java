package me.robomwm.MountainDewritoes;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by RoboMWM on 9/18/2016.
 */
public class SpawnWorldListener implements Listener
{
    World spawn;
    Location spawnLocation;
    MountainDewritoes instance;
    public SpawnWorldListener(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        spawn = instance.getServer().getWorld("spawn");
        spawnLocation = new Location(spawn, -389D, 5D, -124D, 180.344f, -18.881f);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (player.getWorld() != spawn)
            return;
        new BukkitRunnable()
        {
            public void run()
            {
                player.teleport(spawnLocation);
            }
        }.runTaskLater(instance, 1L);
    }
}
