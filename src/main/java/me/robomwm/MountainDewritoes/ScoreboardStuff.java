package me.robomwm.MountainDewritoes;

import com.github.games647.scoreboardstats.SbManager;
import com.github.games647.scoreboardstats.ScoreboardStats;
import com.github.games647.scoreboardstats.scoreboard.bukkit.BukkitScoreboardManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
    private Map<Player, Double> oldBalances = new HashMap<>();
    private Map<Player, BukkitTask> removalTasks = new HashMap<>();
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
                    {
                        oldBalances.put(player, economy.getBalance(player));
                        continue;
                    }

                    int oldBalance = oldBalances.get(player).intValue();
                    int balance = (int)economy.getBalance(player);
                    int difference = balance - oldBalance;
                    if (difference != 0)
                    {
                        sbManager.unregister(player);
                        sbManager.createScoreboard(player);

                        if (difference > 0)
                        {
                            sbManager.update(player, "Credit:  " + ChatColor.GREEN + "+" + economy.format(difference), 1);
                            player.playSound(player.getLocation(), "fortress.credit", SoundCategory.PLAYERS, 300000f, 1.0f);
                        }
                        else if (difference < 0)
                        {
                            sbManager.update(player, "Debit:   " + ChatColor.RED + "-" + economy.format(-difference), 1);
                            player.playSound(player.getLocation(), "fortress.debit", SoundCategory.PLAYERS, 300000f, 1.0f);
                        }


                        sbManager.update(player, "Balance:  " + economy.format(balance), 0);
                        scheduleScoreboardRemoval(sbManager, player, plugin, 100L);
                        oldBalances.put(player, economy.getBalance(player));
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 10L);
    }

    private void onQuit(PlayerQuitEvent event)
    {
        oldBalances.remove(event.getPlayer());
    }

    private void scheduleScoreboardRemoval(SbManager sbManager, Player player, JavaPlugin plugin, long delay)
    {
        if (removalTasks.containsKey(player))
            removalTasks.get(player).cancel();

        BukkitTask task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                sbManager.unregister(player);
            }
        }.runTaskLater(plugin, delay);

        removalTasks.put(player, task);
    }
}
