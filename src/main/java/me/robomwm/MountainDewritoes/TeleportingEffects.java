package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by RoboMWM on 9/16/2016.
 * Yes, I'm _not_ going to re-implement EssentialsX's teleportation system just so I can add some stupid effects
 * Instead, I'll just do a lazy implementation of its warmup and watch commandpreprocess cuz yea.
 * I understand this probably doesn't account for cooldowns or whatever. Oh well.
 */
public class TeleportingEffects implements Listener
{
    World spawn = Bukkit.getWorld("minigames");
    World mall = Bukkit.getWorld("mall");
    Map<Player, Location> preTeleportingPlayers = new HashMap<>();
    MountainDewritoes instance;

    TeleportingEffects(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void playerWannaTeleport(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (world == spawn || world == mall)
            return;
        if (preTeleportingPlayers.containsKey(player))
            return;

        String message = event.getMessage().toLowerCase();
        if (message.startsWith("/tpa") || message.startsWith("/warp") || message.startsWith("/spawn") || message.startsWith("/mall") || message.startsWith("/back"))
        {
            preTeleportingPlayers.put(player, player.getLocation());
            new BukkitRunnable()
            {
                public void run()
                {
                    try
                    {
                        if (preTeleportingPlayers.get(player).distanceSquared(player.getLocation()) < 0.3D)
                            world.playEffect(player.getLocation().add(0.0d, 1.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
                        else
                        {
                            preTeleportingPlayers.remove(player);
                            this.cancel();
                        }
                    }
                    catch (Exception e)
                    {
                        preTeleportingPlayers.remove(player);
                        this.cancel();
                    }
                }
            }.runTaskTimer(instance, 10L, 10L);
        }
    }
}
