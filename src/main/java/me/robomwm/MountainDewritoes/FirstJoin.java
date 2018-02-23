package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    private World WORLD;

    public FirstJoin(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        WORLD = plugin.getServer().getWorld("firstjoin");
        new BukkitRunnable()
        {
            Location location = new Location(WORLD, -1, 70, -4);
            @Override
            public void run()
            {
                for (Player player : WORLD.getPlayers())
                {
                    if (!NSA.getTempdata(player, "firstjoin"))
                        return;
                    if (player.getLocation().distanceSquared(location) > 1)
                        return;
                    NSA.removeTempdata(player, "firstjoin");
                    player.sendBlockChange(new Location(WORLD, -1, 71, -6), Material.SMOOTH_BRICK, (byte)2);
                    player.sendBlockChange(new Location(WORLD, -1, 70, -6), Material.SMOOTH_BRICK, (byte)1);
                    player.sendBlockChange(new Location(WORLD, -2, 69, -4), Material.AIR, (byte)0);
                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, 1.0f);
                }
            }
        }.runTaskTimer(plugin, 1200L, 10L);
    }

    private void onJoinWorld(Player player)
    {
        player.teleport(new Location(WORLD, 3.5, 100, 16.5, -90, 10));
        player.sendBlockChange(new Location(WORLD, -2, 69, -4), Material.SMOOTH_BRICK, (byte)2);
        NSA.setTempdata(player, "firstjoin");
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                onJoinWorld(event.getPlayer());
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event)
    {
        onJoinWorld(event.getPlayer());
    }

}
