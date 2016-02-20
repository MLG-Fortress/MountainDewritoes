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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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

    //Set colors and prefix onJoin
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        setClanPrefix(event.getPlayer());
    }

    //Set colors and prefix if player changes clans
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage();
        final Player player = event.getPlayer();
        if (command.startsWith("/clan create ") || command.startsWith("/clan resign") || command.startsWith("/accept") || command.startsWith("/clan disband"))
        {
            scheduler.scheduleSyncDelayedTask(instance, new Runnable()
            {
                public void run()
                {
                    setClanPrefix(player);
                }
            }, 10L);
        }
    }

    //Get a randomized, consistent color code for player
    public String getColorCode(Player player)
    {
        //TODO: Allow owner to choose unique to player or name
        //Get hash code of player's UUID
        int colorCode = player.getUniqueId().hashCode();
        //Ensure number is positive
        colorCode = Math.abs(colorCode);

        //Will make configurable, hence this
        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e".split(",");
        //Divide hash code by length of acceptableColors, and use remainder
        //to determine which index to use (like a hashtable/map/whatever)
        colorCode = (colorCode % acceptableColors.length);
        String stringColorCode = acceptableColors[colorCode];

        return stringColorCode;
    }

    //Set playerListName if they are not in a clan
    public void setListName(Player p)
    {
        final Player player = p;
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                player.setPlayerListName(player.getDisplayName());
            }
        });
    }

    public void setClanPrefix(Player p)
    {
        final Player player = p;
        final String colorCode = getColorCode(player);

        //Don't alter if player name is already colored
        if (player.getDisplayName().startsWith(player.getName()))
            player.setDisplayName("§" + colorCode + player.getName());

        ClanPlayer clanPlayer = clanManager.getClanPlayer(player);
        if (clanPlayer == null) {
            setListName(player);
            return;
        }

        Clan clan = clanPlayer.getClan();
        if (clan == null) //If not part of a clan, do no more
        {
            setListName(player);
            return;
        }


        //Get colored clan tag
        final String tag = clan.getColorTag();


        //Feature: set prefix in tablist
        //compatible with other prefix/suffix plugins since we just set PlayerListName
        scheduler.scheduleSyncDelayedTask(instance, new Runnable() {
            public void run() {
                player.setPlayerListName("§7" + tag + " " + player.getDisplayName());
            }
        });


        scheduler.scheduleSyncDelayedTask(instance, new Runnable() {
            public void run() {
                Team team = sb.getTeam(player.getName());
                if (team == null)
                    return;
                team.setPrefix("§7" + tag + " §" + colorCode); //TODO: Get name color and use that instead
            }
        }, 40L); //Ensure healthbar made the team
    }

}
