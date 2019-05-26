package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Commands.DebugCommand;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
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

    public GamemodeInventoryManager(MountainDewritoes mountainDewritoes)
    {
        this.instance = mountainDewritoes;
        mountainDewritoes.getServer().getPluginManager().registerEvents(this, mountainDewritoes);
        inventorySnapshotsFile = new File(instance.getDataFolder(), "inventorySnapshots.data");
        experienceSnapshotsFile = new File(instance.getDataFolder(), "experienceSnapshots.data");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChangeGamemode(PlayerGameModeChangeEvent event)
    {
        if (event.getPlayer().isOp())
            return;

        if (event.getNewGameMode() == GameMode.CREATIVE) //to creative
        {
            //If player is in a survival world (except prison) and is not op, deny creative gamemode
            if (!event.getPlayer().hasPermission("md.develop") && instance.isSurvivalWorld(event.getPlayer().getWorld()))
            {
                event.setCancelled(true);
                return;
            }

            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent addtemp webuilder 3h");
            storeAndClearInventory(event.getPlayer());
            saveExperience(event.getPlayer());
        }
        else if (event.getPlayer().getGameMode() == GameMode.CREATIVE) //from creative
        {
            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent removetemp webuilder");
            event.getPlayer().setItemOnCursor(null);
            event.getPlayer().closeInventory();
            event.getPlayer().getInventory().clear();
            restoreInventory(event.getPlayer());
            restoreExperience(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onCqrrot(BlockPlaceEvent event)
    {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        if (event.getBlock().getType().getHardness() < 0)
        {
            event.setCancelled(true);
            instance.getLogger().info(event.getPlayer().getName() + " is a haxor with " + event.getBlock().getType().name());
        }
    }

    //Save or restore inventory before inter-world teleport
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerPreTeleport(PlayerTeleportEvent event)
    {
        DebugCommand.debug(event.getPlayer().getHealth());
        //Do nothing if no world change
        if (!changedWorlds(event.getTo().getWorld(), event.getPlayer()))
            return;

        //Save if exiting survival world
        if (!instance.isSurvivalWorld(event.getTo().getWorld()))
        {
            storeAndClearInventory(event.getPlayer());
            saveExperience(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerPostWorldChange(PlayerChangedWorldEvent event)
    {
        restoreInventory(event.getPlayer());
        restoreExperience(event.getPlayer());
    }

    //Recover inventory on join (e.g. player was in creative while in a "survival"-classed world)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onJoin(PlayerJoinEvent event)
    {
        restoreInventory(event.getPlayer());
        restoreExperience(event.getPlayer());
        instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "lp user " + event.getPlayer().getName() + " parent removetemp webuilder");
    }

    //If teleporting within same world/world type and not in creative, no need to save/restore
    private boolean changedWorlds(World world, Player player)
    {
        return instance.isSurvivalWorld(world) != instance.isSurvivalWorld(player.getWorld())
                && player.getGameMode() != GameMode.CREATIVE;
    }


    //"Security"

    //Deny opening Ender Chests
    @EventHandler(priority = EventPriority.LOWEST)
    void playerOpenEnderChest(InventoryOpenEvent event)
    {
        Player player = (Player)event.getPlayer();

        if (event.getPlayer().isOp())
            return;

        //deny opening ender chest while in creative gamemode or non-survival worlds
        if (event.getInventory().getType() == InventoryType.ENDER_CHEST
                && (event.getPlayer().getGameMode() == GameMode.CREATIVE || !instance.isSurvivalWorld(player.getWorld())))
            event.setCancelled(true);

        //deny all inventory access (sans crafting table) if in creative and in a survival world.
        //TODO this logic seems a tad convoluted
        else if (player.getGameMode() == GameMode.CREATIVE
                && instance.isSurvivalWorld(event.getPlayer().getWorld())
                && event.getInventory().getType() != InventoryType.CRAFTING)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onItemSpawn(EntityTeleportEvent event)
    {
        event.setCancelled(instance.isSurvivalWorld(event.getFrom().getWorld()) != instance.isSurvivalWorld(event.getTo().getWorld()));
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onItemFrameBreak(HangingBreakEvent event)
    {
        if (instance.isSurvivalWorld(event.getEntity().getWorld()))
            return;

        switch (event.getCause())
        {
            case PHYSICS:
            case OBSTRUCTION:
                event.setCancelled(true);
                event.getEntity().remove();
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
                    inventorySnapshots = YamlConfiguration.loadConfiguration(inventorySnapshotsFile);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
                inventorySnapshots = YamlConfiguration.loadConfiguration(inventorySnapshotsFile);
        }

        if (experienceSnapshots == null)
        {
            if (!experienceSnapshotsFile.exists())
            {
                try
                {
                    experienceSnapshotsFile.createNewFile();
                    experienceSnapshots = YamlConfiguration.loadConfiguration(experienceSnapshotsFile);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
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

    private boolean deletePlayerInventorySnapshotSection(Player player, boolean backup)
    {
        if (inventorySnapshots.get(player.getUniqueId().toString()) != null)
        {
            if (backup)
                inventorySnapshots.set(player.getUniqueId().toString() + System.currentTimeMillis(), inventorySnapshots.get(player.getUniqueId().toString()));
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
        //No need to save if the player is in a minigame world
        if (!instance.isSurvivalWorld(player.getWorld()))
            return false;

        //Can happen if player was teleporting to a different world while in creative mode
        if (player.getGameMode() == GameMode.CREATIVE)
            return false;

        //Avoid having to deal with players holding stuff with their mouse cursor and other inventory whatnot (though idk if a tick has to elapse for this to actually work, server side).
        player.closeInventory();

        ConfigurationSection snapshotSection = getPlayerInventorySnapshotSection(player);
        if (snapshotSection.getList("items") != null) //Do not overwrite
            return false;

        snapshotSection.set("items", Arrays.asList(player.getInventory().getContents())); //List<ItemStack> - arrays are stored and read as ArrayLists, so doing this to maintain consistency. Can optimize later if needed.
        snapshotSection.set("armor", Arrays.asList(player.getInventory().getArmorContents())); //List<ItemStack>
        //snapshotSection.set("expLevel", player.getLevel()); //int
        //snapshotSection.set("expProgress", player.getExp()); //float
        snapshotSection.set("maxHealth", player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()); //double
        snapshotSection.set("health", player.getHealth()); //double
        snapshotSection.set("foodLevel", player.getFoodLevel()); //int
        snapshotSection.set("activePotionEffects", new ArrayList<>(player.getActivePotionEffects())); //List<PotionEffect> - no idea what collection type CB uses, but I'm pretty sure it'll also be stored and read as ArrayList.

        saveInventorySnapshots();

        player.getInventory().clear();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20D);
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());

        player.sendMessage(ChatColor.DARK_GRAY + "Inventory saved and cleared.");
        instance.getLogger().info(player.getName() + ": inventory saved.");

        return true;
    }

    private void restoreInventory(Player player)
    {
        ConfigurationSection snapshotSection = getPlayerInventorySnapshotSection(player);
        if (snapshotSection.getList("items") == null) //Nothing to restore
            return;
        player.setItemOnCursor(null);
        player.closeInventory();
        player.getInventory().clear();
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //No need to restore if the player is in a minigame world
                if (!instance.isSurvivalWorld(player.getWorld()))
                    return;

                //Don't restore to creative mode players, else they'll just lose their inventory
                if (player.getGameMode() == GameMode.CREATIVE)
                    return;

                ConfigurationSection snapshotSection = getPlayerInventorySnapshotSection(player);
                if (snapshotSection.getList("items") == null) //Nothing to restore
                    return;

                player.setItemOnCursor(null);
                player.closeInventory();

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
                    player.setWalkSpeed(0.2f);
                    player.setFlySpeed(0.1f);
                    player.resetPlayerTime();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "Error occurred in attempting to restore your inventory :c Please report this!");
                    deletePlayerInventorySnapshotSection(player, true);
                    return;
                }

                deletePlayerInventorySnapshotSection(player, false);
                player.sendMessage(ChatColor.DARK_GRAY + "Inventory restored.");
                instance.getLogger().info(player.getName() + ": inventory restored.");
            }
        }.runTask(instance);
    }

    private boolean saveExperience(Player player)
    {
        //Can happen if player was teleporting to a different world while in creative mode
        if (player.getGameMode() == GameMode.CREATIVE)
            return false;

        //No need to save if the player is in a minigame world
        if (!instance.isSurvivalWorld(player.getWorld()))
            return false;

        ConfigurationSection snapshotSection = getPlayerExperienceSnapshotSection(player);
        if (snapshotSection.getList("expLevel") != null) //Do not overwrite
            return false;

        snapshotSection.set("expLevel", player.getLevel()); //int
        snapshotSection.set("expProgress", player.getExp()); //float, but stored as Double I guess
        player.sendMessage(ChatColor.DARK_GRAY + "Experience level and progress saved.");
        instance.getLogger().info(player.getName() + ": exp saved.");

        return true;
    }

    private void restoreExperience(Player player)
    {
        ConfigurationSection snapshotSection = getPlayerExperienceSnapshotSection(player);
        if (snapshotSection.get("expLevel") == null) //nothing to restore
            return;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //Don't restore to creative mode players, else they'll just lose their experience
                if (player.getGameMode() == GameMode.CREATIVE)
                    return;

                //No need to restore if the player is in a minigame world
                if (!instance.isSurvivalWorld(player.getWorld()))
                    return;

                ConfigurationSection snapshotSection = getPlayerExperienceSnapshotSection(player);
                if (snapshotSection.get("expLevel") == null) //nothing to restore
                    return;

                try
                {
                    player.setLevel(snapshotSection.getInt("expLevel"));
                    player.setExp((float)snapshotSection.getDouble("expProgress"));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "Error occurred in attempting to restore your experience :c Please report this!");
                    return;
                }
                player.sendMessage(ChatColor.DARK_GRAY + "Experience level and progress restored.");
                instance.getLogger().info(player.getName() + ": exp restored.");
                deletePlayerExperienceSnapshotSection(player);
            }
        }.runTask(instance);
    }
}
