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


        //Get colored clan tag
        final String tag = clan.getColorTag();

        //Get a randomized, consistent color code for player
        final String colorCode = getColorCode(player);

        //Feature: set prefix in tablist
        //compatible with other prefix/suffix plugins since we just set PlayerListName
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                player.setPlayerListName(tag + " §" + colorCode + player.getDisplayName());
            }
        }, 30L); //Long delay to ensure this has priority & no need to instantly set


        scheduler.scheduleSyncDelayedTask(instance, new Runnable() {
            public void run()
            {
                Team team = sb.getTeam(player.getName());
                if (team == null)
                    return;
                team.setPrefix("§7" + tag + " §" + colorCode); //TODO: Get name color and use that instead
            }
        }, 40L); //Ensure healthbar made the team

    }

    public String getColorCode(Player player)
    {
        //TODO: Allow owner to choose unique to player or name
        //Get hash code of player's UUID
        int colorCode = player.getUniqueId().hashCode();
        //Ensure number is positive
        colorCode = Math.abs(colorCode);

        //Will make configurable, hence this
        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e,f,g".split(",");
        //Divide hash code by length of acceptableColors, and use remainder
        //to determine which index to use (like a hashtable/map/whatever)
        colorCode = (colorCode % acceptableColors.length);
        String stringColorCode = acceptableColors[colorCode];

        return stringColorCode;
    }

}
