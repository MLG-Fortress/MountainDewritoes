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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

    //Delayed set playerListName (Primarily for onJoin, since Essentials sets displayName late)
    //Automatically adds appropriate spacing
    public void setListName(final String p, final String prefix, final String colorCode)
    {
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                Player player = Bukkit.getPlayer(p);
                if (player == null)
                    return;
                player.setPlayerListName(prefix + colorCode + " " + player.getDisplayName());
            }
        }, 20L);
    }

    //Delayed setDisplayName
    public void setDisplayName(final String p, final String colorCode)
    {
        //Don't alter if player name is already colored
        scheduler.scheduleSyncDelayedTask(instance, new Runnable() {
            public void run() {
                Player player1 = Bukkit.getPlayer(p);
                if (player1 == null)
                    return;
                if (player1.getDisplayName().startsWith(player1.getName()))
                    player1.setDisplayName("§" + colorCode + player1.getName());
            }
        }, 20L); //Ensure Essentials sets displayName before we set displayName (Essentials sets it later)
    }

    //Sets a player's appropriate chat color and prefix.
    public void setClanPrefix(Player player)
    {
        final String playerName = player.getName();
        final String colorCode = getColorCode(player);

        //Set colored display name
        setDisplayName(playerName, colorCode);

        ClanPlayer clanPlayer = clanManager.getClanPlayer(player); //TODO: null check needed?
        Clan clan = clanPlayer.getClan();
        if (clan == null)
        {
            //If not part of a clan, set colored prefix and do no more
            setListName(player.getName(), colorCode, "");
            return;
        }

        final String tag = ("§7" + clan.getColorTag());

        //Feature: set prefix in tablist
        //compatible with other prefix/suffix plugins since we just set PlayerListName
        setListName(playerName, tag, colorCode);

        //Feature: set prefix in nameplate
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                Player player = Bukkit.getPlayerExact(playerName);
                Team team = sb.getTeam(playerName);
                if (team == null || player == null)
                    return;
                //Get displayName color (player can change color via /nick)
                String color = player.getDisplayName().substring(0,2);
                team.setPrefix(tag + " " + color);
            }
        }, 40L); //Ensure healthbar made the team
    }



}
