package me.robomwm.MountainDewritoes.Sounds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by RoboMWM on 10/10/2016.
 * Currently testing to see how badly this impacts performance
 */
public class Footsteps implements Listener
{
    World MALL = Bukkit.getWorld("mall");
    World SPAWN = Bukkit.getWorld("minigames");

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerMoveFootstep(PlayerMoveEvent event)
    {
        //Don't care if player is just looking around
        if (event.getFrom().distanceSquared(event.getTo()) <= 0)
            return;

        Player player = event.getPlayer();
        if (player.isSneaking())
            return;

        //TODO: world checks

        final Location playerLocation = player.getLocation();
        final World playerWorld = player.getWorld();

        //TODO: check material under player's feet, and play according sound

        for (Player target : Bukkit.getOnlinePlayers())
        {
            if (target == player)
                return;
            if (target.getWorld() != playerWorld)
                return;
            if (target.getLocation().distanceSquared(playerLocation) < 256)
                target.playSound(playerLocation, "fortress.stone.step", 1.0f, 1.0f);
        }


    }
}
