package me.robomwm.MountainDewritoes;

import me.robomwm.BetterTPA.PostTPATeleportEvent;
import me.robomwm.BetterTPA.PreTPATeleportEvent;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by RoboMWM on 9/16/2016.
 * <s>Yes, I'm _not_ going to re-implement EssentialsX's teleportation system just so I can add some stupid effects</s>
 * Guess what, I end up doing just that lol.
 * <s>Instead, I'll just do a lazy implementation of its warmup and watch commandpreprocess cuz yea.</s>
 * <s>I understand this probably doesn't account for cooldowns or whatever. Oh well.</s>
 */
public class TeleportingEffects implements Listener
{
    private Set<World> knownWorlds = new HashSet<>();
    private Map<Player, Location> preTeleportingPlayers = new HashMap<>();
    private Map<Player, BukkitTask> taskThingy = new HashMap<>();
    private MountainDewritoes plugin;

    TeleportingEffects(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        knownWorlds.add(plugin.getServer().getWorld("world"));
        knownWorlds.add(plugin.getServer().getWorld("world_nether"));
        knownWorlds.add(plugin.getServer().getWorld("cityworld"));
        knownWorlds.add(plugin.getServer().getWorld("cityworld_nether"));
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerPreTPA(PreTPATeleportEvent event)
    {
        if (toOrFromUnknownWorld(event.getPlayer().getLocation()))
        {
            event.setReason(ChatColor.GRAY.toString() + ChatColor.ITALIC + "***Static noises***");
            event.getPlayer().playSound(event.getPlayer().getLocation(), "fortress.static", SoundCategory.AMBIENT, 3000000f, 1.0f);
            event.setCancelled(true);
            return;
        }

        if (toOrFromUnknownWorld(event.getTargetLocation()))
        {
            event.setReason("We can't locate " + event.getTarget().getDisplayName() + ", perhaps they're in another dimension?");
            event.setCancelled(true);
            return;
        }

        if (event.getTargetLocation().getWorld().getEnvironment() == World.Environment.NETHER ||
                event.getPlayer().getWorld().getEnvironment() == World.Environment.NETHER)
        {
            event.setWarmup(400L);
        }
    }

    private boolean toOrFromUnknownWorld(Location location)
    {
        World world = location.getWorld();
        return !plugin.isSafeWorld(world) && plugin.isSurvivalWorld(world) && !knownWorlds.contains(world);
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
                event.setReason(target.getDisplayName() + " is busy and not able to be teleported to at this time.");
                event.setCancelled(true);
            }
        }

        if (!event.isCancelled())
        {
            playTeleportEffect(event.getPlayer());
            long warmup = event.getWarmup();
            if (warmup > 0L && warmup < 160L)
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), "fortress.transporter", SoundCategory.BLOCKS, 1.0f, 1.0f);
            else if (warmup > 160L)
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), "fortress.transporter_long", SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerTPACancel(PostTPATeleportEvent event)
    {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        preTeleportingPlayers.remove(event.getPlayer());
        if (taskThingy.containsKey(event.getPlayer()))
            taskThingy.remove(event.getPlayer()).cancel();

        if (event.isCancelled())
        {
            //Stop transporter sound
            for (Player p : location.getWorld().getPlayers())
            {
                if (location.distanceSquared(p.getLocation()) < 576D) //24 blocks
                {
                    p.stopSound("fortress.transporter", SoundCategory.BLOCKS);
                    p.stopSound("fortress.transporter_long", SoundCategory.BLOCKS);
                }
            }
            return;
        }

        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), "fortress.transporter_finish", SoundCategory.BLOCKS, 1.5f, 1.0f);
        location.getWorld().playEffect(location.add(0.0d, 1.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
        //TODO: sound effect
        if (event.getTarget() != null)
            plugin.getTitleManager().timedActionBar(event.getTarget(), 5, player.getDisplayName() + ChatColor.AQUA + " teleported to you.");
    }

    void playTeleportEffect(Player player)
    {
        preTeleportingPlayers.put(player, player.getLocation());
        final World world = player.getWorld();
        final Location location = player.getLocation();
        BukkitTask task = new BukkitRunnable()
        {
            int times = 120;
            public void run()
            {
                world.playEffect(location, Effect.ENDER_SIGNAL, 0, 10);
                world.playEffect(location.add(0.0d, 1.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
                world.playEffect(location.add(0.0d, 1.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
                location.add(0.0d, -2.0d, 0.0d);
                if (times-- < 0)
                {
                    plugin.getLogger().warning("Had to time out effect. " + player.getName() + " at " + location.toString());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
        taskThingy.put(player, task);
    }
}
