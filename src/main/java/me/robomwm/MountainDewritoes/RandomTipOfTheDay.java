package me.robomwm.MountainDewritoes;

import me.clip.actionannouncer.ActionAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    Random random = new Random();
    RandomTipOfTheDay(Main blah)
    {
        illRefactorSomeday = blah;
        randomTips.add("Mobs may drop a health canister; use these to add an extra heart.");
        randomTips.add("Long fall boots (iron boots) prevent all fall damage!");
        randomTips.add("We could always use more staff, feel free to /apply");
        randomTips.add("Bored? Talk to U_W0T_B0T by mentioning its name in chat!");
        randomTips.add("We run polls in the /motd so you can vote on new features!");
        randomTips.add("ur message culd b here! Just bcome staff by /apply m8");
        randomTips.add("Play Watch2Win by typing /ad 2 win some neuuuu stooof");
    }
    @EventHandler
    void onPlayerJoinToDeliverATip(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        new BukkitRunnable()
        {
            public void run()
            {
                if (player.isOnline())
                {
                    String tip = randomTips.get(random.nextInt(randomTips.size()));
                    ActionAPI.sendTimedPlayerAnnouncement(illRefactorSomeday, player, ChatColor.GOLD + tip, 20);
                }
            }
        }.runTaskLater(illRefactorSomeday, 1200L);
    }
}
