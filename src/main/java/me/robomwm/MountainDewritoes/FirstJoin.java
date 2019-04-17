package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2/23/2018.
 *
 * @author RoboMWM
 */
public class FirstJoin implements Listener
{
    private MountainDewritoes plugin;
    private Location firstJoinLocation;
    private Location cellar;
    private Map<Location, String> tutorialLocations = new HashMap<>();

    public FirstJoin(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        firstJoinLocation = new Location(plugin.getServer().getWorld("firstjoin"), -1.5, 26.5, -3.5, 180, 20);
        cellar = new Location(plugin.getServer().getWorld("firstjoin"), -5, 25, 25);
//        new BukkitRunnable()
//        {
//            Location location = new Location(WORLD, -1, 70, -4);
//            @Override
//            public void run()
//            {
//                for (Player player : WORLD.getPlayers())
//                {
//                    if (!NSA.getTempdata(player, "firstjoin"))
//                        return;
//                    if (player.getLocation().distanceSquared(location) > 1)
//                        return;
//                    NSA.removeTempdata(player, "firstjoin");
//                    player.sendBlockChange(new Location(WORLD, -1, 71, -6), Material.SMOOTH_BRICK, (byte)2);
//                    player.sendBlockChange(new Location(WORLD, -1, 70, -6), Material.SMOOTH_BRICK, (byte)1);
//                    player.sendBlockChange(new Location(WORLD, -2, 69, -4), Material.AIR, (byte)0);
//                    player.sendBlockChange(new Location(WORLD, -2, 70, -4), Material.AIR, (byte)0);
//                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, 1.0f);
//                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, 1.0f);
//                }
//            }
//        }.runTaskTimer(plugin, 1200L, 10L);
    }

    private void onJoinWorld(Player player)
    {
        if (player.getWorld() == firstJoinLocation.getWorld())
            player.teleport(firstJoinLocation);
        //player.sendBlockChange(new Location(WORLD, -2, 69, -4), Material.SUGAR_CANE_BLOCK, (byte)0);
        //player.sendBlockChange(new Location(WORLD, -2, 70, -4), Material.SUGAR_CANE_BLOCK, (byte)0);
        //NSA.setTempdata(player, "firstjoin");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoinBeforeJoin(PlayerInitialSpawnEvent event)
    {
        if (event.getPlayer().hasPlayedBefore())
            return;
        if (event.getSpawnLocation().getWorld() == firstJoinLocation.getWorld())
            event.setSpawnLocation(firstJoinLocation);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                onJoinWorld(player);
            }
        }.runTaskLater(plugin, 2L);
        event.getPlayer().setMaximumAir(event.getPlayer().getMaximumAir() + player.getLevel()); //Lol idk the default


        if (!event.getPlayer().hasPlayedBefore())
        {
            event.getPlayer().getInventory().addItem(plugin.getCustomItemRecipes().getItem("GOLD_BOOTS"));
            event.getPlayer().getInventory().addItem(plugin.getCustomItemRecipes().getItem("The_Gold_Plate"));
            event.getPlayer().getInventory().addItem(plugin.getCustomItemRecipes().getItem("Coal_Pickaxe"));

            //Spawn mobs in cellar, if none exist in it.
//            for (Entity entity : cellar.getChunk().getEntities())
//                if (entity.getType() == EntityType.SILVERFISH)
//                    return;
//            ((Monster)cellar.getWorld().spawnEntity(cellar, EntityType.SILVERFISH)).setAI(false);
//            ((Monster)cellar.getWorld().spawnEntity(cellar.add(1,0,1), EntityType.SILVERFISH)).setAI(false);
//            ((Monster)cellar.getWorld().spawnEntity(cellar.add(1,0,-1), EntityType.SILVERFISH)).setAI(false);
//            ((Monster)cellar.getWorld().spawnEntity(cellar.add(-1,0,-1), EntityType.SILVERFISH)).setAI(false);
//            ((Monster)cellar.getWorld().spawnEntity(cellar.add(-1,0,1), EntityType.SILVERFISH)).setAI(false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onMobsSpawn(CreatureSpawnEvent event)
    {
        if (event.getEntity().getWorld() != firstJoinLocation.getWorld())
            return;
        if (event.getEntityType() == EntityType.SILVERFISH)
            return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event)
    {
        if (event.getPlayer().getWorld() == firstJoinLocation.getWorld())
            onJoinWorld(event.getPlayer());
    }

    @EventHandler
    private void onPlayerTerminal(PlayerInteractEvent event)
    {
        if (event.getPlayer().getWorld() != firstJoinLocation.getWorld())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        Player player = event.getPlayer();
        BookMeta bookMeta;

        switch (event.getClickedBlock().getType())
        {
            case REPEATING_COMMAND_BLOCK:
                bookMeta = LazyText.getBookMeta();
                bookMeta.spigot().addPage(
                        LazyText.buildPage("Deer " + player.getDisplayName() + ChatColor.BLACK +
                                ",\nSorry 4 missin ur arrival, but I c ur quite an inexperienced adventurer anyways.\u00AF\\_(\u30C4)_/\u00AF\nHow about u go to the cellar and clean up those annoying paper-eaters."),
                        LazyText.buildPage("The cellar is behind you and to the right. Don't worry, your hands should do the trick. And u got a shirt and shoes. So ur gud."));
                break;
            case CHAIN_COMMAND_BLOCK:
                bookMeta = LazyText.getBookMeta();
                bookMeta.spigot().addPage(
                        LazyText.buildPage("Hey " + player.getDisplayName() + ChatColor.BLACK +
                        ",\nThanks for helpin out. Hopefully u got a bit more experience! Btw, Press F to open the menu, or do /menu if ur on lameo 1.8-o.\nEnjoy de /minigames or da memetastic /wild !")
                );
                break;
            default:
                return;
        }

        plugin.openBook(player, LazyText.getBook(bookMeta));
    }

    @EventHandler(ignoreCancelled = true)
    private void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        if (event.getTo().getWorld() != firstJoinLocation.getWorld())
            return;
        for (Map.Entry<Location, String> location : tutorialLocations.entrySet())
        {
            if (location.getKey().distanceSquared(event.getTo()) > 100)
                continue;

            if (player.hasMetadata(location.getValue()))
                return;

            switch (location.getValue())
            {
                case "leggings":

                    player.getInventory().addItem(plugin.getCustomItemRecipes().getItem("GOLDEN_LEGGINGS"));
                    break;
            }
            return;
        }
    }
}
