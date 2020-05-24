package me.robomwm.MountainDewritoes;

import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    //DataStore ds;
    ClanManager clanManager;
    Set<Pattern> filterThingy = new HashSet<>();
    List<String> replacements = new ArrayList<>();


    public ChatListener(MountainDewritoes mountainDewritoes, ClanManager clanManager)
    {
        this.instance = mountainDewritoes;
        //GriefPrevention gp = (GriefPrevention)instance.getServer().getPluginManager().getPlugin("GriefPrevention");
        //this.ds = gp.dataStore;
        this.clanManager = clanManager;
        filterThingy.add(Pattern.compile("(?i)\\bn[^a](gg|99)+(a|er|uh)"));
        filterThingy.add(Pattern.compile("(?i)\\bfag+(s)?\\b|fag+.t|gay"));
        filterThingy.add(Pattern.compile("(?i)(i hate|fuck)+ this server|this server is (shit|crap)+|server sucks"));
        filterThingy.add(Pattern.compile("(?i)\\bass\\b"));
        filterThingy.add(Pattern.compile("(?i)\\bcum\\b"));
        filterThingy.add(Pattern.compile("(?i)f+u+c+k+|f+u+k+|f+v+c+k+|f+u+q+|f+u+c+"));
        filterThingy.add(Pattern.compile("(?i)cunt|whore|fag|slut|queer|bit?ch|bi(c|s)h|bastard|damn|damm|danm|\\bcrap|shit|pussy"));
        filterThingy.add(Pattern.compile("(?i)\\bd\\s*i\\s*c?\\s*k\\b|\\bp\\s*e\\s*n(\\s|\\.)*i\\s*s\\b"));
        filterThingy.add(Pattern.compile("(?i)\\bb\\s*o\\s*o\\s*b\\b|\\bb\\s*r\\s*e\\s*a\\s*s\\s*t(\\s*s)?\\b|\\st\\s*i\\s*t(\\s*s|\\s*t\\s*y|\\s*t\\s*i\\s*e\\s*s)?\\b"));
        filterThingy.add(Pattern.compile("(?i)\\bn\\s*i\\s*g\\s*(g\\s*)?(a|a\\s*h|e\\s*r)?\\b"));
        filterThingy.add(Pattern.compile("(?i)\\bl\\s*e(\\s*s|\\s*z)\\s*b(\\s*o|\\s*i\\s*a\\s*n)?\\b|\\bd\\s*y\\s*k\\s*e\\b"));
        replacements.add("wut");
        replacements.add("cool");
        replacements.add("mlg");
        replacements.add("oops");
        replacements.add("oh");
        replacements.add("meme");
        replacements.add("nice");
        replacements.add("");
    }

    /**
    Message "Bubbles"
    <s>I'd rather use the objective field since I have a slightly higher character limit,

    but a) not sure if possible to show different objective displayNames per-player,
    and b) weighting in what I use, I'd get a net of 4 more characters.
    Probably would be better to use holograms, but most players stare at the
    chat window anyways...</s> Not anymore with 1.13!
    */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.getRecipients().size() < 2)
            return; //ignore if they're the only one on

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
        if (mess.length() > 62)
        {
            int speed = (256 - mess.length()) / 63;
            if (speed < 1)
                speed = 1;
            //62 spaces for ending scroll
            mess += "                                                              ";
            //For storing in hashmap
            int[] tasks = new int[mess.length() - 63];
            String lastMessage = "";
            int maxTime = 0;
            final String firstMess = mess.substring(0, 61);

            //Display first part of message
            scheduler.scheduleSyncDelayedTask(instance, new Runnable()
            {
                public void run()
                {
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
                }, (100 + (speed * (i + 1))));
                lastMessage = message;
                maxTime = (100 + (speed * (i + 1)));
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

        //if not greater than 62 characters...
        final String message = mess;
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
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
        }, 200L); //Display for 10 seconds
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
        if (event.getRecipients().size() < instance.getServer().getOnlinePlayers().size()) //|| ds.isSoftMuted(event.getPlayer().getUniqueId()))
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
                    {
                        if (player.hasMetadata("MD_WARNED"))
                            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + player.getName() + " parent set scrub");
                        else
                        {
                            player.setMetadata("MD_WARNED", new FixedMetadataValue(instance, true));
                            instance.getTitleManager().timedActionBar(player, 10, "Avoid profanity! Try gud memes instead!", 100);
                        }
                    }
                }
            }.runTask(instance);
        }

    }

    private Set<AsyncPlayerChatEvent> softmutedChats = new HashSet<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerChatCheckSoftmute(AsyncPlayerChatEvent event)
    {
        if (event.getRecipients().size() < instance.getServer().getOnlinePlayers().size()) // || ds.isSoftMuted(event.getPlayer().getUniqueId()))
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
            log("Canceled: " + event.getPlayer().getName() + ": " + event.getMessage());
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
