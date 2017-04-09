package me.robomwm.MountainDewritoes;

import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * Created by RoboMWM on 10/25/2016.
 *
 * So... attributes are easier to edit than I thought, and therefore this class is largely useless
 *
 * "Removes" default knockback from melee and projectile damage
 */
public class NoKnockback implements Listener
{
    MountainDewritoes instance;
    public NoKnockback(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    //Do not cancel velocity events from bending abilities
    //A number of abilities set custom velocities, and it uses Entity#damage to damage the entity
    //And since Entity#damage(damage, sourceEntity) uses ENTITY_ATTACK as its cause, we can't tell a plugin called this in the damage event.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onDamagedByBending(AbilityDamageEntityEvent event)
    {
        if (event.getEntity() instanceof LivingEntity)
            event.getEntity().setMetadata("MD_DONT_RESISTKB", new FixedMetadataValue(instance, true));
        instance.getServer().broadcastMessage("debug: " + String.valueOf(event.getDamage()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST) //We aren't modifying this event, only responding to it
    void onEntityGetsHurt(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        if (event.getEntity().hasMetadata("MD_DONT_RESISTKB"))
        {
            event.getEntity().removeMetadata("MD_DONT_RESISTKB", instance);
            return;
        }

        Entity damager = event.getDamager();
        LivingEntity target = (LivingEntity)event.getEntity();

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
                return;
        }

        final double oldKnockbackResistanceValue = target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getBaseValue();

        //Temporarily set a high resistance value
        target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(999D);
        //Then remove it after a tick (assuming this fires before the next damage event comes in...)
        new BukkitRunnable()
        {
            public void run()
            {
                target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(oldKnockbackResistanceValue);
            }
        }.runTask(instance);
    }
}
