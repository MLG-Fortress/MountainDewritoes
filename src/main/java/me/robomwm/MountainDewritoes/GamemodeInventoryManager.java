package me.robomwm.MountainDewritoes;

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
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by RoboMWM on 9/24/2016.
 */
public class GamemodeInventoryManager implements Listener
{
    private MountainDewritoes instance;
    private YamlConfiguration inventorySnapshots;
    private YamlConfiguration experienceSnapshots;
    private File inventorySnapshotsFile;
    private File experienceSnapshotsFile;
    private World MINIGAMES_SPAWN;

    public GamemodeInventoryManager(MountainDewritoes mountainDewritoes)
    {
        this.instance = mountainDewritoes;
        mountainDewritoes.getServer().getPluginManager().registerEvents(this, mountainDewritoes);
        inventorySnapshotsFile = new File(instance.getDataFolder(), "inventorySnapshots.data");
        experienceSnapshotsFile = new File(instance.getDataFolder(), "experienceSnapshots.data");
        MINIGAMES_SPAWN = instance.getServer().getWorld("spawn");
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

    //Save inventory before inter-world teleport
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerPreTeleport(PlayerTeleportEvent event)
    {
        World from = event.getFrom().getWorld();
        World to = event.getTo().getWorld();

        //If teleporting within same world, no need to save
        if (instance.isMinigameWorld(from) == instance.isMinigameWorld(to))
            return;

        if (instance.isMinigameWorld(to))
            storeAndClearInventory(event.getPlayer());
    }

    //Restore inventory after inter-world teleport
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

    //Recover inventory on join (e.g. player was in creative while in a "survival"-classed world)
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

    //Restore and save experience when entering minigames spawn
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerTeleportToMinigamesSpawn(PlayerChangedWorldEvent event)
    {
        if (event.getPlayer().getWorld() != MINIGAMES_SPAWN)
            return;

        //1 tick delay, in case the minigame plugin restores player data after teleporting instead of before.
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (event.getPlayer().getWorld() != MINIGAMES_SPAWN)
                    return;

                //Restore experience, if saved before
                restoreExperience(event.getPlayer());

                saveExperience(event.getPlayer());
            }
        }.runTask(instance);
    }
    @EventHandler
    private void onPlayerJoinInMinigamesSpawn(PlayerJoinEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (event.getPlayer().getWorld() != MINIGAMES_SPAWN)
                    return;

                //Restore experience, if saved before
                restoreExperience(event.getPlayer());

                saveExperience(event.getPlayer());
            }
        }.runTask(instance);
    }

    //Restore experience if teleporting to a survival world from a minigames world
    @EventHandler
    private void onPlayerTeleportRestoreExperience(PlayerChangedWorldEvent event)
    {
        if (!instance.isMinigameWorld(event.getFrom()))
            return;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (instance.isSurvivalWorld(event.getPlayer().getWorld()))
                    restoreExperience(event.getPlayer());
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

        //If in creative (implied if it got to here) and not in minigame world, also deny all inventory access
        if (!instance.isMinigameWorld(event.getPlayer().getWorld()) && event.getInventory().getType() != InventoryType.CRAFTING)
            event.setCancelled(true);
    }

    //Drop item in creative = delete item
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

        if (experienceSnapshotsFile == null)
        {
            if (!experienceSnapshotsFile.exists())
            {
                try
                {
                    experienceSnapshotsFile.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
            }
            experienceSnapshots = YamlConfiguration.loadConfiguration(experienceSnapshotsFile);
        }
    }

    private void saveInventorySnapshots()
    {
        try
        {
            if (inventorySnapshots != null)
                inventorySnapshots.save(inventorySnapshotsFile);
            if (experienceSnapshots != null)
                experienceSnapshots.save(experienceSnapshotsFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private ConfigurationSection getPlayerInventorySnapshotSection(Player player)
    {
        loadInventorySnapshots();
        if (inventorySnapshots.get(player.getUniqueId().toString()) == null)
            return inventorySnapshots.createSection(player.getUniqueId().toString());
        return inventorySnapshots.getConfigurationSection(player.getUniqueId().toString());
    }

    private boolean deletePlayerInventorySnapshotSection(Player player)
    {
        if (inventorySnapshots.get(player.getUniqueId().toString()) != null)
        {
            inventorySnapshots.set(player.getUniqueId().toString(), null);
            saveInventorySnapshots();
            return true;
        }
        return false;
    }

    private ConfigurationSection getPlayerExperienceSnapshotSection(Player player)
    {
        loadInventorySnapshots();
        if (experienceSnapshots.get(player.getUniqueId().toString()) == null)
            return experienceSnapshots.createSection(player.getUniqueId().toString());
        return experienceSnapshots.getConfigurationSection(player.getUniqueId().toString());
    }

    private boolean deletePlayerExperienceSnapshotSection(Player player)
    {
        if (experienceSnapshots.get(player.getUniqueId().toString()) != null)
        {
            experienceSnapshots.set(player.getUniqueId().toString(), null);
            saveInventorySnapshots();
            return true;
        }
        return false;
    }

    private boolean storeAndClearInventory(Player player)
    {
        //Can happen if player was teleporting to a different world while in creative mode
        if (player.getGameMode() == GameMode.CREATIVE)
            return false;

        //No need to save if the player is in a minigame world
        if (instance.isMinigameWorld(player.getWorld()))
            return false;

        //Avoid having to deal with players holding stuff with their mouse cursor and other inventory whatnot (though idk if a tick has to elapse for this to actually work, server side).
        player.closeInventory();

        ConfigurationSection snapshotSection = getPlayerInventorySnapshotSection(player);
        if (snapshotSection.getList("items") != null)
            return false;

        snapshotSection.set("items", Arrays.asList(player.getInventory().getContents())); //List<ItemStack> - arrays are stored and read as ArrayLists, so doing this to maintain consistency. Can optimize later if needed.
        snapshotSection.set("armor", Arrays.asList(player.getInventory().getArmorContents())); //List<ItemStack>
        //snapshotSection.set("expLevel", player.getLevel()); //int
        //snapshotSection.set("expProgress", player.getExp()); //float
        snapshotSection.set("maxHealth", player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()); //double
        snapshotSection.set("health", player.getHealth()); //double
        snapshotSection.set("foodLevel", player.getFoodLevel()); //int
        snapshotSection.set("activePotionEffects", new ArrayList<>(player.getActivePotionEffects())); //List<PotionEffect> - no idea what collection type CB uses, but I'm pretty sure it'll also be stored and read as ArrayList.

        saveInventorySnapshots(); //TODO: schedule in a runnable instead (performance)?

        player.getInventory().clear();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20D);
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());

        return true;
    }

    private boolean restoreInventory(Player player)
    {
        player.closeInventory();

        ConfigurationSection snapshotSection = getPlayerInventorySnapshotSection(player);
        if (snapshotSection.getList("items") == null)
            return false;

        try
        {
            player.getInventory().setContents(snapshotSection.getList("items").toArray(new ItemStack[player.getInventory().getContents().length]));
            player.getInventory().setArmorContents(snapshotSection.getList("armor").toArray(new ItemStack[player.getInventory().getArmorContents().length]));
            //player.setLevel(snapshotSection.getInt("expLevel"));
            //player.setExp((float)snapshotSection.get("expProgress"));
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(snapshotSection.getDouble("maxHealth"));
            player.setHealth(snapshotSection.getDouble("health"));
            player.setFoodLevel(snapshotSection.getInt("foodLevel"));
            for (PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());
            player.addPotionEffects((List<PotionEffect>)snapshotSection.getList("activePotionEffects"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        deletePlayerInventorySnapshotSection(player);

        return true;
    }

    private boolean saveExperience(Player player)
    {
        //Can happen if player was teleporting to a different world while in creative mode
        if (player.getGameMode() == GameMode.CREATIVE)
            return false;

        //No need to save if the player is in a minigame world
        if (instance.isMinigameWorld(player.getWorld()))
            return false;

        ConfigurationSection snapshotSection = getPlayerExperienceSnapshotSection(player);
        if (snapshotSection.getList("expLevel") != null)
            return false;

        snapshotSection.set("expLevel", player.getLevel()); //int
        snapshotSection.set("expProgress", player.getExp()); //float

        return true;
    }

    private boolean restoreExperience(Player player)
    {
        ConfigurationSection snapshotSection = getPlayerExperienceSnapshotSection(player);
        if (snapshotSection.getList("expLevel") == null)
            return false;

        try
        {
            player.setLevel(snapshotSection.getInt("expLevel"));
            player.setExp((float)snapshotSection.get("expProgress"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        deletePlayerExperienceSnapshotSection(player);

        return true;
    }
}
