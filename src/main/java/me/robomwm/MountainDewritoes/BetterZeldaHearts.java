package me.robomwm.MountainDewritoes;

import me.robomwm.usefulutil.UsefulUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 5/25/2016.
 * ZeldaHearts without data storage or dependencies!
 * And more configurable, and OSS!
 */
public class BetterZeldaHearts implements Listener
{
    MountainDewritoes instance;
    Economy economy;

    public BetterZeldaHearts(MountainDewritoes plugin, Economy economy)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.economy = economy;
    }

    Random random = new Random();
    /**
     * Chance of a "heart canister" dropping upon killing a hostile mob
     * and other stuff I add in the future like healthHearts
     */
    @EventHandler(priority = EventPriority.LOW)
    void onEntityDeath(EntityDeathEvent event)
    {
        if (!UsefulUtil.isMonster(event.getEntity()))
            return;
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() == null)
            return;
        Location location = event.getEntity().getLocation();

        //Should we spawn a heart (heals on pickup)
        if (random.nextInt(3) == 1)
        {
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

        /*Mob Money*/
        /*Mob money*/

        if (economy == null)
            return;

        int maxHealth = (int)entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        int moneyToDrop = ThreadLocalRandom.current().nextInt(maxHealth, maxHealth * maxHealth);
        moneyToDrop *= Math.log(entity.getTicksLived() * entity.getTicksLived());

        if (moneyToDrop > 0)
        {
            dropMobMoney(location, moneyToDrop);
        }
    }

    //yes
    @EventHandler
    private void onPlayerKilled(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        Location location = player.getLocation();
        if (!instance.isSurvivalWorld(location.getWorld()) && !instance.isSafeWorld(location.getWorld()))

        /*Mob money*/
        if (economy != null)
        {
            double moneyToDrop = Math.round(economy.getBalance(player) * 0.07);

            if (moneyToDrop > 0)
            {
                economy.withdrawPlayer(player, moneyToDrop);
                player.sendMessage(ChatColor.RED + "Death tax: " + economy.format(moneyToDrop));
                dropMobMoney(location, moneyToDrop);
            }
        }
    }

    private void dropMobMoney(Location location, double amount)
    {
        ItemStack money = new ItemStack(Material.GOLD_INGOT);
        ItemMeta moneyMeta = money.getItemMeta();
        moneyMeta.setDisplayName(ChatColor.YELLOW + economy.format(amount));
        moneyMeta.setLore(Collections.singletonList(String.valueOf(amount)));
        money.setItemMeta(moneyMeta);
        Item moneyItem = location.getWorld().dropItem(location, money);
        moneyItem.setCustomName(moneyMeta.getDisplayName());
        moneyItem.setCustomNameVisible(true);
        moneyItem.setPickupDelay(10);
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
            maxHealth.setBaseValue(maxHealth.getValue() + 2D);
            player.playSound(player.getLocation(), "fortress.healthcanister", 3000000f, 1.0f);
            EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(player, maxHealth.getValue() - player.getHealth(), EntityRegainHealthEvent.RegainReason.CUSTOM);
            Bukkit.getPluginManager().callEvent(healthEvent);
            if (!healthEvent.isCancelled())
                player.setHealth(maxHealth.getValue()); //Fully heal player
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
        if (isHealthHeart(event.getItem().getItemStack()) || isMobMoney(event.getItem().getItemStack()))
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
    void onHealthHeartAttemptedPickup(PlayerAttemptPickupItemEvent event)
    {
        //It still doesn't fly at the player anyways. Either it's because we deleted the item, or the event is canceled. Not sure which, but idc right now.
        //Check if player's inventory is full (since this event fires before PlayerPickupItemEvent)
//        if (event.getPlayer().getInventory().firstEmpty() != -1)
//            return;

        ItemStack itemStack = event.getItem().getItemStack();

        if (isHealthHeart(itemStack))
        {
            if (!healPlayer(event.getPlayer())) //Do nothing if player is already at full health
                return;
            event.getItem().remove();
        }

        else if (isMobMoney(itemStack))
        {
            double money = Double.valueOf(itemStack.getItemMeta().getLore().get(0));
            economy.depositPlayer(event.getPlayer(), money);
            event.getPlayer().playSound(event.getPlayer().getLocation(), "fortress.mobmoney", SoundCategory.PLAYERS, 3000000f, 1.0f);
            event.getItem().remove();
        }
    }

    boolean isHealthHeart(ItemStack item)
    {
        return item.getType() == Material.INK_SACK && item.hasItemMeta() && (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals("healthHeart"));
    }
    boolean isMobMoney(ItemStack item)
    {
        return item.getType() == Material.GOLD_INGOT && item.hasItemMeta() && (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().startsWith(ChatColor.YELLOW + economy.format(0).substring(0,1)));
    }

    boolean healPlayer(Player player)
    {
        if (player.getHealth() >= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
                && player.getFireTicks() <= 0
                && player.getPotionEffect(PotionEffectType.POISON) == null
                && player.getPotionEffect(PotionEffectType.WITHER) == null)
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
