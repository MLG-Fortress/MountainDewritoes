package me.robomwm.MountainDewritoes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2/22/2018.
 *
 * @author RoboMWM
 */
public class AntiLag implements Listener
{
    private Map<Player, Integer> viewDistance = new HashMap<>();

    public AntiLag(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerChangesWorldSetViewDistance(PlayerTeleportEvent event)
    {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN)
            return;
        if (event.getFrom().getWorld() == event.getTo().getWorld() && event.getFrom().distanceSquared(event.getTo()) < 1024)
            return;

        Player player = event.getPlayer();

        if (player.hasMetadata("DEAD") || player.getViewDistance() == 3)
            return;

        viewDistance.put(player, player.getViewDistance());
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        if (viewDistance.containsKey(player) && player.isOnGround())
            player.setViewDistance(viewDistance.get(player));
    }
}
