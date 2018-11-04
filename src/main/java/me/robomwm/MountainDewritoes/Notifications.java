package me.robomwm.MountainDewritoes;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 11/3/2018.
 *
 * @author RoboMWM
 */
public class Notifications implements Listener
{
    private Plugin plugin;
    private Scoreboard mainScoreboard;
    Map<Player, ActionCenter> infoBoards = new HashMap<>();

    public Notifications(Plugin plugin)
    {
        this.plugin = plugin;
        this.mainScoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ActionCenter getActionCenter(Player player)
    {
        ActionCenter scoreboardEntry = infoBoards.get(player);
        if (scoreboardEntry == null)
            new ActionCenter(plugin, this, player, 200);
        return infoBoards.get(player);
    }

    public boolean addEntry(Player player, List<String> lines, String category)
    {
        ActionCenter actionCenter = getActionCenter(player);
        if (actionCenter == null)
            return false;

        return actionCenter.addEntry(category, lines);
    }
}

class ActionCenter
{
    private Plugin plugin;
    private Notifications manager;
    private Scoreboard scoreboard;
    private Player player;
    private BukkitTask expireTask;
    private int expirationLength;
    private Map<String, List<String>> entries = new LinkedHashMap<>();

    ActionCenter(Plugin plugin, Notifications manager, Player player, int expireTimeInTicks)
    {
        //Some other plugin/method is using scoreboard, yield to that.
        if (player.getScoreboard() != plugin.getServer().getScoreboardManager().getMainScoreboard())
            return;
        this.manager = manager;
        this.plugin = plugin;
        manager.infoBoards.put(player, this);
        this.expirationLength = expireTimeInTicks;
        this.player = player;

        //TODO initialize and set scoreboard
    }

    public boolean refreshDisplay()
    {
        if (player.getScoreboard() != scoreboard)
        {
            unregister();
            return false;
        }

        int lineCount = 0;
        do
        {
            if (lineCount > 16)
                entries.remove(entries.keySet().iterator().next());
            lineCount = 0;
            for (List<String> lines : entries.values())
                lineCount += lines.size();
        }
        while (lineCount > 16);

        //TODO: set scoreboard entries
        for (Map.Entry<String, List<String>> test : entries.entrySet())
        {

        }

        return true;
    }

    public boolean addEntry(String category, List<String> lines)
    {
        entries.remove(category);
        entries.put(category, lines);
        refreshExpiration();
        return refreshDisplay();
    }

    public void refreshExpiration()
    {
        cancel();
        expireTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                unregister();
            }
        }.runTaskLater(plugin, expirationLength);
    }

    private void cancel()
    {
        if (expireTask != null)
            expireTask.cancel();
    }

    public void unregister()
    {
        cancel();
        if (manager.infoBoards.get(player) == this)
            manager.infoBoards.remove(player);
        if (player.getScoreboard() == scoreboard)
            player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
        //TODO: Is additional cleanup required??
    }
}
