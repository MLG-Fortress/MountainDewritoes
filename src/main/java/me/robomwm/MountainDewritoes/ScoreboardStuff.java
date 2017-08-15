package me.robomwm.MountainDewritoes;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 8/15/2017.
 *
 * @author RoboMWM
 */
public class ScoreboardStuff
{
    private Map<Player, Double> oldBalances = new HashMap<>();
    public ScoreboardStuff(JavaPlugin plugin, Economy economy)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {

            }
        }.runTaskTimer(plugin, 300L, 200L);
    }
}
