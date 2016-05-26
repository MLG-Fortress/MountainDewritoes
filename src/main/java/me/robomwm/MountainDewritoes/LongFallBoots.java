package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robo on 5/25/2016.
 * This class is exactly what you think it is
 */
public class LongFallBoots implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerFallDamageWearingLongFallBoots(EntityDamageEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity entity = (LivingEntity)event.getEntity();
        if (entity.getEquipment().getBoots().getType() == Material.IRON_BOOTS)
        {
            entity.getWorld().playSound(entity.getLocation(), "fortress.longfallboots", 1.0f, 1.0f);
            event.setCancelled(true);
        }
    }

    /** (Currently disabled)
     * (Replaced with custom fastcraftplus recipe)
     * When player clicks some iron boots
     * @param event
     */
    /*@EventHandler(ignoreCancelled = true)
    void onPlayerSomehowAcquireSomeBoots(InventoryClickEvent event)
    {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != Material.IRON_BOOTS)
            item = event.getCursor();
        if (item == null || item.getType() != Material.IRON_BOOTS)
            return;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta.hasLore())
            return;
        itemMeta.setDisplayName("Long fall boots");
        List<String> lore = new ArrayList<String>();
        lore.add("You know what?");
        lore.add("Go ahead and jump.");
        lore.add("You've got braces");
        lore.add("on your legs.");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
    }
    */
    @EventHandler
}
