package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
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
    /**
     * Chance of a "heart canister" dropping upon killing a hostile mob
     */
    @EventHandler(priority = EventPriority.LOW)
    void onEntityDeath(EntityDeathEvent event)
    {
        if (!(event.getEntity() instanceof Monster))
            return;
        Monster entity = (Monster)event.getEntity();
        if (entity.getKiller() == null || entity.getKiller().getType() != EntityType.PLAYER)
            return;

        //Decrease probability as player obtains higher maxHealth.
        //TODO: Make exponential?
        Player player = entity.getKiller();
        if (new Random().nextInt((int)player.getMaxHealth() * 2) != 1)
            return;

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
            //Poot sound effect here
        }
    }

    /**
     * Set new player's health to 3 hearts
     */
    @EventHandler
    void onNewJoin(PlayerJoinEvent event)
    {
        if (!event.getPlayer().hasPlayedBefore())
            event.getPlayer().setMaxHealth(6D);
    }
}
