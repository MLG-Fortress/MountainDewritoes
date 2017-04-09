package me.robomwm.MountainDewritoes;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 3/31/2017.
 *
 * Effectively removes the noDamageTicks feature, with exception for damage done by blocks
 *
 * Some things to note: https://bukkit.org/threads/whats-up-with-setnodamageticks.141901/#post-1638021
 *
 * @author RoboMWM
 */
public class RevisedNoDamageImmunity implements Listener
{
    MountainDewritoes instance;

    public RevisedNoDamageImmunity(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        instance.registerListener(this);
    }

    Map<LivingEntity, Double> blockDamagedEntities = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerJoinRemoveDamageTicks(PlayerJoinEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                event.getPlayer().setMaximumNoDamageTicks(3);
            }
        }.runTaskLater(instance, 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onCreatureSpawnRemoveDamageTicks(CreatureSpawnEvent event)
    {
        event.getEntity().setMaximumNoDamageTicks(3);
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockDamage(EntityDamageByBlockEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        event.setCancelled(!addBlockDamagedEntity((LivingEntity)event.getEntity(), event.getFinalDamage()));
    }

    /**
     * Determines and adds an entity to "environmental damage" immunity
     * Q: Why do we not track and delete old scheduled tasks?
     * A: The only time the old task would be obsolete is if the player was damaged by something that deals more damage than the original
     * @param entity
     * @param damage
     * @return Whether the player should be damaged or not
     */
    private boolean addBlockDamagedEntity(LivingEntity entity, double damage)
    {
        //Don't damage player if new damage is less or equal to old damage
        if (blockDamagedEntities.containsKey(entity) && damage <= blockDamagedEntities.get(entity))
            return false;

        blockDamagedEntities.put(entity, damage);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (blockDamagedEntities.containsKey(entity) && blockDamagedEntities.get(entity) == damage)
                    blockDamagedEntities.remove(entity);
            }
        }.runTaskLater(instance, 6L);
        return true;
    }
}
