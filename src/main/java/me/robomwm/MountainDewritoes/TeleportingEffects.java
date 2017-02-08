package me.robomwm.MountainDewritoes;

import me.robomwm.BetterTPA.BetterTPA;
import me.robomwm.BetterTPA.PostTPATeleportEvent;
import me.robomwm.BetterTPA.PreTPATeleportEvent;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
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
    Location spawnLocation;
    Location mallLocation;
    Map<Player, Location> preTeleportingPlayers = new HashMap<>();
    Map<Player, BukkitTask> taskThingy = new HashMap<>();
    MountainDewritoes instance;
    BetterTPA betterTPA;

    TeleportingEffects(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        betterTPA = (BetterTPA)instance.getServer().getPluginManager().getPlugin("BetterTPA");
        spawnLocation = new Location(instance.getServer().getWorld("minigames"), -389D, 5D, -124D, 180.344f, -18.881f);
        mallLocation = new Location(instance.getServer().getWorld("mall"), 2.488, 5, -7.305, 0f, 0f);
    }

    /**
     * Idk where I should put this. Maybe a class named "command overriders"
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onWantToTeleportToSpawnOrMall(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        String[] message = event.getMessage().split(" ");
        String command = message[0].toLowerCase();
        if (command.equals("/warp") && message.length > 1)
            command = message[1].toLowerCase();
        else
            command = command.substring(1, command.length());

        boolean warmup = player.getWorld() != spawnLocation.getWorld() && player.getWorld() != mallLocation.getWorld();
        event.setCancelled(true);
        switch (command)
        {
            case "spawn":
            case "hub":
            case "lobby":
                betterTPA.teleportPlayer(player, "spawn", spawnLocation, warmup, null);
                break;
            case "mall":
                betterTPA.teleportPlayer(player, "mall", mallLocation, warmup, null);
                break;
            default:
                event.setCancelled(false);
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerTPA(PreTPATeleportEvent event)
    {
        playTeleportEffect(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerTPACancel(PostTPATeleportEvent event)
    {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        preTeleportingPlayers.remove(event.getPlayer());
        taskThingy.remove(event.getPlayer()).cancel();
        if (event.isCancelled())
            return;
        location.getWorld().playEffect(location.add(0.0d, 1.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
        if (event.getTarget() != null)
            instance.timedBar(event.getTarget(), 5, player.getDisplayName() + ChatColor.AQUA + " teleported to you.");
    }

    void playTeleportEffect(Player player)
    {
        if (preTeleportingPlayers.containsKey(player))
            return;
        preTeleportingPlayers.put(player, player.getLocation());
        World world = player.getWorld();
        BukkitTask task = new BukkitRunnable()
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
                catch (IllegalArgumentException e)
                {
                    preTeleportingPlayers.remove(player);
                    this.cancel();
                }
            }
        }.runTaskTimer(instance, 10L, 10L);
        taskThingy.put(player, task);
    }
}
