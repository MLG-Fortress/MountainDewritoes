package me.robomwm.MountainDewritoes;

import me.robomwm.usefulutil.UsefulUtil;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChangeGamemode(PlayerGameModeChangeEvent event)
    {
        if (event.getNewGameMode() == GameMode.CREATIVE) //to creative
        {
            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent addtemp webuilder 1h");
            saveInventory(event.getPlayer());
        }
        else if (event.getPlayer().getGameMode() == GameMode.CREATIVE) //from creative
        {
            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent removetemp webuilder");
            event.getPlayer().getInventory().clear();
            restoreInventory(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerTeleportsAcrossTimeAndSpace(PlayerChangedWorldEvent event)
    {
        World from = event.getFrom();
        World to = event.getPlayer().getWorld();

        //If not traversing from/to a minigame world, or player is (somehow) in creative, no need to do anything
        if (instance.isMinigameWorld(from) == instance.isMinigameWorld(to))
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        if (instance.isMinigameWorld(to))
            saveInventory(event.getPlayer());
        else
            restoreInventory(event.getPlayer());
    }

    //Recover inventory, if necessary
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onJoin(PlayerJoinEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!instance.isMinigameWorld(event.getPlayer().getWorld()) && event.getPlayer().getGameMode() != GameMode.CREATIVE)
                    restoreInventory(event.getPlayer());
            }
        }.runTask(instance);
    }

    //"Security"

    //Deny opening Ender Chests
    @EventHandler(priority = EventPriority.LOWEST)
    void playerOpenEnderChest(InventoryOpenEvent event)
    {
        Player player = (Player)event.getPlayer();

        //Only if they're in creative and/or in a minigame world
        if (player.getGameMode() != GameMode.CREATIVE && !instance.isMinigameWorld(player.getWorld()))
            return;

        if (event.getInventory().getType() == InventoryType.ENDER_CHEST)
        {
            event.setCancelled(true);
            return;
        }

        //If in creative and not in minigame world (creative implied), also deny all inventory access
        if (!instance.isMinigameWorld(event.getPlayer().getWorld()) && event.getInventory().getType() != InventoryType.CRAFTING)
            event.setCancelled(true);
    }

    //Drop item = delete item
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerDropItem(PlayerDropItemEvent event)
    {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
        {
            event.setCancelled(true);
            event.getPlayer().getInventory().setItemInMainHand(null);
        }
    }

    private boolean saveInventory(Player player)
    {
        if (instance.isMinigameWorld(player.getWorld()))
            return false;
        return UsefulUtil.storeAndClearInventory(player);
    }

    private boolean restoreInventory(Player player)
    {
        return UsefulUtil.restoreInventory(player);
    }
}
