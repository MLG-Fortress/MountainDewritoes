package me.robomwm.MountainDewritoes.armor;

import me.robomwm.MountainDewritoes.Commands.DebugCommand;
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

    //Player cannot eat if their energy bar is full
    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerPreEat(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)
            return;
        Player player = event.getPlayer();
        if (player.getFoodLevel() < 20)
            return;
        if (player.getHealth() >= player.getAttribute(Attribute.MAX_HEALTH).getValue())
            return;
        if (getFood(event.getItem()) > 0) {
            DebugCommand.debug("OldFood preEat: " + player.getName() + " food 20->19 health " + player.getHealth() + " item " + event.getItem().getType());
            player.setFoodLevel(19);
            DebugCommand.debug("OldFood preEat after: food=" + player.getFoodLevel());
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerEats(PlayerItemConsumeEvent event)
    {
        Player player = event.getPlayer();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double health = player.getHealth();
        double healthToAdd;

        healthToAdd = getFood(event.getItem());

        if (healthToAdd == 0)
            return;

        if (health >= maxHealth)
        {
            player.sendActionBar(player.getDisplayName() + " says I'm stuffed.");
            return;
        }

        if (health + healthToAdd >= maxHealth)
            healthToAdd = maxHealth - health;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(event.getPlayer(), healthToAdd, EntityRegainHealthEvent.RegainReason.EATING);
        instance.getServer().getPluginManager().callEvent(healthEvent);
        DebugCommand.debug("OldFood onEat: " + player.getName() + " item " + event.getItem().getType() + " health " + health + "->" + (health + healthToAdd) + " healthEvent cancelled=" + healthEvent.isCancelled() + " consumeCancelled=" + event.isCancelled() + " food=" + player.getFoodLevel() + " handRaised=" + player.isHandRaised());
        if (healthEvent.isCancelled())
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
                return 1D;
            case MILK_BUCKET:
                return 2D;
            default:
                return 6D;
        }
    }
}
