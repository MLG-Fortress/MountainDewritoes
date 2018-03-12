package me.robomwm.MountainDewritoes.Rewards;

import com.robomwm.grandioseapi.player.GrandPlayer;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created on 8/14/2017.
 *
 * @author RoboMWM
 */
public class LevelingProgression implements Listener
{
    private LodsOfEmone lodsOfEmone;
    private MountainDewritoes plugin;
    private Set<Player> playersToCheck = new HashSet<>();

    public LevelingProgression(MountainDewritoes plugin, LodsOfEmone lodsOfEmone)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.lodsOfEmone = lodsOfEmone;
    }

    //How many times did the player level up (compared to when we last checked)?
    private int getLevelUpAmount(Player player)
    {
        GrandPlayer grandPlayer = plugin.getGrandioseAPI().getGrandPlayerManager().getGrandPlayer(player);
        Integer lastRecordedLevel = grandPlayer.getYaml().getInt("expLevel");
        if (lastRecordedLevel == 0)
        {
            plugin.getLogger().severe(player.getName() + " had no prior expLevel!");
            return 0;
        }
        final int timesToLevelUp = player.getLevel() - lastRecordedLevel; //Current level - last seen level
        if (timesToLevelUp > 0)
        {
            grandPlayer.getYaml().set("expLevel", player.getLevel());
            return timesToLevelUp;
        }
        return 0;
    }

    @EventHandler(ignoreCancelled = true)
    private void levelChangeEvent(PlayerExpChangeEvent event) //We only want to fire on naturally-collected XP.
    {
        if (!plugin.isSurvivalWorld(event.getPlayer().getWorld()))
            return;

        Player player = event.getPlayer();

        //Wait a tick so player.getLevel() updates
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                int timesToLevelUp = getLevelUpAmount(player);
                while (timesToLevelUp > 0)
                {
                    timesToLevelUp--;
                    lodsOfEmone.rewardPlayer(player, player.getLevel() - timesToLevelUp, RewardType.XP_LEVELUP);
                }
            }
        }.runTask(plugin);

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

    //Save player's experience level, in case they somehow "spend" it.
    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        if (!plugin.isSurvivalWorld(event.getPlayer().getWorld()))
        {
            playersToCheck.add(event.getPlayer());
            return;
        }
        registerPlayerLevel(event.getPlayer());
    }
    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event)
    {
        playersToCheck.remove(event.getPlayer());
    }
    @EventHandler
    private void onPlayerChangesWorld(PlayerChangedWorldEvent event)
    {
        if (!plugin.isSurvivalWorld(event.getPlayer().getWorld()))
            return;
        if (playersToCheck.contains(event.getPlayer()))
            registerPlayerLevel(event.getPlayer());
    }
    private void registerPlayerLevel(Player player)
    {
        GrandPlayer grandPlayer = plugin.getGrandioseAPI().getGrandPlayerManager().getGrandPlayer(player);
        Integer lastRecordedLevel = grandPlayer.getYaml().getInt("expLevel");
        if (lastRecordedLevel == 0)
        {
            grandPlayer.getYaml().set("expLevel", player.getLevel());
            grandPlayer.saveYaml();
        }
        playersToCheck.remove(player);
    }

    /*
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

        //Slimefun guide, usually
        if (enchantments.isEmpty())
            return null;

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
