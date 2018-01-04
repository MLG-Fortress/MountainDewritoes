package me.robomwm.MountainDewritoes.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 1/3/2018.
 *
 * @author RoboMWM
 */
public class ArmorAugmentation implements Listener
{
    JavaPlugin instance;

    public ArmorAugmentation(JavaPlugin plugin)
    {
        instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(new GoldArmor(this), plugin);
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

    //Check to determine whether to activate one-use sneak ability
    public boolean shiftAbility(Player player)
    {
        if (player.isOnGround())
            return false;
        if (player.hasMetadata("MD_USED_SHIFT"))
            return false;
        player.setMetadata("MD_USED_SHIFT", new FixedMetadataValue(instance, true));
        return true;
    }
    @EventHandler
    private void onLand(PlayerMoveEvent event)
    {
        if (!event.getPlayer().isOnGround())
            return;
        if (event.getPlayer().hasMetadata("MD_USED_SHIFT"))
            event.getPlayer().removeMetadata("MD_USED_SHIFT", instance);
    }
}
