package me.robomwm.MountainDewritoes;

import me.robomwm.BetterTPA.BetterTPA;
import me.robomwm.BetterTPA.PostTPATeleportEvent;
import me.robomwm.BetterTPA.PreTPATeleportEvent;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    Map<Player, Location> preTeleportingPlayers = new HashMap<>();
    Map<Player, BukkitTask> taskThingy = new HashMap<>();
    MountainDewritoes instance;

    TeleportingEffects(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerTPA(PreTPATeleportEvent event)
    {
        Player target = event.getTarget();
        //target checks
        if (target != null)
        {
            if (target.isDead() || target.hasMetadata("DEAD"))
            {
                event.setReason(target.getName() + " iz ded rite now :( Try again in a few seconds?");
                event.setCancelled(true);
            }
            else if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR)
            {
                event.setReason(target.getName() + " is not able to be teleported to at this time.");
                event.setCancelled(true);
            }
        }

        if (!event.isCancelled())
        {
            playTeleportEffect(event.getPlayer());
            //TODO: play sound effect
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerTPACancel(PostTPATeleportEvent event)
    {
        //TODO: stop sound effect
        Player player = event.getPlayer();
        Location location = player.getLocation();
        preTeleportingPlayers.remove(event.getPlayer());
        taskThingy.remove(event.getPlayer()).cancel();
        if (event.isCancelled())
            return;

        location.getWorld().playEffect(location.add(0.0d, 1.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
        //TODO: sound effect
        if (event.getTarget() != null)
            instance.timedActionBar(event.getTarget(), 5, player.getDisplayName() + ChatColor.AQUA + " teleported to you.");
    }

    void playTeleportEffect(Player player)
    {
        preTeleportingPlayers.put(player, player.getLocation());
        final World world = player.getWorld();
        final Location location = player.getLocation();
        BukkitTask task = new BukkitRunnable()
        {
            public void run()
            {
                world.playEffect(location.add(0.0d, 1.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
                world.playEffect(location.add(0.0d, 2.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
            }
        }.runTaskTimer(instance, 10L, 10L);
        taskThingy.put(player, task);
    }
}
