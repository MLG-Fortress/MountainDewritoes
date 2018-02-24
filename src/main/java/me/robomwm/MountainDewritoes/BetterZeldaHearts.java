package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.items.CustomItems;
import me.robomwm.usefulutil.SetExpFix;
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
    private CustomItems customItems;
    private MountainDewritoes instance;
    private Economy economy;

    public BetterZeldaHearts(MountainDewritoes plugin, Economy economy)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.economy = economy;

        ItemStack heart = new ItemStack(Material.INK_SACK);
        heart.setDurability((short)1);
        ItemMeta heartMeta = heart.getItemMeta();
        heartMeta.setDisplayName("healthHeart");
        heart.setItemMeta(heartMeta);
        customItems.registerItem(heart,"healthHeart");

        ItemStack healthCanister = new ItemStack(Material.POTION);
        PotionMeta potionMeta = (PotionMeta)healthCanister.getItemMeta();
        potionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
        potionMeta.setDisplayName(ChatColor.RED + "Health Canister");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.BLACK + "MLGID:1");
        lore.add("Increases ur maximum swegginess");
        potionMeta.setLore(lore);
        healthCanister.setItemMeta(potionMeta);
        customItems.registerItem(healthCanister, "healthCanister");

        customItems.registerItem(new ItemStack(Material.GOLD_INGOT), "mobMoney");
    }

    Random random = new Random();
    /**
     * Chance of a "heart canister" dropping upon killing a hostile mob
     * and other stuff I add in the future like healthHearts
     */
    @EventHandler(priority = EventPriority.LOW)
    void onEntityDeath(EntityDeathEvent event)
    {
        if (!instance.isSurvivalWorld(event.getEntity().getWorld()))
            return;

        if (!UsefulUtil.isMonster(event.getEntity()))
            return;
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() == null)
            return;
        Location location = event.getEntity().getLocation();

        //Should we spawn a heart (heals on pickup)
        if (random.nextInt(3) == 1)
        {
            Item heartItem = location.getWorld().dropItem(location, customItems.getItem("healthHeart"));
            heartItem.setCustomName(ChatColor.RED + "SwagPack");
            heartItem.setCustomNameVisible(true);
            heartItem.setPickupDelay(10);
        }

        //Otherwise, check if we should spawn a health canister
        //Now a fixed value to encourage using health canisters
        else if (random.nextInt(10) == 1)
        {
            //Prepare a new health canister
            event.getDrops().add(customItems.getItem("healthCanister"));
        }

        /*Mob money*/

        if (economy == null)
            return;

        int maxHealth = (int)entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        int moneyToDrop = maxHealth;
        moneyToDrop *= Math.log(entity.getTicksLived());

        if (moneyToDrop > 1)
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
        if (!instance.isSurvivalWorld(location.getWorld()))
            return;

        /*Taxes*/
        if (economy != null)
        {
            double moneyToDrop = Math.round(economy.getBalance(player) * 0.07);

            if (moneyToDrop > 1)
            {
                economy.withdrawPlayer(player, moneyToDrop);
                player.sendMessage(ChatColor.RED + "Death tax: " + economy.format(moneyToDrop));
                dropMobMoney(location, moneyToDrop);
            }
        }
    }

    private void dropMobMoney(Location location, double amount)
    {
        ItemStack money = customItems.getItem("mobMoney");
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

        if (!customItems.isItem("healthCanister", item))
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

    //Set new player's health to 3 hearts
    @EventHandler
    void onNewJoin(PlayerJoinEvent event)
    {
        if (!event.getPlayer().hasPlayedBefore())
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2D);
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
        return item.getType() == Material.INK_SACK && customItems.isItem("healthHeart", item);
    }
    boolean isMobMoney(ItemStack item)
    {
        return item.getType() == Material.GOLD_INGOT && customItems.isItem("mobMoney", item);
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

    //Player loses 1 heart on death (down to minimum currentXPLevel hearts)
    @EventHandler
    void resetHealthOnRespawn(PlayerRespawnEvent event)
    {
        if (instance.isMinigameWorld(event.getRespawnLocation().getWorld()))
            return;

        double maxHealth = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - 2D;

        if (event.getPlayer().getLevel() >= 90) //Cap is 90
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(180D);
        else if (maxHealth < 2D) //Why
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2D);
        else if (maxHealth <= event.getPlayer().getLevel() * 2) //Would fall currentXPLevel
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(event.getPlayer().getLevel() * 2);
        else
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    }
}
