package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Robo on 2/16/2016.
 */
public class ChatListener implements Listener
{
    private Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
    BukkitScheduler scheduler = Bukkit.getScheduler();
    public Main instance;
    ConcurrentHashMap<String, int[]> messageScrolling = new ConcurrentHashMap<String, int[]>();

    public ChatListener(Main main)
    {
        instance = main;
    }

    /*
    Message "Bubbles"
    I'd rather use the objective field since I have a slightly higher character limit,

    but a) not sure if possible to show different objective displayNames per-player,
    and b) weighting in what I use, I'd get a net of 4 more characters.
    Probably would be better to use holograms, but most players stare at the
    chat window anyways...

    TODO: Handle /me command
    */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.getRecipients().size() < 2)
            return; //ignore if they're the only one on or softmuted

        final Player player = event.getPlayer();

        //Is a prior message bubble being displayed? If so, remove it
        if (messageScrolling.containsKey(player.getName()))
        {
            final int [] tasksToRemove = messageScrolling.get(player.getName());
            for (int i = 0; i < tasksToRemove.length; i++)
                scheduler.cancelTask(tasksToRemove[i]);
            messageScrolling.remove(player.getName());
        }

        String mess = event.getMessage();

        //Feature: Message "scrolling"
        if (mess.length() > 14)
        {
            int speed = (100 - mess.length()) / 15;
            if (speed < 1)
                speed = 1;
            //14 spaces for ending scroll
            mess += "              ";
            //For storing in hashmap
            int[] tasks = new int[mess.length() - 15];
            String lastMessage = "";
            int maxTime = 0;
            final String firstMess = mess.substring(0, 13);

            //Display first part of message
            scheduler.scheduleSyncDelayedTask(instance, new Runnable()
            {
                public void run()
                {
                    //TODO: Figure out how to cancel tasks if team ever equals null
                    Team team = sb.getTeam(player.getName());
                    if (team == null)
                        return;
                    team.setSuffix(": " + firstMess);
                }
            });

            //Then print rest of message
            for (int i = 1; i < (mess.length() - 14); i++)
            {
                final String message = mess.substring(i, i + 13);

                //Store messages in int array
                tasks[i - 1] = scheduler.scheduleSyncDelayedTask(instance, new Runnable()
                {
                    public void run()
                    {
                        Team team = sb.getTeam(player.getName());
                        if (team == null)
                            return;
                        team.setSuffix(": " + message);
                    }
                }, (60 + (speed * (i + 1))));
                lastMessage = message;
                maxTime = (60 + (speed * (i + 1)));
            }

            //Store int array in hashmap
            messageScrolling.put(player.getName(), tasks);

            final String message = lastMessage;

            scheduler.scheduleSyncDelayedTask(instance, new Runnable()
            {
                public void run()
                {

                    Team team = sb.getTeam(player.getName());
                    if ((team == null) || (player == null))
                    {
                        messageScrolling.remove(player.getName());
                        return;
                    }
                    if (team.getSuffix().equals(": " + message))
                    {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "healthbar reloadplayer " + player.getName());
                    }
                    messageScrolling.remove(player.getName());
                }
            }, maxTime + 1);

            return;
        }

        //if not greater than 14 characters...
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
        }, 180L); //Display for 9 seconds
    }
}
