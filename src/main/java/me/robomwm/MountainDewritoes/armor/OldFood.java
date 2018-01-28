package me.robomwm.MountainDewritoes.armor;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 1/28/2018.
 *
 * @author RoboMWM
 */
public class OldFood implements Listener
{
    JavaPlugin instance;

    OldFood(JavaPlugin plugin)
    {
        instance = plugin;
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
            return;
        }

        switch(event.getItem().getType())
        {
            //TODO: fill
            case POTION:
                return;
            default:
                healthToAdd = 1D;
        }

        if (health + healthToAdd >= maxHealth)
            healthToAdd = maxHealth - health;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(event.getPlayer(), healthToAdd, EntityRegainHealthEvent.RegainReason.EATING);
        instance.getServer().getPluginManager().callEvent(healthEvent);
        if (event.isCancelled())
            event.setCancelled(true);
        else
            player.setHealth(health + healthToAdd);

    }
}
