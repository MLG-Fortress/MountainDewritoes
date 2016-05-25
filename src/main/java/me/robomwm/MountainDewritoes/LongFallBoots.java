package me.robomwm.MountainDewritoes;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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
    void onPlayerFallDamage(EntityDamageEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        Player player = (Player)event.getEntity();
        if (player.getInventory().getBoots().getType() == Material.IRON_BOOTS)
            event.setCancelled(true);
    }

    /**
     * When player clicks some iron boots
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    void onPlayerSomehowAcquireSomeBoots(InventoryClickEvent event)
    {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != Material.IRON_BOOTS)
            return;
        if (item.hasItemMeta())
            return;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta.hasLore() || itemMeta.hasDisplayName())
            return;
        itemMeta.setDisplayName("Long fall boots");
        List<String> lore = new ArrayList<String>();
        lore.add("You know what?");
        lore.add("Go ahead and jump.");
        lore.add("You've got braces");
        lore.add("on your legs.");
        itemMeta.setLore(lore);
        //item.setItemMeta(itemMeta); idk taking shots in dark rn
    }
}
