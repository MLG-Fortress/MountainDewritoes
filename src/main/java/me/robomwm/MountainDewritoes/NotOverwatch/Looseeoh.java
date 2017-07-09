package me.robomwm.MountainDewritoes.NotOverwatch;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;
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

    @EventHandler(ignoreCancelled = true)
    void wallRide(PlayerToggleSneakEvent event)
    {
        if (!event.isSneaking())
            return;
        Player player = event.getPlayer();
        if (!ogrewatch.isLucio(player))
            return;
        if (player.isOnGround())
            return;
        if (player.hasMetadata("MD_WALLRIDING"))
            return;

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

        Vector ridingVector = player.getVelocity();;
        //If player is not sprinting, they won't have any velocity in x or z direction
        //In this case, we'll just use the direction vector
        if (ridingVector.getX() == ridingVector.getZ())
            ridingVector = player.getLocation().getDirection();

        if (Math.abs(ridingVector.getX()) > Math.abs(ridingVector.getZ()))
        {
            ridingVector.setZ(0);
        }
        else
        {
            ridingVector.setX(0);
        }
        ridingVector.setY(0.04); //0.02 works for ideal conditions (no lag at all). Might try to "dynamically set" based on ping value.

        final Vector finalVector = ridingVector;

        player.setMetadata("MD_WALLRIDING", new FixedMetadataValue(instance, true));


        new BukkitRunnable()
        {
            Location lastLocation = player.getLocation().add(0, 2, 0);
            @Override
            public void run()
            {
                Location currentLocation = player.getLocation();
                //Increase absolute value of x or z component slowly towards 1...
                if (finalVector.getX() > 0 && finalVector.getX() < 1)
                    finalVector.setX(finalVector.getX() + 0.05);
                else if (finalVector.getX() < 0 && finalVector.getX() > -1)
                    finalVector.setX(finalVector.getX() - 0.05);
                else if (finalVector.getZ() > 0 && finalVector.getZ() < 1)
                    finalVector.setZ(finalVector.getZ() + 0.05);
                else if (finalVector.getZ() < 0 && finalVector.getZ() > -1)
                    finalVector.setZ(finalVector.getZ() - 0.05);

                player.setVelocity(finalVector);

                if (!player.isOnline() || !ogrewatch.isLucio(player) || player.isOnGround()
                        || player.getLocation().distanceSquared(lastLocation) < 0.5) //TODO: can lag break this check? (Do we care? Maybe we don't want a laggy player stuck wallriding either)
                {
                    cancel();
                    player.removeMetadata("MD_WALLRIDING", instance);
                    return;
                }

                Block block1 = player.getLocation().getBlock();

                    //Near an adjacent, solid block?
                if (block1.getRelative(BlockFace.NORTH).getType().isTransparent()
                    && block1.getRelative(BlockFace.SOUTH).getType().isTransparent()
                    && block1.getRelative(BlockFace.EAST).getType().isTransparent()
                    && block1.getRelative(BlockFace.WEST).getType().isTransparent())
                {
                    cancel();
                    player.removeMetadata("MD_WALLRIDING", instance);
                }

                lastLocation = player.getLocation();
            }
        }.runTaskTimer(instance, 0L, 1L);
    }
}
