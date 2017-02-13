package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Music.AtmosphericManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by RoboMWM on 11/26/2016.
 */
public class SleepManagement implements Listener
{
    MountainDewritoes instance;
    World world;
    boolean playedMorning = false;
    boolean playedNight = false;

    public SleepManagement(MountainDewritoes mountainDewritoes)
    {
        this.instance = mountainDewritoes;
        world = instance.getServer().getWorld("world");
        monitorTimeInWorld();
    }

    /**Sync time and whatnot, calls TimeDawnEvent and Dusk, etc.*/
    void monitorTimeInWorld()
    {
        World cityworld = instance.getServer().getWorld("cityworld");
        World mall = instance.getServer().getWorld("mall");
        World spawn = instance.getServer().getWorld("minigames");
        new BukkitRunnable()
        {
            public void run()
            {
                syncTime(cityworld);
                syncTime(mall);
                syncTime(spawn);
            }
        }.runTaskTimer(instance, 1200L, 1200L);
    }

    void syncTime(World targetWorld) //Allows for worlds to not be loaded (recovering from backup or w/e) without the entire plugin breaking
    {
        if (targetWorld == null)
            return;
        if (world.getTime() == targetWorld.getTime())
            return;
        targetWorld.setTime(world.getTime());
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
                    player.eject();
            }
        }.runTaskLater(instance, 80L);
    }
}

//if (world.getTime() > 23000)
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