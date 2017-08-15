package me.robomwm.MountainDewritoes;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 8/15/2017.
 *
 * @author RoboMWM
 */
public class ScoreboardStuff implements Listener
{
    private Map<Player, Double> oldBalances = new HashMap<>();
    public ScoreboardStuff(JavaPlugin plugin, Economy economy)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : plugin.getServer().getOnlinePlayers())
                {
                    if (!oldBalances.containsKey(player))
                        oldBalances.put(player, economy.getBalance(player));
                    double oldBalance = oldBalances.get(player);
                    double newBalance = economy.getBalance(player);
                    double difference = newBalance - oldBalance;
                    if (difference != 0)
                    {
                        if (difference > 0)
                        {

                        }
                        else if (difference < 0)
                        {

                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 300L, 200L);
    }

    private void onQuit(PlayerQuitEvent event)
    {
        oldBalances.remove(event.getPlayer());
    }
}
