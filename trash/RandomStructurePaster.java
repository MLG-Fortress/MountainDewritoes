package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 5/26/2016.
 * Made this because I didn't want to bother setting biomeheight and volatility of 62 biomes
 * by hand, nor create a program to do that to randomly spawn BO3 objects in TerrainControl
 * http://dev.bukkit.org/bukkit-plugins/terrain-control/forum/60715-amplified-1-7-2/?post=5
 */
public class RandomStructurePaster implements Listener
{
    List<String> schematics = new ArrayList<>();
    MountainDewritoes instance;
    RandomStructurePaster(MountainDewritoes blah)
    {
        //schematics.add("loopysquish_brickhouse");
        //schematics.add("Monsta-Lazy");
        //schematics.add("nikita_cheetah_village");
        //schematics.add("nikita_cheetah_beach");
        instance = blah;
        instance.registerListener(this);
    }

    //@EventHandler(ignoreCancelled = true)
    void onChunkGen(ChunkLoadEvent event) //only new chunks call this, yes?
    {
        if (!event.isNewChunk())
            return;
        if (!instance.isSurvivalWorld(event.getWorld()))
            return;

        if (r4nd0m(0, 1000) != 1)
            return;

        Location location = event.getChunk().getBlock(r4nd0m(0, 15),64,r4nd0m(0, 15)).getLocation();
        location.setY(event.getWorld().getHighestBlockYAt(location));

        new BukkitRunnable()
        {
            public void run()
            {
                Schematic.paste(schematics.get(r4nd0m(0, schematics.size() - 1)), location);
            }
        }.runTaskLater(instance, 200L);
    }

    public int r4nd0m(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
