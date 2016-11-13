package me.robomwm.MountainDewritoes;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 5/25/2016.
 * Reimplements sound effect on death and other, future miscellaneous stuff
 */
public class DeathListener implements Listener
{
    MountainDewritoes instance;
    HashMap<Player, List<ItemStack>> deathItems = new HashMap<>();
    Map<Player, Integer> hasRecentlyDied = new HashMap<>();
    Random random = new Random();
    Location respawnLocation;
    DeathListener(MountainDewritoes yayNoMain)
    {
        instance = yayNoMain;
        respawnLocation = new Location(instance.getServer().getWorld("minigames"), -404, 9, -157, 123.551f, 27.915f);
    }

    @EventHandler
    void onPlayerSadness(PlayerDeathEvent event)
    {
        final Player player = event.getEntity();

        /**
        Only drop some items (randomly determined)
         */
        ItemStack drop;
        List<ItemStack> drops = event.getDrops();
        Iterator<ItemStack> iterator = drops.iterator();
        List<ItemStack> dropsToReturn = new ArrayList<>();
        while (iterator.hasNext())
        {
            if (ThreadLocalRandom.current().nextInt(4) == 0)
            {
                iterator.next();
                continue;
            }
            dropsToReturn.add(iterator.next());
            iterator.remove();
        }
        deathItems.put(player, dropsToReturn);

        /**
         * Only lose 8 XP (vs. all XP on death)
         */
        if (player.getLevel() > 8)
            event.setNewLevel(player.getLevel() - 8);

        //Stop all playing sounds, if any.
        player.stopSound("");
        //Believe it or not, the Minecraft client does not even trigger this sound on player death,
        //it just plays player_hurt, so yea...
        //Apparently, it actually triggers it for other players, just not the player who died, I guess...?
        player.playSound(player.getLocation(), "fortress.death", 3000000f, 1.0f);

        /**Auto-respawn player if they haven't clicked respawn within the last 6.5 seconds
        //Helps prevent weird client problems like client-side entity buildup or whatever,
        thus freezing the client or idk that's what happened to me.
         */
        new BukkitRunnable()
        {
            public void run()
            {
                player.spigot().respawn();
            }
        }.runTaskLater(instance, 130L);

        /**
         * Death spectating timer
         * Keeps track of how long the player has been dead (counts down)
         */
        hasRecentlyDied.put(player, 120);
        new BukkitRunnable()
        {
            public void run()
            {
                if (!hasRecentlyDied.containsKey(player))
                {
                    this.cancel();
                    return;
                }

                if (hasRecentlyDied.get(player) < 2)
                {
                    hasRecentlyDied.remove(player);
                    return;
                }

                hasRecentlyDied.put(player, hasRecentlyDied.get(player) - 1);
            }
        }.runTaskTimer(instance, 1L, 1L);
    }

    /**
     * Give back items and exp that were not dropped on death
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerRespawn(PlayerRespawnEvent event)
    {
        event.setRespawnLocation(respawnLocation);
        Player player = event.getPlayer();
        //Items
        if (deathItems.containsKey(player))
        {
            for (ItemStack drop : deathItems.get(player))
            {
                player.getInventory().addItem(drop);
            }
            player.sendMessage("Saved " + String.valueOf(deathItems.get(player).size()) + " item stacks from your inventory when you died.");
            deathItems.remove(player);
        }

        if (!hasRecentlyDied.containsKey(player))
            return;

        /**
         * Death spectating
         */

        //Schedule task to teleport player in (6 - time spent while dead) seconds
        new BukkitRunnable()
        {
            public void run()
            {
                player.teleport(respawnLocation);
                player.removeMetadata("DEAD", instance);
            }
        }.runTaskLater(instance, hasRecentlyDied.get(player));

        player.setMetadata("DEAD", new FixedMetadataValue(instance, true));
        player.setGameMode(GameMode.SPECTATOR);
        event.setRespawnLocation(player.getLocation());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerTryToTeleportWhenDead(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        if (player.hasMetadata("DEAD"))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerTryToRunCommandWhenDead(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        if (player.hasMetadata("DEAD") && !event.getMessage().toLowerCase().startsWith("/me "))
            event.setCancelled(true);
    }
}
