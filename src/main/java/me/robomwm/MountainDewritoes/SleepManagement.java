package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Sounds.AtmosphericManager;
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
    AtmosphericManager atmosphericManager;
    boolean playedMorning = false;
    boolean playedNight = false;

    public SleepManagement(MountainDewritoes mountainDewritoes, AtmosphericManager atmosphericManager)
    {
        this.instance = mountainDewritoes;
        this.atmosphericManager = atmosphericManager;
        monitorTimeInWorld();
    }

    /**Sync time and whatnot, calls TimeDawnEvent and Dusk, etc.*/
    void monitorTimeInWorld()
    {
        World world = instance.getServer().getWorld("world");
        World cityworld = instance.getServer().getWorld("cityworld");
        World mall = instance.getServer().getWorld("mall");
        World spawn = instance.getServer().getWorld("minigames");

        cityworld.setTime(world.getTime()); //Initial sync
        mall.setTime(world.getTime());
        spawn.setTime(world.getTime());
        new BukkitRunnable()
        {
            public void run()
            {
                Long worldTime = world.getTime();
                if (cityworld.getTime() != worldTime)
                    cityworld.setTime(worldTime); //ReSync
                mall.setTime(worldTime); //Should always be in sync
                spawn.setTime(worldTime);
                if (world.getTime() > 23000)
                    morningEventCall();
                else if (worldTime > 13000 && worldTime < 14000)
                    nightEventCall();
            }
        }.runTaskTimer(instance, 200L, 200L);
    }

    void morningEventCall()
    {
        if (playedMorning) return;
        atmosphericManager.morningListener();
        playedMorning = true;
        playedNight = false;
    }

    void nightEventCall()
    {
        if (playedNight) return;
        atmosphericManager.nightListener();
        playedNight = true;
        playedMorning = false;
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
