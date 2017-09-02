package me.robomwm.MountainDewritoes.Rewards;

import me.robomwm.MountainDewritoes.Commands.SetExpFix;
import me.robomwm.usefulutil.UsefulUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 8/14/2017.
 *
 * @author RoboMWM
 */
public class LevelingProgression implements Listener
{
    LodsOfEmone lodsOfEmone;
    JavaPlugin instance;

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
                while (nextLevel <= player.getLevel())
                {
                    lodsOfEmone.rewardPlayer(player, nextLevel, RewardType.XP_LEVELUP);
                    nextLevel++;
                }
            }
        }.runTask(instance);
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
}
