package me.robomwm.MountainDewritoes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by RoboMWM on 5/26/2016.
 * Made this because I didn't want to bother setting biomeheight and volatility of 62 biomes
 * by hand, nor create a program to do that to randomly spawn BO3 objects in TerrainControl
 * http://dev.bukkit.org/bukkit-plugins/terrain-control/forum/60715-amplified-1-7-2/?post=5
 */
public class RandomStructurePaster implements Listener
{
    World world = Bukkit.getWorld("world");
    List<String> schematics = new ArrayList<>();
    Random random;
    Main iKnowIShouldntCallItMainOhWell;
    RandomStructurePaster(Main blah)
    {
        schematics.add("banditplayz");
        schematics.add("bradymc_loopysquish_blockhunt");
        schematics.add("diamond");
        schematics.add("diamond1");
        schematics.add("diamond2");
        schematics.add("diamond3");
        schematics.add("Etanarvazac_MobArena_Idea");
        schematics.add("hazedfordayz");
        schematics.add("iagario_shop");
        schematics.add("iagario_village");
        schematics.add("lazyspawn");
        schematics.add("lazyspawnquartz");
        schematics.add("loopysquish_brickhouse");
        schematics.add("loyalpvp_blockhunt");
        schematics.add("Monsta-Lazy");
        schematics.add("nikita_cheetah_village");
        schematics.add("nikita_cheetah_beach");
        schematics.add("tylerboyer360amn_parkour");
        schematics.add("tylerboyer360amn_shop");
        schematics.add("war-Lazy");
        schematics.add("ravens_prey_spleef");
        random = new Random();
        iKnowIShouldntCallItMainOhWell = blah;
    }

    @EventHandler(ignoreCancelled = true)
    void onChunkGen(ChunkPopulateEvent event) //This is always called for new chunks, yea?
    {
        if (!event.getWorld().equals(world))
            return;

        if (random.nextInt(20) != 1)
            return;

        Location location = event.getChunk().getBlock(7,64,7).getLocation();
        new BukkitRunnable()
        {
            public void run()
            {
                Schematic.paste(schematics.get(random.nextInt(schematics.size())), location);
            }
        }.runTaskLater(iKnowIShouldntCallItMainOhWell, 20L);
    }
}