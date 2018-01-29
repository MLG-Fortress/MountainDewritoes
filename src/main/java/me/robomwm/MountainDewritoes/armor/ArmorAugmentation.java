package me.robomwm.MountainDewritoes.armor;

import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 1/3/2018.
 *
 * Handles the armor abilities, controls energy bar
 *
 * @author RoboMWM
 */
public class ArmorAugmentation implements Listener
{
    private JavaPlugin instance;

    public ArmorAugmentation(JavaPlugin plugin)
    {
        instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(new GoldArmor(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new OldFood(instance), plugin);
    }

    //Reduce messy if/else
    public boolean isEquipped(Player player, Material armorToMatch)
    {
        ItemStack equippedArmor = null;
        switch (armorToMatch)
        {
            case GOLD_BOOTS:
            case IRON_BOOTS:
            case DIAMOND_BOOTS:
            case CHAINMAIL_BOOTS:
                equippedArmor = player.getInventory().getBoots();
                break;
        }
        return equippedArmor != null && equippedArmor.getType() == armorToMatch;
    }

    //Misc. gameplay changes to accomodate

    //There's no such thing as starving
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onHungerDamage(EntityDamageEvent event)
    {
        if (event.getCause() == EntityDamageEvent.DamageCause.STARVATION)
            event.setCancelled(true);
    }

    //Since when does having a full stomach make your wounds heal faster?
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onStomachHeal(EntityRegainHealthEvent event)
    {
        Player player = (Player)event.getEntity();

        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED)
        {
            event.setCancelled(true);
            player.setSaturation(0f);
        }
    }

    //That's an energy bar, not a hunger bar.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onGettingHungry(FoodLevelChangeEvent event)
    {
        event.setCancelled(true);
    }

    //Sprinting takes energy
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onSprint(PlayerToggleSprintEvent event)
    {
        if (!event.isSprinting())
            return;

        Player player = event.getPlayer();
        if (player.getFoodLevel() < 1)
            return;

        player.setFoodLevel(player.getFoodLevel() - 1);

        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                if (player.isSprinting())
                    player.setFoodLevel(player.getFoodLevel() - 1);
                else
                    this.cancel();
            }
        }.runTaskTimer(instance, 20L, 20L);
    }

    //Refill energy bar gradually
    private void ATPgeneration()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : instance.getServer().getOnlinePlayers())
                {
                    if (player.getFoodLevel() >= 20)
                        return;
                    player.setFoodLevel(player.getFoodLevel() + 1);
                }
            }
        }.runTaskTimer(instance, 40L, 40L);
    }
}
