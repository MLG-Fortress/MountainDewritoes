package me.robomwm.MountainDewritoes;

import com.github.games647.scoreboardstats.SbManager;
import com.github.games647.scoreboardstats.ScoreboardStats;
import com.github.games647.scoreboardstats.scoreboard.bukkit.BukkitScoreboardManager;
import me.robomwm.usefulutil.UsefulUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 8/15/2017.
 *
 * @author RoboMWM
 */
public class ScoreboardStuff implements Listener
{
    private Map<Player, Double> oldBalances = new HashMap<>();
    private Map<Player, BukkitTask> removalTasks = new HashMap<>();
    private Map<Player, List<String>> transactions = new HashMap<>();
    Pattern negativeSign;
    Economy economy;

    public ScoreboardStuff(JavaPlugin plugin, Economy economy)
    {
        this.economy = economy;
        negativeSign = Pattern.compile("-");

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
                    double balance = economy.getBalance(player);
                    double difference = balance - oldBalance;
                    if (difference != 0)
                    {
                        addTransaction(player, difference);
                        String differenceString = properFormat(difference, false);

                        sbManager.unregister(player);
                        sbManager.createScoreboard(player);

                        if (difference > 0)
                        {
                            sbManager.update(player, "Credit:  " + ChatColor.GREEN + "+" + differenceString, 1);
                            player.playSound(player.getLocation(), "fortress.credit", SoundCategory.PLAYERS, 300000f, 1.0f);
                        }
                        else if (difference < 0)
                        {
                            sbManager.update(player, "Debit:   " + differenceString, 1);
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

    public String getTransactions(Player player)
    {
        if (!transactions.containsKey(player))
            return "No transactions occurred recently.";
        StringBuilder listOfTransactions = new StringBuilder();
        for (String transaction : transactions.get(player))
        {
            listOfTransactions.append(transaction);
            listOfTransactions.append("\n");
        }
        return listOfTransactions.toString();
    }

    private void addTransaction(Player player, double change)
    {
        if (!transactions.containsKey(player))
            transactions.put(player, new ArrayList<>());
        transactions.get(player).add(properFormat(change, true) + " " + UsefulUtil.formatTime() + " ago.");
    }

    private String properFormat(double amount, boolean color)
    {
        ChatColor chatColor;
        String format;
        if (amount < 0)
        {
            format = "-" + negativeSign.matcher(economy.format(amount)).replaceAll("");
            chatColor = ChatColor.RED;
        }
        else
        {
            format = economy.format(amount);
            chatColor = ChatColor.GREEN;
        }

        if (color)
            return chatColor + format;
        return format;
    }
}
