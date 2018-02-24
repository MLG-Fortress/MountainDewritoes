package me.robomwm.MountainDewritoes.armor;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 1/28/2018.
 *
 * @author RoboMWM
 */
public class OldFood implements Listener
{
    private JavaPlugin instance;

    OldFood(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        instance = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerPreEat(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)
            return;
        Player player = event.getPlayer();
        if (player.getFoodLevel() < 20)
            return;
        if (player.getHealth() >= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
            return;
        if (getFood(event.getItem()) > 0)
            player.setFoodLevel(19);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerEats(PlayerItemConsumeEvent event)
    {
        Player player = event.getPlayer();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double health = player.getHealth();
        double healthToAdd;

        if (health >= maxHealth)
        {
            player.sendActionBar(player.getDisplayName() + " says I'm stuffed.");
            event.setCancelled(true);
            return;
        }

        healthToAdd = getFood(event.getItem());

        if (health + healthToAdd >= maxHealth)
            healthToAdd = maxHealth - health;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(event.getPlayer(), healthToAdd, EntityRegainHealthEvent.RegainReason.EATING);
        instance.getServer().getPluginManager().callEvent(healthEvent);
        if (event.isCancelled())
            event.setCancelled(true);
        else
            player.setHealth(health + healthToAdd);

    }

    private double getFood(ItemStack itemStack)
    {
        if (itemStack == null)
            return 0;
        switch(itemStack.getType())
        {
            case POTION:
                return 0;
            default:
                return 1D;
        }
    }
}
