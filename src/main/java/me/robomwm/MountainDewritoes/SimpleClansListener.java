package me.robomwm.MountainDewritoes;

import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Created by Robo on 2/13/2016.
 */
public class SimpleClansListener implements Listener
{
    private Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
    ClanManager clanManager;
    BukkitScheduler scheduler = Bukkit.getScheduler();
    public Main instance;

    public SimpleClansListener(Main main)
    {
        SimpleClans sc = (SimpleClans)Bukkit.getPluginManager().getPlugin("SimpleClans");
        clanManager = sc.getClanManager();
        instance = main;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        ClanPlayer clanPlayer = clanManager.getClanPlayer(player);
        if (clanPlayer == null)
            return;
        Clan clan = clanPlayer.getClan();
        if (clan == null) //If not part of a clan, do no more
            return;


        final String tag = clan.getColorTag();

        //Feature: set prefix in tablist
        //compatible with other prefix/suffix plugins since we just set PlayerListName
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                player.setPlayerListName(tag + " " + player.getDisplayName());
            }
        }, 30L); //Long delay to ensure this has priority & no need to instantly set


        scheduler.scheduleSyncDelayedTask(instance, new Runnable() {
            public void run()
            {
                Team team = sb.getTeam(player.getName());
                if (team == null)
                    return;
                team.setPrefix("§7" + tag + " §f"); //TODO: Get name color and use that instead
            }
        }, 40L); //Ensure healthbar made the team

    }

    //Not really a SimpleClans feature but too lazy to make it a separate plugin right now
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.getRecipients().size() < 2)
            return; //ignore if they're the only one on or softmuted

        final Player player = event.getPlayer();
        String mess = event.getMessage();
        if (mess.length() > 14)
            mess = mess.substring(0, 10) + "...";
        final String message = mess;
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                //TODO: are the 3 lines below thread-safe?
                Team team = sb.getTeam(player.getName());
                if (team == null)
                    return;
                team.setSuffix(": " + message);
            }
        });

        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                Team team = sb.getTeam(player.getName());
                if (team == null)
                    return;
                if (team.getSuffix().equals(": " + message))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "healthbar reloadplayer " + player.getName());
            }
        }, 140L);


    }

}
