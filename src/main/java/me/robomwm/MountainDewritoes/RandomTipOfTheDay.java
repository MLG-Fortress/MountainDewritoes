package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by RoboMWM on 5/27/2016.
 */
public class RandomTipOfTheDay implements Listener
{
    Main illRefactorSomeday;
    List<String> randomTips = new ArrayList<>();
    Random random;
    RandomTipOfTheDay(Main blah)
    {
        illRefactorSomeday = blah;
        randomTips.add("Mobs may drop a health canister, which you can use to add an extra heart.");
        randomTips.add("Long fall boots negate all fall damage! Just craft some iron boots to get 'em.");
        randomTips.add("We could always use more staff, feel free to /apply");
    }
    @EventHandler
    void onPlayerJoinToDeliverATip(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        new BukkitRunnable()
        {
            public void run()
            {
                if (player.isOnline())
                {
                    String tip = randomTips.get(random.nextInt(randomTips.size()));
                    //Too lazy to add a dependency right now, especially since it's not mavenized
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "aa send " + player.getName() + " 10 " + tip);
                }
            }
        }.runTaskLater(illRefactorSomeday, 2400L);
    }
}
