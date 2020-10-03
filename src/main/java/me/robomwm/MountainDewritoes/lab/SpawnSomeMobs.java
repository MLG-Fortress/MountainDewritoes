package me.robomwm.MountainDewritoes.lab;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 10/2/2020.
 *
 * @author RoboMWM
 */
public class SpawnSomeMobs
{
    private MountainDewritoes plugin;
    private List<Location> locations = new ArrayList<>();

    public SpawnSomeMobs(MountainDewritoes plugin, World mall)
    {
        this.plugin = plugin;

        try
        {
            locations.add(new Location(mall, -43, 4, 157));
        }

        catch (Throwable rock)
        {
            return;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Location location : locations)
                {
                    World world = location.getWorld();

                    if (world.getPlayerCount() == 0)
                        return;

                    world.spawnEntity(location, getRandomMob());
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    //TODO: move to a util or something
    public EntityType getRandomMob()
    {
        List<EntityType> mobTypes = new ArrayList<>();

        for (EntityType type : EntityType.values())
        {
            if (type.isAlive() && type.isSpawnable())
                mobTypes.add(type);
        }

        return mobTypes.get(ThreadLocalRandom.current().nextInt(mobTypes.size()));
    }
}
