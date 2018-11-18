package me.robomwm.MountainDewritoes;

import com.robomwm.usefulutil.UsefulUtil;
import me.robomwm.MountainDewritoes.Events.MonsterTargetPlayerEvent;
import me.robomwm.MountainDewritoes.Events.TransactionEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 2/13/2017.
 * @author RoboMWM
 * Various trackers
 */
public class NSA implements Listener
{
    private static MountainDewritoes instance;

    private static Map<Player, List<Transaction>> transactions = new HashMap<>();
    private static Map<OfflinePlayer, Integer> midairMap = new HashMap<>();
    private static Map<Player, Location> lastLocation = new HashMap<>();
    private static Map<Player, Set<String>> tempMetadata = new ConcurrentHashMap<>(); //thread-safe????????

    NSA(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        instance.registerListener(this);

        new BukkitRunnable()
        {
            Scoreboard sb = mountainDewritoes.getServer().getScoreboardManager().getMainScoreboard();
            @Override
            public void run()
            {
                for (Player player : mountainDewritoes.getServer().getOnlinePlayers())
                    scoreboardSynchronizer(sb, player.getScoreboard());
            }
        }.runTaskTimer(mountainDewritoes, 20L, 1L);
    }

    private void scoreboardSynchronizer(Scoreboard source, Scoreboard target)
    {
        if (source == target)
            return;

        //sync teams
        for (Team sTeam : source.getTeams())
        {
            Team tTeam = target.getTeam(sTeam.getName());
            if (tTeam == null)
                tTeam = target.registerNewTeam(sTeam.getName());

            tTeam.setPrefix(sTeam.getPrefix());
            tTeam.setSuffix(sTeam.getSuffix());
            tTeam.setColor(sTeam.getColor());
            for (String entry : sTeam.getEntries())
                tTeam.addEntry(entry);
            //Currently unused
//            tTeam.setDisplayName(sTeam.getDisplayName());
//            tTeam.setCanSeeFriendlyInvisibles(sTeam.canSeeFriendlyInvisibles());
//            tTeam.setAllowFriendlyFire(sTeam.allowFriendlyFire());
//            for (Team.Option option : Team.Option.values())
//            {
//                tTeam.setOption(option, sTeam.getOption(option));
//            }
        }

        //sync objectives
        for (Objective sObjective : source.getObjectives())
        {
            Objective tObjective = target.getObjective(sObjective.getName());
            if (tObjective == null)
                tObjective = target.registerNewObjective(sObjective.getName(), sObjective.getCriteria(), sObjective.getDisplayName());
            tObjective.setDisplaySlot(sObjective.getDisplaySlot());
            if (!sObjective.isModifiable())
                continue;

            //sync scores
            for (String entry : source.getEntries())
            {
                Score sScore = sObjective.getScore(entry);
                Score tScore = tObjective.getScore(entry);
                tScore.setScore(sScore.getScore());
            }
        }
    }

    public static void removeScoreboard(Player player, Scoreboard scoreboard)
    {
        if (player.getScoreboard() == instance.getServer().getScoreboardManager().getMainScoreboard())
            return;
        if (player.getScoreboard() != scoreboard)
            return;
        final Scoreboard oldScoreboard = player.getScoreboard();
        player.setScoreboard(instance.getServer().getScoreboardManager().getMainScoreboard());

        //Idk if unregistering is necessary if we don't hold a reference to the scoreboard anymore...?
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Team team : oldScoreboard.getTeams())
                    team.unregister();
                for (Objective objective : oldScoreboard.getObjectives())
                    objective.unregister();
            }
        }.runTaskAsynchronously(instance);
    }

    static private final String mobTrackingMetadata = "MD_MOBTRACKING";
    static private final String killStreak = "MD_KILLSTREAK";
    static private final String spreePoints = "MD_KILLSTREAKPOINTS";

    @EventHandler
    private void cleanupMetadataOnQuit(PlayerQuitEvent event) //You never know if memory leaks
    {
        cleanup(event.getPlayer());
    }

    public void cleanup(Player player)
    {
        player.removeMetadata(mobTrackingMetadata, instance);
        clearSpreePoints(player);
        lastLocation.remove(player);
        tempMetadata.remove(player);
    }

    public static boolean getTempdata(Player player, String key)
    {
        return tempMetadata.containsKey(player) && tempMetadata.get(player).contains(key);
    }

    public static boolean removeTempdata(Player player, String key)
    {
        boolean existed = getTempdata(player, key);
        if (existed)
            tempMetadata.get(player).remove(key);
        return existed;
    }

    public static boolean setTempdata(Player player, String key) //Also add way to unset when needed
    {
        if (!tempMetadata.containsKey(player))
            tempMetadata.put(player, ConcurrentHashMap.newKeySet());
        return tempMetadata.get(player).add(key);
    }

    public static Location getLastLocation(Player player)
    {
        return lastLocation.get(player);
    }

    //Track last location (for precise "current velocity")
    @EventHandler(ignoreCancelled = true)
    private void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        lastLocation.put(player, event.getFrom());
    }

    public static Map<OfflinePlayer, Integer> getMidairMap()
    {
        return midairMap;
    }

    //Clear mid-air "metadata" when no longer in air.
    @EventHandler(ignoreCancelled = true)
    private void onPlayerLands(PlayerMoveEvent event)
    {
        if (event.getPlayer().isOnGround())
        {
            midairMap.remove(event.getPlayer());
//            Integer integer = midairMap.remove(event.getPlayer());
//            if (integer != null && integer < 0)
//                instance.getServer().getPluginManager().callEvent(new PlayerLandEvent(event.getPlayer(), integer));
        }
    }

    /* # of mobs targeting player tracker */

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.MONITOR) //MUST BE ON MONITOR else any calls to "howmanyaretracking" will remove the newly-added mob, since it is not yet targeting the player at this point lol
    private void onPlayerTargeted(MonsterTargetPlayerEvent event)
    {
        Player player = event.getPlayer();
        Creature entity = event.getBadEntity();

        if (!player.hasMetadata(mobTrackingMetadata))
        {
            Set<Creature> targeters = new HashSet<>();
            targeters.add(entity);
            player.setMetadata(mobTrackingMetadata, new FixedMetadataValue(instance, targeters));
        }
        else
        {
            Set<Creature> targeters = (Set<Creature>)player.getMetadata(mobTrackingMetadata).get(0).value();
            targeters.add(entity);
            player.setMetadata(mobTrackingMetadata, new FixedMetadataValue(instance, targeters));
        }
    }

    /**
     * Also updates the target list
     * @param player
     * @return how many monsters are trying to attack this player
     */
    @SuppressWarnings("unchecked")
    static public int howManyTargetingPlayer(Player player)
    {
        if (!player.hasMetadata(mobTrackingMetadata))
            return 0;

        Set<Creature> trackers = (Set<Creature>)player.getMetadata(mobTrackingMetadata).get(0).value();
        Set<Creature> nonTargeters = new HashSet<>();

        //Remove mobs no longer targeting this player
        for (Creature entity : trackers)
        {
            if (!entity.isValid() || entity.isDead() || entity.getTarget() != player)
                nonTargeters.add(entity);
        }
        trackers.removeAll(nonTargeters);
        player.setMetadata(mobTrackingMetadata, new FixedMetadataValue(instance, trackers));

        return trackers.size();
    }

    /* kill streak tracker */

    @EventHandler
    @SuppressWarnings("unchecked")
    private void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER && !UsefulUtil.isMonster(event.getEntity()))
            return;
        Entity killerEntity = UsefulUtil.getKiller(event);
        if (killerEntity == null || killerEntity.getType() != EntityType.PLAYER)
            return;

        Player player = (Player)killerEntity;

        int points = 1;
        if (event.getEntityType() == EntityType.PLAYER)
            points = 5;

        Queue<BukkitTask> runnables;


        if (!player.hasMetadata(killStreak))
            player.setMetadata(killStreak, new FixedMetadataValue(instance, new ArrayDeque<BukkitRunnable>()));
        if (!player.hasMetadata(spreePoints))
            player.setMetadata(spreePoints, new FixedMetadataValue(instance, 0));

        int currentPoints = player.getMetadata(spreePoints).get(0).asInt();
        player.setMetadata(spreePoints, new FixedMetadataValue(instance, currentPoints + points));
        runnables = (Queue<BukkitTask>)player.getMetadata(killStreak).get(0).value();

        //Remove point after 2 minutes
        final int finalpoints = points;
        runnables.add(new BukkitRunnable()
        {
            @Override
            public void run()
            {
                int currentPoints = player.getMetadata(spreePoints).get(0).asInt();
                player.setMetadata(spreePoints, new FixedMetadataValue(instance, currentPoints - finalpoints));
            }
        }.runTaskLater(instance, 2400L));
    }

    @SuppressWarnings("unchecked")
    private void clearSpreePoints(Player player)
    {
        if (player.hasMetadata(killStreak))
        {
            Queue<BukkitTask> tasksToKill = (Queue<BukkitTask>)player.getMetadata(killStreak).get(0).value();
            for (BukkitTask task : tasksToKill)
                task.cancel();
        }
        player.removeMetadata(killStreak, instance);
        player.removeMetadata(spreePoints, instance);
    }

    //Clear spree points when killed
    @EventHandler
    @SuppressWarnings("unchecked")
    private void onPlayerDeath(PlayerDeathEvent event)
    {
        clearSpreePoints(event.getEntity());
    }

    @SuppressWarnings("unchecked")
    static public int getSpreePoints(Player player)
    {
        if (!player.hasMetadata(spreePoints))
            return 0;
        return player.getMetadata(spreePoints).get(0).asInt();
    }

    public static String getTransactions(Player player)
    {
        if (!transactions.containsKey(player))
            return ChatColor.GRAY + "No transactions occurred recently.";
        StringBuilder listOfTransactions = new StringBuilder("Recent transactions:\n");

        //Only store and display last 10 transactions
        while (transactions.get(player).size() > 10)
        {
            transactions.get(player).remove(0);
        }

        for (Transaction transaction : transactions.get(player))
        {
            String prefix = ChatColor.GREEN + "+";
            if (transaction.getAmount() < 0)
                prefix = ChatColor.RED.toString();
            listOfTransactions.append(prefix + instance.getEconomy().format(transaction.getAmount())
                    + " " + ChatColor.GRAY + UsefulUtil.formatTime(UsefulUtil.getEpoch() - transaction.getSeconds()) + " ago");
            listOfTransactions.append("\n");
        }
        return listOfTransactions.toString();
    }

    @EventHandler
    private void onTransaction(TransactionEvent event)
    {
        addTransaction(event.getPlayer(), event.getAmount());
    }

    private static void addTransaction(Player player, double change)
    {
        if (!transactions.containsKey(player))
            transactions.put(player, new ArrayList<>());
        transactions.get(player).add(new Transaction(change));
    }

    public static String getRandomString(String... strings)
    {
        return strings[ThreadLocalRandom.current().nextInt(strings.length)];
    }
}

class Transaction
{
    private long time;
    private double amount;

    Transaction(double amount)
    {
        this.time = System.currentTimeMillis();
        this.amount = amount;
    }

    public double getAmount()
    {
        return amount;
    }

    public long getSeconds()
    {
        return time / 1000;
    }
}
