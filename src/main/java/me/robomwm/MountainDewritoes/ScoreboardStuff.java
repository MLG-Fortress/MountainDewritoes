package me.robomwm.MountainDewritoes;

import com.github.games647.scoreboardstats.SbManager;
import com.github.games647.scoreboardstats.ScoreboardStats;
import com.github.games647.scoreboardstats.scoreboard.bukkit.BukkitScoreboardManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 8/15/2017.
 *
 * @author RoboMWM
 */
public class ScoreboardStuff implements Listener
{
    private JavaPlugin instance;
    private Map<Player, Double> oldBalances = new HashMap<>();
    public ScoreboardStuff(JavaPlugin plugin, Economy economy)
    {
        ScoreboardStats scoreboardStats = (ScoreboardStats)plugin.getServer().getPluginManager().getPlugin("ScoreboardStats");
        if (scoreboardStats == null)
            return;
        SbManager sbManager = scoreboardStats.getScoreboardManager();
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : plugin.getServer().getOnlinePlayers())
                {
                    if (!oldBalances.containsKey(player))
                        oldBalances.put(player, economy.getBalance(player));
                    int oldBalance = oldBalances.get(player).intValue();
                    int newBalance = (int)economy.getBalance(player);
                    int difference = newBalance - oldBalance;
                    if (difference != 0)
                    {
                        if (difference > 0)
                        {
                            sbManager.createScoreboard(player);
                            sbManager.update(player, ChatColor.GREEN + "Gained +", difference);
                            sbManager.update(player, ChatColor.AQUA + "Balance", newBalance);
                            scheduleScoreboardRemoval(sbManager, player, 2L);
                        }
                        else if (difference < 0)
                        {
                            sbManager.createScoreboard(player);
                            sbManager.update(player, ChatColor.RED + "Lost -", difference);
                            sbManager.update(player, ChatColor.AQUA + "Balance", newBalance);
                            scheduleScoreboardRemoval(sbManager, player, 2L);
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

    private void scheduleScoreboardRemoval(SbManager sbManager, Player player, long delay)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                sbManager.unregister(player);
            }
        }.runTaskLater(instance, delay);
    }
}
