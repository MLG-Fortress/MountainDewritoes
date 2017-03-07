package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Events.MonsterTargetPlayerEvent;
import me.robomwm.usefulutil.UsefulUtil;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    @EventHandler
    private void cleanupMetadataOnQuit(PlayerQuitEvent event) //You never know if memory leaks
    {
        Player player = event.getPlayer();
        player.removeMetadata(mobTrackingMetadata, instance);
        player.removeMetadata(killStreak, instance);
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
    private void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity().getKiller() == null)
            return;
        if (event.getEntityType() != EntityType.PLAYER && !UsefulUtil.isMonster(event.getEntity()))
            return;

        Player player = event.getEntity().getKiller();
        int points = 1;
        if (event.getEntityType() == EntityType.PLAYER)
            points = 5;

        if (!player.hasMetadata(killStreak))
        {
            player.setMetadata(killStreak, new FixedMetadataValue(instance, points));
        }
        else
        {
            points += player.getMetadata(killStreak).get(0).asInt();
            player.setMetadata(killStreak, new FixedMetadataValue(instance, points));
        }

        //Remove points after 2 minutes
        final int finalPoints = points;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                int oldPoints = player.getMetadata(killStreak).get(0).asInt();
                player.setMetadata(killStreak, new FixedMetadataValue(instance, oldPoints - finalPoints));
            }
        }.runTaskLater(instance, 2400L);
    }

    static public int getSpreePoints(Player player)
    {
        if (!player.hasMetadata(mobTrackingMetadata))
            return 0;
        else
            return player.getMetadata(killStreak).get(0).asInt();
    }


}
