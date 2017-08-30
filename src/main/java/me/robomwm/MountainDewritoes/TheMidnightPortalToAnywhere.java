package me.robomwm.MountainDewritoes;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 8/29/2017.
 *
 * @author RoboMWM
 */
public class TheMidnightPortalToAnywhere implements Listener
{
    MountainDewritoes instance;
    List<World> enabledWorlds = new ArrayList<>();
    YamlConfiguration storedPortals;

    public TheMidnightPortalToAnywhere(MountainDewritoes plugin)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        enabledWorlds.add(instance.getServer().getWorld("world"));
        enabledWorlds.add(instance.getServer().getWorld("world_nether"));
        enabledWorlds.add(instance.getServer().getWorld("cityworld"));
        enabledWorlds.add(instance.getServer().getWorld("cityworld_nether"));

        //As much as I wanted to use some sort of mathematical function that links both locations, storing the location will help preserve the portal link if I ever change worldborders/worlds/etc.
        File storageFile = new File(plugin.getDataFolder(), "portal.data");
        if (!storageFile.exists())
        {
            try
            {
                storageFile.createNewFile();
            }
            catch (IOException e)
            {
                plugin.getLogger().severe("Could not create portal.data!");
                e.printStackTrace();
                return;
            }
        }
        storedPortals = YamlConfiguration.loadConfiguration(storageFile);

        for (World world : enabledWorlds)
        {
            if (storedPortals.getConfigurationSection(world.getName()) == null)
                storedPortals.createSection(world.getName());
        }

        saveStoredPortals();
    }

    private void saveStoredPortals()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                File storageFile = new File(instance.getDataFolder(), "portal.data");
                try
                {
                    storedPortals.save(storageFile);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.runTask(instance);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerPortal(PlayerPortalEvent event)
    {
        Location location = randomChunkMappedLocation(event.getFrom());
        if (location == null)
            return;
        event.useTravelAgent(true);
        event.getPortalTravelAgent().setSearchRadius(36);
        event.setTo(location);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityPortal(EntityPortalEvent event)
    {
        Location location = randomChunkMappedLocation(event.getFrom());
        if (location == null)
            return;
        event.useTravelAgent(true);
        event.getPortalTravelAgent().setSearchRadius(36);
        event.setTo(location);
    }

    private boolean isVanillaNether(World world)
    {
        if (world.getEnvironment() != World.Environment.NETHER)
            return false;

        if (world.getLoadedChunks().length == 0)
            world.loadChunk(0, 0);
        return world.getLoadedChunks()[0].getBlock(0, 128, 0).getType() == Material.BEDROCK;
    }

    private Location randomChunkMappedLocation(Location location)
    {
        Chunk chunk = location.getChunk();
        if (!enabledWorlds.contains(chunk.getWorld()))
            return null;

        String chunkId = String.valueOf(chunk.getX()) + "," + chunk.getZ();

        if (storedPortals.getConfigurationSection(chunk.getWorld().getName()).contains(chunkId))
            return (Location)storedPortals.getConfigurationSection(chunk.getWorld().getName()).get(chunkId);

        Random random = ThreadLocalRandom.current(); //Turns out since I'm storing generated links anyways, there's no need to seed the random object... oh well
        World world = enabledWorlds.get(random.nextInt(enabledWorlds.size()));

        //Find the min and max block locations within the worldborder to use in our random retriever thingy
        Location borderCenter;
        int borderSize;

        if (world.getWorldBorder() == null || world.getWorldBorder().getCenter() == null) //Apparently this can be null......
        {
            borderCenter = new Location(world, 0, 0, 0);
            borderSize = 60000000;
        }
        else
        {
            borderCenter = world.getWorldBorder().getCenter();
            borderSize = (int)(world.getWorldBorder().getSize() / 2) - 10000; //i.e. world must have a border size far above 10000
        }
        int maxX = borderCenter.getBlockX() + borderSize;
        int minX = borderCenter.getBlockX() - borderSize;
        int maxZ = borderCenter.getBlockZ() + borderSize;
        int minZ = borderCenter.getBlockZ() - borderSize;

        int x = random.nextInt(maxX + 1 - minX) + minX;
        int z = random.nextInt(maxZ + 1 - minZ) + minZ;
        int y;
        if (isVanillaNether(world))
            y = random.nextInt(110) + 6;
        else
            y = random.nextInt(240) + 6;

        //Save linked portal locations
        Location toLocation = new Location(world, x, y, z);
        storedPortals.getConfigurationSection(chunk.getWorld().getName()).set(chunkId, toLocation);
        chunk = toLocation.getChunk();
        chunkId = String.valueOf(chunk.getX()) + "," + chunk.getZ();
        storedPortals.getConfigurationSection(chunk.getWorld().getName()).set(chunkId, location);
        saveStoredPortals();

        return toLocation;
    }
}
