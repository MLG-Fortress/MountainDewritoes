package me.robomwm.MountainDewritoes.armor;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

/**
 * Created on 9/2/2018
 *
 * Chestplate:
 * Leggings: Mega knockback
 * Boots: Roll (on ground only)
 *
 * @author RoboMWM
 */
public class ChainmailArmor implements Listener
{
    private ArmorAugmentation armorAugmentation;

    ChainmailArmor(MountainDewritoes plugin, ArmorAugmentation armorAugmentation)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.armorAugmentation = armorAugmentation;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();

        if (!event.isSneaking() || !event.getPlayer().isOnGround())
            return;
        if (!armorAugmentation.isEquipped(player, Material.GOLDEN_BOOTS))
            return;
        Vector vector = player.getLocation().toVector();

        player.setVelocity(vector.subtract(NSA.getLastLocation(player).toVector().setY(0D))); //TODO: adjust

        Integer jump = NSA.getMidairMap().get(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;
        if (event.getEntityType() == EntityType.ARMOR_STAND || !(event.getEntity() instanceof LivingEntity))
            return;
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getDamager();
        LivingEntity damagee = (LivingEntity)event.getEntity();

        if (!player.isSprinting() || player.getFoodLevel() <= 6 || !armorAugmentation.isEquipped(player, Material.CHAINMAIL_LEGGINGS))
            return;

        float power = player.getFoodLevel() / 20f;
        power = power * 3f;

        damagee.setVelocity(player.getLocation().toVector().subtract(damagee.getLocation().toVector()).normalize().multiply(power));

        player.setFoodLevel(0);
    }
}
