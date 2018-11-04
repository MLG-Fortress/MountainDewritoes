package me.robomwm.MountainDewritoes;

import com.github.games647.scoreboardstats.SbManager;
import com.github.games647.scoreboardstats.ScoreboardStats;
import me.robomwm.MountainDewritoes.Events.TransactionEvent;
import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 8/15/2017.
 *
 * @author RoboMWM
 */
public class ScoreboardStuff implements Listener
{
    private JavaPlugin instance;
    private Map<Player, List<String>> currentScoreboardView;
    private Map<Player, BukkitTask> removalTasks = new HashMap<>();
    private SbManager sbManager;

    public ScoreboardStuff(JavaPlugin plugin)
    {
        try
        {
            ScoreboardStats scoreboardStats = (ScoreboardStats)plugin.getServer().getPluginManager().getPlugin("ScoreboardStats");
            if (scoreboardStats == null || !scoreboardStats.isEnabled())
                return;
            sbManager = scoreboardStats.getScoreboardManager();
            //currentScoreboardView = new HashMap<>();
            removalTasks = new HashMap<>();
            this.instance = plugin;
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
        catch (Throwable rock)
        {
            plugin.getLogger().warning("ScoreboardStats not found or needs an update.");
        }

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

    @EventHandler
    private void onTransaction(TransactionEvent event)
    {
        if (sbManager == null)
            return;
        Player player = event.getPlayer();
        sbManager.unregister(player);
        sbManager.createScoreboard(player);

        if (event.getAmount() > 0)
        {
            sbManager.update(player, "Credit:  " + ChatColor.GREEN + "+" + event.getEconomy().format(event.getAmount()), 1);
            player.playSound(player.getLocation(), "fortress.credit", SoundCategory.PLAYERS, 300000f, 1.0f);
        }
        else if (event.getAmount() < 0)
        {
            sbManager.update(player, "Debit:   " + ChatColor.RED + event.getEconomy().format(event.getAmount()), 1);
            player.playSound(player.getLocation(), "fortress.debit", SoundCategory.PLAYERS, 300000f, 1.0f);
        }

        sbManager.update(player, "Balance:  " + event.getEconomy().format(event.getEconomy().getBalance(player)), 0);
        scheduleScoreboardRemoval(sbManager, player, instance, 160L);
    }
}
