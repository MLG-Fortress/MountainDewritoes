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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
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
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() >= 180D)
        {
            player.sendMessage(ChatColor.RED + "u reached da maximum sweg of 90 swegcaps!");
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

    //Set new player's health to 30 hearts
    @EventHandler
    void onNewJoin(PlayerJoinEvent event)
    {
        if (!event.getPlayer().hasPlayedBefore())
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60D);
        event.getPlayer().setMaximumAir(600);
    }

    //Don't allow mobs to pick this up
    @EventHandler(ignoreCancelled = true)
    void onNonPlayerPickup(EntityPickupItemEvent event)
    {
        if (event.getEntityType() == EntityType.PLAYER) //TODO: is this event fired for players?
            return;
        if (isHealthHeart(event.getItem().getItemStack()))
        {
            event.getItem().setCanMobPickup(false);
            event.setCancelled(true);
        }
    }

    /**
     * Player collecting healthHeart
     * You think up a better internal name for that
     */
    @EventHandler(ignoreCancelled = true)
    void onHealthHeartPickup(PlayerPickupItemEvent event)
    {
        if (isHealthHeart(event.getItem().getItemStack()))
        {
            event.setCancelled(true);
            if (!healPlayer(event.getPlayer())) //Do nothing if player is already at full health
                return;
            event.setFlyAtPlayer(true);
            event.getItem().remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onHealthHeartAttemptedPickup(PlayerAttemptPickupItemEvent event)
    {
        //Check if player's inventory is full (since this event fires before PlayerPickupItemEvent)
        if (event.getPlayer().getInventory().firstEmpty() != -1)
            return;

        //Copy paste above basically :c
        if (isHealthHeart(event.getItem().getItemStack()))
        {
            if (!healPlayer(event.getPlayer())) //Do nothing if player is already at full health
                return;
            event.getItem().remove();
        }
    }

    boolean isHealthHeart(ItemStack item)
    {
        return item.getType() == Material.INK_SACK && item.hasItemMeta() && (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals("healthHeart"));
    }

    boolean healPlayer(Player player)
    {
        if (player.getHealth() == player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()
                && player.getFireTicks() == 0
                && player.getPotionEffect(PotionEffectType.POISON) != null
                && player.getPotionEffect(PotionEffectType.WITHER) != null)
            return false;
        player.addPotionEffect(PotionEffectType.HEAL.createEffect(1, 2));
        player.setFireTicks(0); //Extinguish
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.playSound(player.getLocation(), "fortress.healthheart", 3000000f, 1f);
        return true;
    }

    //Player loses 1/8 of extra health
    @EventHandler
    void resetHealthOnRespawn(PlayerRespawnEvent event)
    {
        if (event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() < 60D)
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60D);
        else
        {
            int extraHearts = (int)event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() - 60;
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60 + (extraHearts - (extraHearts/8)));
            //ensure even value
            double health = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health - (health % 2));
        }
    }
}
