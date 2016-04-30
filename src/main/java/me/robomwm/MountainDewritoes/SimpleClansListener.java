package me.robomwm.MountainDewritoes;

import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
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
        //I'm 400,000% sure there's a better way to do this
        if (command.startsWith("/clan create ") || command.startsWith("/clan resign") || command.startsWith("/accept") || command.startsWith("/clan disband") || command.startsWith("/nick "))
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
    public void setListName(final Player player, final String prefix)
    {
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                if (!player.isOnline())
                    return;
                if (prefix.isEmpty())
                    player.setPlayerListName(player.getDisplayName());
                else
                    player.setPlayerListName(prefix + " " + player.getDisplayName());
            }
        }, 20L);
    }

    //Delayed setDisplayName
    //Now kinda useless, and not delayed.
    public void setDisplayName(final Player player1, final String colorCode)
    {
        if (!player1.hasPlayedBefore() && player1.getDisplayName().startsWith(player1.getName()))
        {
            scheduler.scheduleSyncDelayedTask(instance, new Runnable()
            {
                public void run()
                {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nick " + player1.getName() + " &" + colorCode + player1.getName());
                }
            });
        }
        //[16:34:22] RoboMWM: Does EssX constantly set or change the displayName? I'm able to change the displayName (I use it to setPlayerListName) but chat messages revert the color to white
        //[16:50:46] RoboMWM: Alright, so either I hook into Essentials and grab the nickname from there or implement my own sort of /nick

        //Don't alter if player name is already colored
//        scheduler.scheduleSyncDelayedTask(instance, new Runnable() {
//            public void run() {
//                if (!player1.isOnline())
//                    return;
//                if (player1.getDisplayName().startsWith(player1.getName()))
//                {
//                    player1.setDisplayName("\u00A7" + colorCode + player1.getName());
//                    System.out.println("display name of " + player1.getName() + " is: " + player1.getDisplayName());
//                }
//
//            }
//        }, 20L); //Ensure Essentials sets displayName before we set displayName (Essentials sets it later)
    }

    //Sets a player's appropriate chat color and prefix.
    public void setClanPrefix(final Player player)
    {
        final String colorCode = getColorCode(player);

        //Set colored display name
        setDisplayName(player, colorCode);

        ClanPlayer clanPlayer = clanManager.getClanPlayer(player);
        if (clanPlayer == null)
        {
            //Yes, a nullcheck is needed
            setListName(player, "");
            return;
        }
        Clan clan = clanPlayer.getClan();
        if (clan == null)
        {
            //If not part of a clan, set colored name and do no more
            setListName(player, "");
            return;
        }

        final String tag = ("\u00A77" + clan.getColorTag());

        //Feature: set prefix in tablist
        //compatible with other prefix/suffix plugins since we just set PlayerListName
        setListName(player, tag);

        //Feature: set prefix in nameplate
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                Team team = sb.getTeam(player.getName());
                if (team == null || !player.isOnline())
                    return;
                //Feature: color nameplate name
                //Get displayName color (player can change color via /nick)
                //Temporarily removed since some colors are hard to see
//                String color = "";
//                char[] charName = player.getDisplayName().toCharArray();
//                if (charName[2] == '\u00A7') //If they have a staff prefix
//                    color = String.valueOf(charName[3]);
//                else
//                    color = String.valueOf(charName[1]);

                team.setPrefix(tag + " \u00A7f");
            }
        }, 40L); //Ensure healthbar made the team
    }



}
