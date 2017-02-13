package me.robomwm.MountainDewritoes;

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
    MountainDewritoes instance;
    List<String> randomTips = new ArrayList<>();
    Random random = new Random();
    RandomTipOfTheDay(MountainDewritoes blah)
    {
        instance = blah;
        randomTips.add("Mobs may drop a health canister; use these to add an extra heart.");
        randomTips.add("Long fall boots (iron boots) prevent " + ChatColor.BOLD + ChatColor.AQUA + "ALL fall damage!");
        randomTips.add("We could always use more staff, feel free to /apply");
        randomTips.add("Bored? Talk 2 U_W0T_B0T by mentioning it in chat!");
        randomTips.add("Got any suggestions for the MLG pack? Just state your opinions in chat!");
        randomTips.add("ur message culd b here! Just bcome staff by /apply m8");
        randomTips.add("Play Watch2Win by typing /ad 2 win some neuuuu stooof");
        randomTips.add("Need a crate key? Win one via an /ad or see if there's any at the /mall");
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
                    instance.timedActionBar(player, 20, ChatColor.GOLD + tip);
                }
            }
        }.runTaskLater(instance, 1200L);
    }
}
