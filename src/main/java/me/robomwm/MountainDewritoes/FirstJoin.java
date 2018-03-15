package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 2/23/2018.
 *
 * @author RoboMWM
 */
public class FirstJoin implements Listener
{
    private JavaPlugin plugin;
    private Location firstJoinLocation;

    public FirstJoin(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        firstJoinLocation = new Location(plugin.getServer().getWorld("firstjoin"), -1.5, 26.5, -3.5, 180, 20);
//        new BukkitRunnable()
//        {
//            Location location = new Location(WORLD, -1, 70, -4);
//            @Override
//            public void run()
//            {
//                for (Player player : WORLD.getPlayers())
//                {
//                    if (!NSA.getTempdata(player, "firstjoin"))
//                        return;
//                    if (player.getLocation().distanceSquared(location) > 1)
//                        return;
//                    NSA.removeTempdata(player, "firstjoin");
//                    player.sendBlockChange(new Location(WORLD, -1, 71, -6), Material.SMOOTH_BRICK, (byte)2);
//                    player.sendBlockChange(new Location(WORLD, -1, 70, -6), Material.SMOOTH_BRICK, (byte)1);
//                    player.sendBlockChange(new Location(WORLD, -2, 69, -4), Material.AIR, (byte)0);
//                    player.sendBlockChange(new Location(WORLD, -2, 70, -4), Material.AIR, (byte)0);
//                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, 1.0f);
//                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, 1.0f);
//                }
//            }
//        }.runTaskTimer(plugin, 1200L, 10L);
    }

    private void onJoinWorld(Player player)
    {
        player.teleport(firstJoinLocation);
        //player.sendBlockChange(new Location(WORLD, -2, 69, -4), Material.SUGAR_CANE_BLOCK, (byte)0);
        //player.sendBlockChange(new Location(WORLD, -2, 70, -4), Material.SUGAR_CANE_BLOCK, (byte)0);
        //NSA.setTempdata(player, "firstjoin");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (!event.getPlayer().hasPlayedBefore())
        {
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2D);
            event.getPlayer().setLevel(1);
            //TODO: give gold boots, etc.
        }

        event.getPlayer().setMaximumAir(event.getPlayer().getMaximumAir() + player.getLevel()); //Lol idk the default

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (player.getWorld() == firstJoinLocation.getWorld())
                    onJoinWorld(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event)
    {
        if (event.getPlayer().getWorld() == firstJoinLocation.getWorld())
            onJoinWorld(event.getPlayer());
    }

}
