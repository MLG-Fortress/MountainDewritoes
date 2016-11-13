package me.robomwm.MountainDewritoes;

import me.clip.actionannouncer.ActionAPI;
import me.robomwm.BetterTPA.PostTPATeleportEvent;
import me.robomwm.BetterTPA.PreTPATeleportEvent;
import org.bukkit.Bukkit;
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
    World spawn = Bukkit.getWorld("minigames");
    World mall = Bukkit.getWorld("mall");
    Map<Player, Location> preTeleportingPlayers = new HashMap<>();
    Map<Player, BukkitTask> taskThingy = new HashMap<>();
    MountainDewritoes instance;

    TeleportingEffects(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void playerWannaTeleport(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        String[] message = event.getMessage().split(" ");
        String command = message[0].toLowerCase();
        //I really wish EssentialsX provided an API/event for this. Am gonna make issue 4 dis now
        if ((message.length > 1 && command.equals("/warp"))
                || (command.equals("/spawn") || command.equals("/mall") || command.equals("/back")))
        {
            playTeleportEffect(player);
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
        taskThingy.remove(event.getPlayer()).cancel();
        preTeleportingPlayers.remove(event.getPlayer());
        if (event.isCancelled())
            return;
        ActionAPI.sendTimedPlayerAnnouncement(instance, event.getTarget(), event.getPlayer().getDisplayName() + ChatColor.AQUA + " teleported to you.", 5);
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
                        taskThingy.remove(player);
                    }
                }
                catch (IllegalArgumentException e)
                {
                    preTeleportingPlayers.remove(player);
                    this.cancel();
                    taskThingy.remove(player);
                }
            }
        }.runTaskTimer(instance, 10L, 10L);
        taskThingy.put(player, task);
    }
}
