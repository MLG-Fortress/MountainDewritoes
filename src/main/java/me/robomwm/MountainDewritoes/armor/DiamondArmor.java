package me.robomwm.MountainDewritoes.armor;

import me.robomwm.MountainDewritoes.Events.PlayerLandEvent;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2/21/2018.
 *
 * @author RoboMWM
 */
public class DiamondArmor implements Listener
{
    private ArmorAugmentation armorAugmentation;

    DiamondArmor(JavaPlugin plugin, ArmorAugmentation armorAugmentation)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.armorAugmentation = armorAugmentation;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event)
    {
//        Player player = event.getPlayer();
//
//        if (!event.isSneaking() || event.getPlayer().isOnGround())
//            return;
//        if (!armorAugmentation.isEquipped(player, Material.DIAMOND_BOOTS))
//            return;
//
//        if (!NSA.getMidairMap().containsKey(player))
//        {
//            NSA.getMidairMap().put(player, -1);
//            Vector vector = player.getLocation().getDirection();
//            if (vector.getY() > 0)
//            {
//                player.setVelocity(vector.setY(0));
//            }
//        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityDamaged(EntityDamageByEntityEvent event)
    {
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;
        Player damager = (Player)event.getDamager();
        if (!damager.isSprinting() || !armorAugmentation.isEquipped(damager, Material.DIAMOND_LEGGINGS))
            return;

        event.getEntity().setVelocity(event.getEntity().getLocation().toVector().subtract(damager.getLocation().toVector()).normalize().setY(0.02));
    }

    @EventHandler(ignoreCancelled = true)
    public void onLand(EntityDamageEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL || event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getEntity();

        if (armorAugmentation.isEquipped(player, Material.DIAMOND_CHESTPLATE))
            return;

        Vector ministun = new Vector(0, 0.01, 0);
        for (Entity entity : player.getNearbyEntities(3, 1, 3))
            entity.setVelocity(ministun);
    }
}
