package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
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
        if (entity.getKiller() == null || entity.getKiller().getType() != EntityType.PLAYER)
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
        else if (random.nextInt(100) == 1)
        {
            //Prepare a new health canister
            ItemStack healthCanister = new ItemStack(Material.POTION);
            PotionMeta potionMeta = (PotionMeta)healthCanister.getItemMeta();
            potionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
            potionMeta.setDisplayName(ChatColor.RED + "Health Canister");
            List<String> lore = new ArrayList<>();
            lore.add("Permanently increases");
            lore.add("your max health.");
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
        //Why aren't we checking the name? Because it's likely we'll change it... soooon
        List<String> lore = potionMeta.getLore();
        if (!lore.get(0).equals("Permanently increases") || !lore.get(1).equals("your max health."))
            return;
        Player player = event.getPlayer();
        if (player.getMaxHealth() >= 180D)
        {
            player.sendMessage(ChatColor.RED + "You have reached the maximum health of 90 hearts!");
            return;
        }
        else
        {
            player.setMaxHealth(player.getMaxHealth() + 2D);
            player.playSound(player.getLocation(), "fortress.healthcanister", 3000000f, 1.0f);
        }
    }

    /**
     * Set new player's health to 13 hearts
     */
    @EventHandler
    void onNewJoin(PlayerJoinEvent event)
    {
        if (event.getPlayer().getMaxHealth() < 50D)
            event.getPlayer().setMaxHealth(50D);
        event.getPlayer().setMaximumAir(3600);
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
            if (event.getPlayer().getHealth() == event.getPlayer().getMaxHealth())
                return;
            event.getPlayer().addPotionEffect(PotionEffectType.HEAL.createEffect(1, 0));
            event.getPlayer().playSound(event.getPlayer().getLocation(), "fortress.healthheart", 3000000f, 1f);
            event.getItem().remove();
        }
    }
}
