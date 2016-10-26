package me.robomwm.MountainDewritoes;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * Created by RoboMWM on 10/25/2016.
 * "Removes" default knockback from melee and projectile damage
 * Also reduces noDamageTicks for attacks which we "remove" knockback from
 */
public class NoKnockback implements Listener
{
    MountainDewritoes instance;
    Map<Entity, BukkitTask> ignoreDamageEvent = new HashMap<>();
    Map<Player, Boolean> cancelKnockback = new HashMap<>();
    public NoKnockback(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    void ignoreDamageEventForThisTick(Entity entity)
    {
        cancelKnockback.remove(entity);
        //Cancel existing "remove player from ignoreDamageEvent"
        BukkitTask task = ignoreDamageEvent.get(entity);
        if (task != null)
            task.cancel();
        ignoreDamageEvent.put(entity,
                new BukkitRunnable()
                {
                    public void run()
                    {
                        ignoreDamageEvent.remove(entity);
                    }
                }.runTaskLater(instance, 1L));
    }

    void letPlayerVelocityEventHandleIt(Player player, boolean isMelee)
    {
        cancelKnockback.put(player, isMelee);
        //To avoid complicating matters, we'll assume a PlayerVelocityEvent is always called when a player is attacked
        //Otherwise we'd have to do what we did in ignoreDamageEventForThisTick
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR) //We aren't modifying this event, only responding to it
    void onEntityGetsHurt(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        Entity damager = event.getDamager();
        LivingEntity target = (LivingEntity)event.getEntity();

        if (ignoreDamageEvent.containsKey(damager))
            return;

        //If the entity is likely to be dead anyways, don't bother trying to deal with knockback.
        if (event.getFinalDamage() > target.getHealth())
            return;

        //We only care about melee and projectile damage - the rest provide negligible or intended knockback
        switch (event.getCause())
        {
            case ENTITY_ATTACK:
            case PROJECTILE:
            case THORNS:
                break;
            default:
                ignoreDamageEventForThisTick(damager);
                return;
        }

        target.setNoDamageTicks(5);

        //"Special" case for players (Cancel PlayerVelocityEvent instead to avoid momentary "slowdown" when getting hit).
        if (target.getType() == EntityType.PLAYER)
        {
            Player player = (Player)target;
            boolean isMelee = false;
            //Generally indicates a melee attack. Not always. See https://gist.github.com/RoboMWM/8dd1db97726544805ab0c8353ce31975
            if (event.getCause() == DamageCause.ENTITY_ATTACK && damager.getType() == EntityType.PLAYER)
                isMelee = true;
            letPlayerVelocityEventHandleIt(player, isMelee);
            return;
        }

        new BukkitRunnable()
        {
            public void run()
            {
                target.setVelocity(new Vector(0, 0, 0));
            }
        }.runTaskLater(instance, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST) //We still want to remove entries in cancelKnockback map even if event is canceled
    void cancelKnockbackVelocity(PlayerVelocityEvent event)
    {
        Player player = event.getPlayer();
        if (!cancelKnockback.containsKey(player))
            return;
        boolean isMelee = cancelKnockback.remove(player);
        if (isMelee)
            cancelKnockback.put(player, false);
        event.setCancelled(true);
    }
}