package me.robomwm.MountainDewritoes;

import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
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

    //Do not cancel velocity events from bending abilities
    //A number of abilities set custom velocities, and it uses Entity#damage to damage the entity
    //And since Entity#damage(damage, sourceEntity) uses ENTITY_ATTACK as its cause, we can't tell a plugin called this in the damage event.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onDamagedByBending(AbilityDamageEntityEvent event)
    {
        ignoreDamageEventForThisTick(event.getSource());
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
            //We need to handle melee attacks from players a tad differently, since canceling the first velocity event causes a second one to be fired.
            boolean isMelee = false;
            //Generally indicates a melee attack. If not, ignoreDamageEventForThisTick() should have been called earlier in the tick, provided plugins fire a custom event before calling Entity#damage() See https://gist.github.com/RoboMWM/8dd1db97726544805ab0c8353ce31975
            if (event.getCause() == DamageCause.ENTITY_ATTACK && damager.getType() == EntityType.PLAYER)
                isMelee = true;
            letPlayerVelocityEventHandleIt(player, isMelee);
            return;
        }
        //Otherwise, set mob's velocity to 0 on the next tick
        //Note: there is a very small, visible "jump" the entity makes before its velocity is set to 0 on next tick
        //Is there a reasonable way to set velocity after the MC server processes the damage event besides waiting for the next tick?
        else
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    target.setVelocity(new Vector(0, 0, 0));
                }
            }.runTask(instance);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) //We still want to remove entries in cancelKnockback map even if event is canceled
    void cancelKnockbackVelocity(PlayerVelocityEvent event)
    {
        Player player = event.getPlayer();
        if (!cancelKnockback.containsKey(player))
            return;

        boolean isMelee = cancelKnockback.remove(player);
        if (isMelee && !event.isCancelled())
            cancelKnockback.put(player, false); //See https://gist.github.com/RoboMWM/8dd1db97726544805ab0c8353ce31975
        event.setCancelled(true);
    }
}
