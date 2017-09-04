package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Commands.SetExpFix;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

/**
 * Created by RoboMWM on 9/24/2016.
 */
public class GamemodeInventoryManager implements Listener
{
    MountainDewritoes instance;
    private YamlConfiguration inventorySnapshots;
    private File inventorySnapshotsFile;

    public GamemodeInventoryManager(MountainDewritoes mountainDewritoes)
    {
        this.instance = mountainDewritoes;
        mountainDewritoes.getServer().getPluginManager().registerEvents(this, mountainDewritoes);
        inventorySnapshotsFile = new File(instance.getDataFolder(), "inventorySnapshots.data");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChangeGamemode(PlayerGameModeChangeEvent event)
    {
        if (event.getNewGameMode() == GameMode.CREATIVE) //to creative
        {
            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent addtemp webuilder 1h");
            storeAndClearInventory(event.getPlayer());
        }
        else if (event.getPlayer().getGameMode() == GameMode.CREATIVE) //from creative
        {
            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent removetemp webuilder");
            event.getPlayer().getInventory().clear();
            restoreInventory(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerPreTeleport(PlayerTeleportEvent event)
    {
        World from = event.getFrom().getWorld();
        World to = event.getTo().getWorld();

        //If not traversing from/to a minigame world, or player is (somehow) in creative, no need to do anything
        if (instance.isMinigameWorld(from) == instance.isMinigameWorld(to))
            return;

        if (instance.isMinigameWorld(to))
            storeAndClearInventory(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerPostTeleport(PlayerChangedWorldEvent event)
    {
        World from = event.getFrom();
        World to = event.getPlayer().getWorld();

        //If not traversing from/to a minigame world, or player is (somehow) in creative, no need to do anything
        if (instance.isMinigameWorld(from) == instance.isMinigameWorld(to))
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        if (!instance.isMinigameWorld(to))
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

    private void loadInventorySnapshots()
    {
        if (inventorySnapshots == null)
        {
            if (!inventorySnapshotsFile.exists())
            {
                try
                {
                    inventorySnapshotsFile.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
            }
            inventorySnapshots = YamlConfiguration.loadConfiguration(inventorySnapshotsFile);
        }
    }

    private void saveInventorySnapshots()
    {
        if (inventorySnapshots == null)
            return;
        try
        {
            inventorySnapshots.save(inventorySnapshotsFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private ConfigurationSection getPlayerSnapshotSection(Player player)
    {
        loadInventorySnapshots();
        if (inventorySnapshots.get(player.getUniqueId().toString()) == null)
        {
            instance.getLogger().info("created");
            return inventorySnapshots.createSection(player.getUniqueId().toString());
        }
        instance.getLogger().info("exists");
        return inventorySnapshots.getConfigurationSection(player.getUniqueId().toString());
    }

    private boolean deletePlayerSnapshotSection(Player player)
    {
        if (inventorySnapshots.get(player.getUniqueId().toString()) != null)
        {
            inventorySnapshots.set(player.getUniqueId().toString(), null);
            instance.getLogger().info("deleted(?)");
            saveInventorySnapshots();
            return true;
        }
        instance.getLogger().info("already(?) deleted");
        return false;
    }

    private boolean storeAndClearInventory(Player player)
    {
        if (instance.isMinigameWorld(player.getWorld()) || player.getGameMode() == GameMode.CREATIVE)
            return false;

        player.closeInventory();

        ConfigurationSection snapshotSection = getPlayerSnapshotSection(player);
        if (snapshotSection.getList("items") != null)
            return false;

        snapshotSection.set("items", player.getInventory().getContents()); //ItemStack[]
        snapshotSection.set("armor", player.getInventory().getArmorContents()); //ItemStack[]
        snapshotSection.set("exp", player.getTotalExperience() + 1); //int //For our purposes, totalExperience is ok since experience can't be spent. We add 1 since exp can be more precise than an int...
        snapshotSection.set("health", player.getHealth()); //double
        snapshotSection.set("maxHealth", player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()); //double
        snapshotSection.set("foodLevel", player.getFoodLevel()); //int

        saveInventorySnapshots(); //TODO: schedule in a runnable instead (performance)?

        player.getInventory().clear();

        return true;
    }

    private boolean restoreInventory(Player player)
    {
        player.closeInventory();

        ConfigurationSection snapshotSection = getPlayerSnapshotSection(player);
        if (snapshotSection.getList("items") == null)
            return false;

        player.getInventory().setContents(snapshotSection.getList("items").toArray(new ItemStack[player.getInventory().getContents().length]));
        player.getInventory().setArmorContents(snapshotSection.getList("armor").toArray(new ItemStack[player.getInventory().getArmorContents().length]));
        SetExpFix.setTotalExperience(player, snapshotSection.getInt("exp"));
        player.setHealth(snapshotSection.getDouble("health"));
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(snapshotSection.getDouble("maxHealth"));
        player.setFoodLevel(snapshotSection.getInt("foodLevel"));

        if (snapshotSection.getInt("additionalExp") != 0)
        {
            Bukkit.getPluginManager().callEvent(new PlayerExpChangeEvent(player, snapshotSection.getInt("additionalExp")));
        }

        deletePlayerSnapshotSection(player);

        return true;
    }
}
