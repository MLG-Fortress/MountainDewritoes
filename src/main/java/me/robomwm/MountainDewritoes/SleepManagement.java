package me.robomwm.MountainDewritoes;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by RoboMWM on 11/26/2016.
 */
public class SleepManagement implements Listener
{
    MountainDewritoes instance;
    World WORLD;
    private Set<World> worldsToSync = new HashSet<>();
    boolean playedMorning = false;
    boolean playedNight = false;

    public SleepManagement(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.instance = plugin;
        WORLD = instance.getServer().getWorld("cityworld");
        for (World world : plugin.getServer().getWorlds())
        {
            if (world.getGameRuleValue("doDaylightCycle").equals("false"))
                continue;
            worldsToSync.add(world);
        }

        worldsToSync.remove(WORLD);

        new BukkitRunnable()
        {
            public void run()
            {
                for (World world : worldsToSync)
                    syncTime(world);
            }
        }.runTaskTimer(instance, 1200L, 1200L);
    }

    void syncTime(World targetWorld) //Allows for worlds to not be loaded (recovering from backup or w/e) without the entire plugin breaking
    {
        if (targetWorld == null)
            return;
        if (WORLD.getTime() == targetWorld.getTime())
            return;
        targetWorld.setTime(WORLD.getTime());
    }



    /**
     * Eject player out of bed after sleeping for 4 seconds (and send them into a dream)
     * Untested
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onBedEnter(PlayerBedEnterEvent event)
    {
        Player player = event.getPlayer();
        new BukkitRunnable()
        {
            public void run()
            {
                if (player.isSleeping())
                    player.damage(0D);
            }
        }.runTaskLater(instance, 80L);
    }
}

//if (WORLD.getTime() > 23000)
//        morningEventCall();
//        else if (worldTime > 13000 && worldTime < 14000)
//        nightEventCall();

//    void morningEventCall()
//    {
//        if (playedMorning) return;
//        atmosphericManager.morningListener();
//        playedMorning = true;
//        playedNight = false;
//    }
//
//    void nightEventCall()
//    {
//        if (playedNight) return;
//        atmosphericManager.nightListener();
//        playedNight = true;
//        playedMorning = false;
//    }