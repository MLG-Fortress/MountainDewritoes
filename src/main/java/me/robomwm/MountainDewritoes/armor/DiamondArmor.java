package me.robomwm.MountainDewritoes.armor;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

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
    public void onVelocityApplied(PlayerVelocityEvent event)
    {
        Player player = event.getPlayer();
        if (!player.isSneaking() || player.isOnGround())
            return;
        if (!armorAugmentation.isEquipped(player, Material.DIAMOND_BOOTS))
            return;
        event.setCancelled(true);
        //TODO: sound, effect
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLand(EntityDamageEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL || event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getEntity();

        if (armorAugmentation.isEquipped(player, Material.DIAMOND_BOOTS))
            return;

        Vector ministun = new Vector(0, 0.1, 0);
        for (Entity entity : player.getNearbyEntities(3, 1, 3))
            entity.setVelocity(ministun);
        //TODO short potion effect
        //TODO sound, effect
    }
}
