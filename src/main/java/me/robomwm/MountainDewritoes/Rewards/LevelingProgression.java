package me.robomwm.MountainDewritoes.Rewards;

import me.robomwm.MountainDewritoes.Commands.SetExpFix;
import me.robomwm.usefulutil.UsefulUtil;
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
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private Map<Player, Integer> playerLevel = new HashMap<>();

    public LevelingProgression(JavaPlugin plugin, LodsOfEmone lodsOfEmone)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.lodsOfEmone = lodsOfEmone;
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
        playerLevel.put(event.getPlayer(), event.getPlayer().getLevel());
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
        //int initialCost = anvilInventory.getRepairCost();
        anvilInventory.setRepairCost(0);
    }

    @EventHandler(ignoreCancelled = true)
    private void levelChangeEvent(PlayerExpChangeEvent event)
    {
        //TODO: track exp (so players don't lose any)
        if (event.getSource() == null)
            return;
        Player player = event.getPlayer();

        final int finalNextLevel = player.getLevel() + 1;

        new BukkitRunnable()
        {
            int nextLevel = finalNextLevel;
            @Override
            public void run()
            {
                playerLevel.put(player, player.getLevel());

                while (nextLevel <= playerLevel.get(player))
                {
                    lodsOfEmone.rewardPlayer(player, nextLevel, RewardType.XP_LEVELUP);
                    nextLevel++;
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

        //If nothing in results, first check if it's because of a magicloot enchantment book
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
        {
            List<ItemStack> contents = Arrays.asList(event.getInventory().getContents());
            ItemStack enchantBook = null;
            ItemStack itemToEnchant = null;
            for (ItemStack itemStack : event.getInventory().getContents())
            {
                Material type = itemStack.getType();
                if (type.isBlock())
                    return;
                if (type == Material.ENCHANTED_BOOK)
                    enchantBook = itemStack;
                else
                    itemToEnchant = itemStack;
            }

            if (itemToEnchant == null || enchantBook == null)
                return;

            Map<Enchantment, Integer> enchantments = enchantBook.getEnchantments();
            for (Enchantment enchantment : enchantments.keySet())
            {
                itemToEnchant.addUnsafeEnchantment(enchantment, enchantments.get(enchantment));
            }
            ItemMeta itemMeta = itemToEnchant.getItemMeta();
            itemMeta.setLore(enchantBook.getItemMeta().getLore());
            itemToEnchant.setItemMeta(itemMeta);
            event.setCursor(itemToEnchant);
            event.getClickedInventory().clear();
            return;
        }

        event.setCursor(event.getCurrentItem());
        event.getClickedInventory().clear();
    }
}
