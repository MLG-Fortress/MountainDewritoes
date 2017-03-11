package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Events.MonsterTargetPlayerEvent;
import me.robomwm.usefulutil.UsefulUtil;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Created on 2/13/2017.
 * @author RoboMWM
 * Various trackers
 */
public class NSA implements Listener
{
    MountainDewritoes instance;

    NSA(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        instance.registerListener(this);
    }

    static private final String mobTrackingMetadata = "MD_MOBTRACKING";
    static private final String killStreak = "MD_KILLSTREAK";
    static private final String spreePoints = "MD_KILLSTREAKPOINTS";

    @EventHandler
    private void cleanupMetadataOnQuit(PlayerQuitEvent event) //You never know if memory leaks
    {
        Player player = event.getPlayer();
        player.removeMetadata(mobTrackingMetadata, instance);
        clearSpreePoints(player);
    }

    /* # of mobs targeting player tracker */

    @SuppressWarnings("unchecked")
    @EventHandler //Keeps track of monsters targeting this player
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


}
