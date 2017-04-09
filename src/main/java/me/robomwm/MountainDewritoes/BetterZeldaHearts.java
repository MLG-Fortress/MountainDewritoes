package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by RoboMWM on 5/25/2016.
 * ZeldaHearts without data storage or dependencies!
 * And more configurable, and OSS!
 */
public class BetterZeldaHearts implements Listener
{
    Random random = new Random();
    /**
     * Chance of a "heart canister" dropping upon killing a hostile mob
     * and other stuff I add in the future like healthHearts
     */
    @EventHandler(priority = EventPriority.LOW)
    void onEntityDeath(EntityDeathEvent event)
    {
        if (!(event.getEntity() instanceof Monster))
            return;
        Monster entity = (Monster)event.getEntity();
        if (entity.getKiller() == null)
            return;

        //Should we spawn a heart (heals on pickup)
        if (random.nextInt(3) == 1)
        {
            Location location = event.getEntity().getLocation();
            ItemStack heart = new ItemStack(Material.INK_SACK);
            heart.setDurability((short)1);
            ItemMeta heartMeta = heart.getItemMeta();
            heartMeta.setDisplayName("healthHeart");
            heart.setItemMeta(heartMeta);
            Item heartItem = location.getWorld().dropItem(location, heart);
            heartItem.setCustomName(ChatColor.RED + "health");
            heartItem.setCustomNameVisible(true);
            heartItem.setPickupDelay(10);
        }

        //Otherwise, check if we should spawn a health canister
        //Now a fixed value to encourage using health canisters
        else if (random.nextInt(10) == 1)
        {
            //Prepare a new health canister
            ItemStack healthCanister = new ItemStack(Material.POTION);
            PotionMeta potionMeta = (PotionMeta)healthCanister.getItemMeta();
            potionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
            potionMeta.setDisplayName(ChatColor.RED + "Health Canister");
            List<String> lore = new ArrayList<>();
            lore.add("Permanently increases");
            lore.add("your max health");
            lore.add("(until you die).");
            potionMeta.setLore(lore);
            healthCanister.setItemMeta(potionMeta);
            event.getDrops().add(healthCanister);
        }
    }

    /**
     * Player consuming heart canister
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onConsumeHeartCanister(PlayerItemConsumeEvent event)
    {
        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION)
            return;
        PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
        if (potionMeta.getBasePotionData().getType() != PotionType.INSTANT_HEAL)
            return;
        if (!potionMeta.hasLore())
            return;

        List<String> lore = potionMeta.getLore();
        if (!lore.get(0).equals("Permanently increases") || !lore.get(1).startsWith("your max health"))
            return;
        Player player = event.getPlayer();
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() >= 74D)
        {
            player.sendMessage(ChatColor.RED + "u reached da maximum sweg of 37 swegcaps!");
            event.setCancelled(true);
            return;
        }
        else
        {
            AttributeInstance maxHealth = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
            maxHealth.setBaseValue(maxHealth.getBaseValue() + 2D);
            player.playSound(player.getLocation(), "fortress.healthcanister", 3000000f, 1.0f);
            event.setItem(null);
        }
    }

    /**
     * Set new player's health to 13 hearts
     */
    @EventHandler
    void onNewJoin(PlayerJoinEvent event)
    {
        if (!event.getPlayer().hasPlayedBefore())
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(26D);
        event.getPlayer().setMaximumAir(1200);
    }

    /**
     * Player collecting healthHeart
     * You think up a better internal name for that
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onHealthHeartPickup(PlayerPickupItemEvent event)
    {

        if (event.getItem().getItemStack().getType() != Material.INK_SACK)
            return;
        ItemStack item = event.getItem().getItemStack();
        if (!item.hasItemMeta())
            return;
        if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals("healthHeart"))
        {
            event.setCancelled(true);
            if (event.getPlayer().getHealth() == event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue())
                return;
            event.getPlayer().addPotionEffect(PotionEffectType.HEAL.createEffect(1, 2));
            clearBadEffects(event.getPlayer());
            event.getPlayer().playSound(event.getPlayer().getLocation(), "fortress.healthheart", 3000000f, 1f);
            event.getItem().remove();
        }
    }

    void clearBadEffects(Player player)
    {
        player.setFireTicks(0); //Extinguish
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WITHER);
    }

    @EventHandler
    void resetHealthOnRespawn(PlayerRespawnEvent event)
    {
        event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(26D);
    }
}
