package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 8/19/2017.
 *
 * @author RoboMWM
 */
public class TitleManager implements Listener
{
    private MountainDewritoes instance;
    private Map<Player, TitleMeta> usingTitlePlayers = new HashMap<>();
    private Map<Player, TitleMeta> usingActionPlayers = new HashMap<>();
    private Title blankTitle;

    public TitleManager(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.instance = plugin;
        Title.Builder titleBuilder = new Title.Builder();
        titleBuilder.title(" ");
        titleBuilder.subtitle(" ");
        blankTitle = titleBuilder.build();
    }

    public boolean isUsingTitle(Player player)
    {
        return usingTitlePlayers.containsKey(player);
    }

    /**
     *
     * @param player
     * @param priority
     * @param title
     * @return whether it successfully printed a title to the player
     */
    public boolean sendTitle(Player player, int priority, Title title)
    {
        //Deny if priority is lesser than existing title's priority and is currently active
        if (isUsingTitle(player))
        {
            TitleMeta titleMeta = usingTitlePlayers.get(player);
            if (titleMeta.getDuration() > instance.getCurrentTick() && titleMeta.getPriority() > priority)
                return false;
        }

        long duration = title.getFadeIn() + title.getStay();
        usingTitlePlayers.put(player, new TitleMeta(priority, instance.getCurrentTick() + duration));
        player.sendTitle(title);
        return true;
    }

    /**
     * Removes titles at or below the specified priority (since we do not ID title messages)
     * @param player
     */
    public void removeTitle(Player player, int priority)
    {
        if (!isUsingTitle(player))
        {
            player.sendTitle(blankTitle);
            return;
        }
        TitleMeta titleMeta = usingTitlePlayers.get(player);
        if (titleMeta.getPriority() > priority)
            return;
        player.sendTitle(blankTitle);
        usingTitlePlayers.remove(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        usingTitlePlayers.remove(event.getPlayer());
    }

    public boolean timedActionBar(Player player, int seconds, String message, int priority)
    {
        if (message == null || player == null)
            return true;

        //Deny if priority is lesser than existing action message's priority and is currently active
        if (usingActionPlayers.containsKey(player))
        {
            TitleMeta actionMeta = usingActionPlayers.get(player);
            if (actionMeta.getDuration() > instance.getCurrentTick() && actionMeta.getPriority() > priority)
                return false;
        }

        if (seconds <= 0)
        {
            player.sendActionBar(message);
            return true;
        }

        TitleMeta actionMeta = new TitleMeta(priority, instance.getCurrentTick() + (seconds * 20));
        usingActionPlayers.put(player, actionMeta);

        new BukkitRunnable()
        {
            int secondsRemaining = seconds * 2;
            public void run()
            {
                if (actionMeta != usingActionPlayers.get(player))
                {
                    this.cancel();
                    return;
                }

                player.sendActionBar(message);
                secondsRemaining--;
                if (secondsRemaining <= 0 || !instance.getServer().getOnlinePlayers().contains(player))
                {
                    this.cancel();
                    usingActionPlayers.remove(player);
                }
            }
        }.runTaskTimer(instance, 0L, 10L);

        return true;
    }

    /**
     * Send an actionbar with a customizable duration
     * @param player
     * @param seconds
     * @param message
     */
    public boolean timedActionBar(Player player, int seconds, String message)
    {
        return timedActionBar(player, seconds, message, 0);
    }
}

class TitleMeta
{
    private int priority;
    private long duration;

    TitleMeta(int priority, long duration)
    {
        this.priority = priority;
        this.duration = duration;
    }

    public long getDuration()
    {
        return duration;
    }

    public int getPriority()
    {
        return priority;
    }
}
