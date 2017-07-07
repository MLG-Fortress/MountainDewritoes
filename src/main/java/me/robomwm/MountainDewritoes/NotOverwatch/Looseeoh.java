package me.robomwm.MountainDewritoes.NotOverwatch;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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
    void wallRide(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();
        if (!ogrewatch.isLucio(player))
            return;
        if (player.isOnGround())
            return;

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
        }
        else
        {
            velocity.setX(0);
        }
        velocity.setY(0.01);


        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //Increase absolute value of x or z component slowly towards 1...
                if (ridingVector.getX() > 0 && ridingVector.getX() < 1)
                    ridingVector.setX(ridingVector.getX() + 0.05);
                else if (ridingVector.getX() < 0 && ridingVector.getX() > -1)
                    ridingVector.setX(ridingVector.getX() - 0.05);
                else if (ridingVector.getZ() > 0 && ridingVector.getZ() < 1)
                    ridingVector.setZ(ridingVector.getZ() + 0.05);
                else if (ridingVector.getZ() < 0 && ridingVector.getZ() > -1)
                    ridingVector.setZ(ridingVector.getZ() - 0.05);

                player.setVelocity(ridingVector);

                Block block1 = player.getLocation().getBlock();

                if (!player.isOnline() || !ogrewatch.isLucio(player) || player.isOnGround())
                {
                    cancel();
                    return;
                }

                    //Near an adjacent, solid block?
                if (block1.getRelative(BlockFace.NORTH).getType().isTransparent()
                    && block1.getRelative(BlockFace.SOUTH).getType().isTransparent()
                    && block1.getRelative(BlockFace.EAST).getType().isTransparent()
                    && block1.getRelative(BlockFace.WEST).getType().isTransparent())
                    cancel();
            }
        }.runTaskTimer(instance, 0L, 1L);
    }
}
