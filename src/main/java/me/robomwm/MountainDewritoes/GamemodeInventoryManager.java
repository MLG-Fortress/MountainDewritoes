package me.robomwm.MountainDewritoes;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Created by RoboMWM on 9/24/2016.
 */
public class GamemodeInventoryManager implements Listener
{
    @EventHandler
    void playerChangeWorld(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        if (player.isOp())
            return;
        if (player.getGameMode() == GameMode.CREATIVE)
            player.getInventory().clear();
    }

    @EventHandler
    void playerOpenEnderChest(InventoryOpenEvent event)
    {
        Player player = (Player)event.getPlayer();
        if (player.isOp())
            return;
        if (player.getGameMode() != GameMode.CREATIVE)
            return;
        if (event.getInventory().getType() == InventoryType.ENDER_CHEST)
        {
            event.setCancelled(true);
        }
    }
}
