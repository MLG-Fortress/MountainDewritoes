package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Commands.SetExpFix;
import me.robomwm.usefulutil.UsefulUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 8/14/2017.
 *
 * @author RoboMWM
 */
public class LevelingProgression implements Listener
{
	Economy economy;
	
    public LevelingProgression(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.economy = economy;
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

        event.setExpLevelCost(0);
    }
    @EventHandler(ignoreCancelled = true)
    private void enchantingIsNo(InventoryOpenEvent event)
    {
        if (event.getInventory().getType() != InventoryType.ENCHANTING)
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void levelChangeEvent(PlayerExpChangeEvent event)
    {
        //TODO: track exp (so players don't lose any)
        if (event.getSource() == null) return;
        Player player = event.getPlayer();

        int lvl = player.getLevel();
		int xp = 0;
		if(lvl <= 16) xp = 2 * lvl + 7;
		else if(lvl >= 17 && lvl <= 31) xp = 5 * lvl - 38;
		else if(lvl >= 32) xp = 9 * lvl - 158; 

        if (player.getExp() + Double.parseDouble(args[0]) >= xp) //change how it checks for lvl up cos it triggersed when the player dont have the xp to lvl up
        {
			double money = 0;
			//the amount of money is rewarded is based on how much xp it takes to lvl up and level the player is on
			if(lvl <= 30) money = xp * 2.5;
			else if (lvl >= 31 && lvl <= 50) money = xp * 3.75;
			else if (lvl >= 51 && lvl <= 75) money = xp * 4.5;
			else if (lvl >= 76 && lvl <= 110) money = xp * 6;
			else money = xp * 7.5;
			eco.depositPlayer(player,money);
			player.sendActionBar(ChatColor.GREEN + "You gained " + eco.format(money) + " for leveling up to level " + (player.getLevel() + 1));
        }
    }
}
