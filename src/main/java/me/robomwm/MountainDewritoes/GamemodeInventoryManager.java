package me.robomwm.MountainDewritoes;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by RoboMWM on 9/24/2016.
 */
public class GamemodeInventoryManager implements Listener
{
    MountainDewritoes instance;
    public GamemodeInventoryManager(MountainDewritoes mountainDewritoes)
    {
        this.instance = mountainDewritoes;
    }
    @EventHandler(priority = EventPriority.LOWEST)
    void playerOpenEnderChest(InventoryOpenEvent event)
    {
        Player player = (Player)event.getPlayer();
        if (!checkPlayer(player))
            return;

        if (event.getInventory().getType() == InventoryType.ENDER_CHEST)
            event.setCancelled(true);

        //Deny all inventory access (except if they have the "yesok" permission)
        if (event.getInventory().getType() != InventoryType.CRAFTING && !player.hasPermission("yesok"))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onChangeGamemode(PlayerGameModeChangeEvent event)
    {
        checkAndClearPlayerInventory(event.getPlayer());
        if (event.getNewGameMode() == GameMode.CREATIVE) //to creative
        {
            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent addtemp webuilder 1h");
        }
        else if (event.getPlayer().getGameMode() == GameMode.CREATIVE) //from creative
        {
            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent removetemp webuilder");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onQuit(PlayerQuitEvent event)
    {
        if (checkAndClearPlayerInventory(event.getPlayer()))
            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent removetemp webuilder");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerDropItem(PlayerDropItemEvent event)
    {
        if (checkPlayer(event.getPlayer()))
        {
            event.setCancelled(true);
            event.getPlayer().getInventory().setItemInMainHand(null);
        }
    }

    /**
     * Clears player's inventory only if they are in creative mode
     * @param player
     * @return whether the player's inventory was cleared
     */
    boolean checkAndClearPlayerInventory(Player player)
    {
        if (checkPlayer(player))
        {
            player.getInventory().clear();
            return true;
        }
        return false;
    }

    /**
     * Checks if player is in creative or opped
     * could be extended to permission checks or ignored worlds, hence the
     * @param player
     * @return true if player is in creative and doesn't have permissions to be exempt
     */
    boolean checkPlayer(Player player)
    {
        return !player.isOp() && player.getGameMode() == GameMode.CREATIVE;
    }
}
