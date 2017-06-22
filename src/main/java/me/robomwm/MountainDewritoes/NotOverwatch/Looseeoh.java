package me.robomwm.MountainDewritoes.NotOverwatch;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Created on 6/20/2017.
 *
 * @author RoboMWM
 */
public class Looseeoh implements Listener
{
    Ogrewatch ogrewatch;
    JavaPlugin instance;

    Looseeoh(JavaPlugin plugin, Ogrewatch wat)
    {
        this.ogrewatch = wat;
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    void wallRide(PlayerToggleFlightEvent event)
    {
        Player player = event.getPlayer();
        if (!ogrewatch.isLucio(player))
            return;

        event.setCancelled(true);
        Vector velocity = player.getVelocity();
        //Block block = velocity.add(velocity).toLocation(player.getWorld()).getBlock();
        Block block = player.getLocation().getBlock();

        //Near an adjacent, solid block?
        if (block.getRelative(BlockFace.NORTH).getType().isTransparent()
                && block.getRelative(BlockFace.SOUTH).getType().isTransparent()
                && block.getRelative(BlockFace.EAST).getType().isTransparent()
                && block.getRelative(BlockFace.WEST).getType().isTransparent())
            return;

        //Can only ride on solid blocks
//        if (block.getType().isTransparent())
//            return;

        player.sendMessage("wallriding");

        //TODO: check if already wallriding?

        Vector ridingVector = velocity;
        if (Math.abs(ridingVector.getX()) > Math.abs(ridingVector.getZ()))
        {
            velocity.setZ(0);
            velocity.setX(1);
        }
        else
        {
            velocity.setX(0);
            velocity.setZ(1);
        }
        velocity.setY(0.01);


        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.setVelocity(ridingVector);

                Block block1 = player.getLocation().getBlock();
                //Near an adjacent, solid block?
            if (block1.getRelative(BlockFace.NORTH).getType().isTransparent()
                && block1.getRelative(BlockFace.SOUTH).getType().isTransparent()
                && block1.getRelative(BlockFace.EAST).getType().isTransparent()
                && block1.getRelative(BlockFace.WEST).getType().isTransparent())
                cancel();
            if (!player.isOnline() || player.isOnGround())
                cancel();
            }
        }.runTaskTimer(instance, 0L, 2L);
    }
}
