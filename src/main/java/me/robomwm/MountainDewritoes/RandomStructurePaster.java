package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

import java.util.Random;

/**
 * Created by RoboMWM on 5/26/2016.
 * Made this because I didn't want to bother setting biomeheight and volatility of 62 biomes
 * by hand, nor create a program to do that to randomly spawn BO3 objects in TerrainControl
 * http://dev.bukkit.org/bukkit-plugins/terrain-control/forum/60715-amplified-1-7-2/?post=5
 */
public class RandomStructurePaster implements Listener
{
    World world = Bukkit.getWorld("world");

    @EventHandler(ignoreCancelled = true)
    void onChunkGen(ChunkPopulateEvent event)
    {
        if (!event.getWorld().equals(world))
            return;

        if (new Random().nextInt(20) != 1)
            return;
    }
}
