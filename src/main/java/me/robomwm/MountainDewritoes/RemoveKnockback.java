package me.robomwm.MountainDewritoes;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

/**
 * Created by RoboMWM on 9/10/2016.
 * It is also possible to listen to the PlayerVelocityEvent, but this will only apply to players,
 * leading to inconsistent behavior of weapons on players vs. mobs.
 * Thus, only removing knockback on arrows (I can't even remove it from other projectiles >_>)
 */
public class RemoveKnockback implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onDamageDisableKnockback(EntityDamageByEntityEvent event)
    {
        Entity damager = event.getDamager();
        if (!(damager instanceof Projectile))
            return; //Can only set knockback on arrows
        ((Arrow) damager).setKnockbackStrength(0);
    }

    //Haha no I'm not using NMS thanks
//    private static final UUID movementSpeedUID = UUID.fromString("206a89dc-ae78-4c4d-b42c-3b31db3f5a7c");
//
//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void onEntitySpawn(CreatureSpawnEvent event){
//        LivingEntity entity = event.getEntity();
//
//        if (entity.getType() == EntityType.ZOMBIE){
//            EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
//            AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.d);
//
//            AttributeModifier modifier = new AttributeModifier(movementSpeedUID, "<plugin_name> movement speed multiplier", 1.1d, 1);
//
//            attributes.b(modifier);
//            attributes.a(modifier);
//        }
//    }

}
