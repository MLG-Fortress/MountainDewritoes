package me.robomwm.MountainDewritoes;

import org.bukkit.GameRule;
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
    boolean playedMorning = false;
    boolean playedNight = false;

    public SleepManagement(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.instance = plugin;
        final World WORLD = instance.getServer().getWorld("firstjoin");

        new BukkitRunnable()
        {
            public void run()
            {
                for (World world : plugin.getServer().getWorlds())
                {
                    if (!world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))
                        continue;
                    if (WORLD.getTime() == world.getTime())
                        continue;
                    world.setTime(WORLD.getTime());
                }
            }
        }.runTaskTimer(instance, 20L, 6000L);
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
                    player.wakeup(true);
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