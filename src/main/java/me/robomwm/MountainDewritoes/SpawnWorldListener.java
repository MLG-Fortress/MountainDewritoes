package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
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
        spawn = Bukkit.getWorld("minigames");
        spawnLocation = new Location(spawn, -389D, 5D, -124D, 180.344f, -18.881f);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (player.getWorld() != spawn)
            return;
        if (player.hasPermission("i.am.jailed")) //ignore jailed players
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
