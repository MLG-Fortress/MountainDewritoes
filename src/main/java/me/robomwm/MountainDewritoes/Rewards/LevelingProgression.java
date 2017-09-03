package me.robomwm.MountainDewritoes.Rewards;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 8/14/2017.
 *
 * @author RoboMWM
 */
public class LevelingProgression implements Listener
{
    private LodsOfEmone lodsOfEmone;
    private JavaPlugin instance;
    private Map<Player, Integer> recordedPlayerLevel = new HashMap<>();

    public LevelingProgression(JavaPlugin plugin, LodsOfEmone lodsOfEmone)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.lodsOfEmone = lodsOfEmone;
    }

    //How many times did the player level up (compared to when we last checked)?
    private int getLevelUpAmount(Player player)
    {
        final int timesToLevelUp = recordedPlayerLevel.get(player) - player.getLevel();
        if (timesToLevelUp > 0)
        {
            recordedPlayerLevel.put(player, player.getLevel());
            return timesToLevelUp;
        }
        return 0;
    }

    /**
     * Deny use of enchantment table. Make anything else require no levels to "enchant."
     */
    @EventHandler(ignoreCancelled = true)
    private void enchantingIsNo(EnchantItemEvent event)
    {
        if (event.getExpLevelCost() <= 0)
            return;

        if (event.getEnchantBlock().getType() == Material.ENCHANTMENT_TABLE)
        {
            event.setCancelled(true);
            event.getEnchanter().closeInventory();
            return;
        }

        //Never gets here since this is only called for enchantment tables at the moment

        event.setExpLevelCost(0);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        recordedPlayerLevel.put(event.getPlayer(), event.getPlayer().getLevel());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        recordedPlayerLevel.remove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    private void enchantingIsNo(InventoryOpenEvent event)
    {
        if (event.getInventory().getType() != InventoryType.ENCHANTING)
            return;
        event.setCancelled(true);
    }
    @EventHandler
    private void anvil(PrepareAnvilEvent event)
    {
        AnvilInventory anvilInventory = event.getInventory();
        ItemStack magicLootItem = magicLootEnchant(event.getInventory().getContents());
        anvilInventory.setRepairCost(0);
        if (magicLootItem != null)
            event.setResult(magicLootItem);
    }

    @EventHandler(ignoreCancelled = true)
    private void levelChangeEvent(PlayerExpChangeEvent event)
    {
        //TODO: track exp (so players don't lose any)

        Player player = event.getPlayer();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                int timesToLevelUp = getLevelUpAmount(player);
                while (timesToLevelUp > 0)
                {
                    lodsOfEmone.rewardPlayer(player, player.getLevel() - timesToLevelUp, RewardType.XP_LEVELUP);
                    timesToLevelUp--;
                }
            }
        }.runTask(instance);

        //[11:42:40] RoboMWM: so it seems that xp orbs can be more precise values than ints
        //[11:43:14] RoboMWM: yet we're only given an int from the API? Did xp used to be an int, and now it stays that way for compatibility?
        //[11:43:49] RoboMWM: https://i.imgur.com/QxAMJ4H.png
//        int nextLevelExp = SetExpFix.getExpToLevel(nextLevel) - player.getTotalExperience(); //Remaining experience required to level up
//        int expAmount = event.getAmount(); //Current amount of exp from the orb
//
//        if (expAmount < nextLevelExp)
//            return;
//
//        //If the xp amount is enough to level up,
//        while (expAmount >= nextLevelExp)
//        {
//            lodsOfEmone.rewardPlayer(player, nextLevel, RewardType.XP_LEVELUP); //Reward player for leveling up
//            expAmount -= nextLevelExp; //Subtract amount of xp remaining from the orb (we "spent" it on leveling up)
//            nextLevelExp += SetExpFix.getExpAtLevel(++nextLevel); //Calculate the xp needed to level up to the next level (that's a lot of levels)
//        }
    }

    //Allow anvil use (setting enchantment cost to 0 causes the client not to not try to enchant anything
    @EventHandler(ignoreCancelled = true)
    private void forceEnchant(InventoryClickEvent event)
    {
        if (event.getSlotType() != InventoryType.SlotType.RESULT || event.getClickedInventory().getType() != InventoryType.ANVIL)
            return;

        //don't accidentally delete a held item
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR)
            return;

        //first check if it's because of a magicloot enchantment book (only some or no enchants will be applied since they aren't "safe")
//        ItemStack magicLootEnchant = magicLootEnchant(event.getClickedInventory().getContents());
//        if (magicLootEnchant != null)
//        {
//            event.setCursor(magicLootEnchant);
//            event.getClickedInventory().clear();
//            return;
//        }

        //nothing in results
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        event.setCursor(event.getCurrentItem());
        event.getClickedInventory().clear();
    }


    private ItemStack magicLootEnchant(ItemStack[] contents)
    {
        ItemStack enchantBook = null;
        ItemStack itemToEnchant = null;
        for (ItemStack itemStack : contents)
        {
            if (itemStack == null)
                continue;
            Material type = itemStack.getType();
            if (type.isBlock())
                return null;
            if (type == Material.ENCHANTED_BOOK)
                enchantBook = itemStack.clone();
            else
                itemToEnchant = itemStack.clone();
        }

        if (itemToEnchant == null || enchantBook == null)
            return null;

        //Not a MagicLoot enchant book
        if (!enchantBook.getItemMeta().hasDisplayName())
            return null;

        Map<Enchantment, Integer> enchantments = enchantBook.getEnchantments();
        for (Enchantment enchantment : enchantments.keySet())
        {
            itemToEnchant.addUnsafeEnchantment(enchantment, enchantments.get(enchantment));
        }
        ItemMeta itemMeta = itemToEnchant.getItemMeta();
        itemMeta.setLore(enchantBook.getItemMeta().getLore());
        itemMeta.setDisplayName(enchantBook.getItemMeta().getDisplayName());
        itemToEnchant.setItemMeta(itemMeta);
        return itemToEnchant;
    }
    //https://bukkit.org/threads/how-to-put-unsafe-enchantments-to-result-item-in-anvil.412472/
    //Apparently I should've used EnchantmentStorageMeta, but uh I guess I don't really need to...
    //EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta) e.getInventory().getItem(1).getItemMeta();
}
