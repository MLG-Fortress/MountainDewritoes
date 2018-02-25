package me.robomwm.MountainDewritoes;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Robo on 2/16/2016.
 */
public class ChatListener implements Listener
{
    private Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
    BukkitScheduler scheduler = Bukkit.getScheduler();
    public MountainDewritoes instance;
    ConcurrentHashMap<String, int[]> messageScrolling = new ConcurrentHashMap<String, int[]>();
    DataStore ds;
    ClanManager clanManager;
    Set<Pattern> filterThingy = new HashSet<>();
    List<String> replacements = new ArrayList<>();


    public ChatListener(MountainDewritoes mountainDewritoes, ClanManager clanManager)
    {
        this.instance = mountainDewritoes;
        GriefPrevention gp = (GriefPrevention)instance.getServer().getPluginManager().getPlugin("GriefPrevention");
        this.ds = gp.dataStore;
        this.clanManager = clanManager;
        filterThingy.add(Pattern.compile("\\bn[^a](gg|99)+(a|er|uh)"));
        filterThingy.add(Pattern.compile("\\bfag+(s)?\\b|fag+.t|gay"));
        filterThingy.add(Pattern.compile("(i hate|fuck)+ this server|this server is (shit|crap)+|server sucks"));
        filterThingy.add(Pattern.compile("\\bass\\b"));
        filterThingy.add(Pattern.compile("\\bcum\\b"));
        filterThingy.add(Pattern.compile("f+u+c+k+|f+u+k+|f+v+c+k+|f+u+q+|f+u+c+"));
        filterThingy.add(Pattern.compile("cunt|whore|fag|slut|queer|bitch|bastard|damn|damm|\\bcrap|shit"));
        filterThingy.add(Pattern.compile("\\bd\\s*i\\s*c?\\s*k\\b|\\bp\\s*e\\s*n(\\s|\\.)*i\\s*s\\b"));
        filterThingy.add(Pattern.compile("\\bb\\s*o\\s*o\\s*b\\b|\\bb\\s*r\\s*e\\s*a\\s*s\\s*t(\\s*s)?\\b|\\st\\s*i\\s*t(\\s*s|\\s*t\\s*y|\\s*t\\s*i\\s*e\\s*s)?\\b"));
        filterThingy.add(Pattern.compile("\\bn\\s*i\\s*g\\s*(g\\s*)?(a|a\\s*h|e\\s*r)?\\b"));
        filterThingy.add(Pattern.compile("\\bl\\s*e(\\s*s|\\s*z)\\s*b?(\\s*o|\\s*i\\s*a\\s*n)?\\b|\\bd\\s*y\\s*k\\s*e\\b"));
        replacements.add("wut");
        replacements.add("cool");
        replacements.add("mlg");
        replacements.add("hmm");
        replacements.add("oh");
        replacements.add("meme");
        replacements.add("nice");
        replacements.add("n0sc0p3");
        replacements.add("w0t");
        replacements.add("");
    }

    /**
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
            return; //ignore if they're the only one on

        //MC 1.11 increased length of chat messages. We aren't even going to try if it's too long.
        if (event.getMessage().length() > 100)
            return;

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

    @EventHandler(priority = EventPriority.LOW)
    void onPlayerMeMessage(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage().toLowerCase();
        if (!message.startsWith("/me "))
            return;

        if (event.isCancelled())
        {
            event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "* " + event.getPlayer().getDisplayName() + ChatColor.DARK_PURPLE + event.getMessage().substring(3));
            return;
        }

        message = message.substring(4);
        boolean filtered = false;
        for (Pattern pattern : filterThingy)
        {
            Matcher matcher = pattern.matcher(message);
            if (matcher.matches())
            {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "* " + event.getPlayer().getDisplayName() + ChatColor.DARK_PURPLE + event.getMessage().substring(3));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onPlayerChatFilter(AsyncPlayerChatEvent event)
    {
        //Employ softmute check, since no need to filter if softmuted
        if (event.getRecipients().size() < instance.getServer().getOnlinePlayers().size() || ds.isSoftMuted(event.getPlayer().getUniqueId()))
            return;

        String message = ChatColor.stripColor(event.getMessage().toLowerCase());
        boolean filtered = false;
        for (Pattern pattern : filterThingy)
        {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find())
            {
                filtered = true;
                message = matcher.replaceAll(replacements.get(ThreadLocalRandom.current().nextInt(replacements.size())));
            }
        }

        if (filtered)
        {
            Player player = event.getPlayer();
            log("Filtered: " + event.getPlayer().getName() + ": " + event.getMessage());
            event.getRecipients().remove(event.getPlayer());
            String name = player.getDisplayName();
            if (clanManager.getClanPlayer(player) != null)
                name = ChatColor.GRAY + clanManager.getClanPlayer(player).getClan().getColorTag() + " " + name;
            event.getPlayer().sendMessage(String.format(event.getFormat(), name, event.getMessage()));
            event.setMessage(message);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (player.hasPermission("chester.log"))
                        instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + player.getName() + " parent set scrub");
                }
            }.runTask(instance);
        }

    }

    private Set<AsyncPlayerChatEvent> softmutedChats = new HashSet<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerChatCheckSoftmute(AsyncPlayerChatEvent event)
    {
        if (event.getRecipients().size() < instance.getServer().getOnlinePlayers().size() || ds.isSoftMuted(event.getPlayer().getUniqueId()))
        {
            event.setCancelled(true);
            softmutedChats.add(event);
            log("softmute: " + event.getPlayer().getName() + ": " + event.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerWasSoftmuted(AsyncPlayerChatEvent event)
    {
        if (softmutedChats.remove(event))
            event.setCancelled(false);
        else if (event.isCancelled())
            log("Canceled: " + event.getMessage());
    }

    void log(String message)
    {
        for (Player player : instance.getServer().getOnlinePlayers())
        {
            if (player.hasPermission("idont.thinkso"))
                player.sendMessage(ChatColor.GRAY + message);
        }
        instance.getServer().getLogger().info(ChatColor.stripColor(message));
    }
}
