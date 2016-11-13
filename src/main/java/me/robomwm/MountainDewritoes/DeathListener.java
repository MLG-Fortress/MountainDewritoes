package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    Map<Player, Entity> victimsKiller = new HashMap<>();
    Location respawnLocation;
    DeathListener(MountainDewritoes yayNoMain)
    {
        instance = yayNoMain;
        respawnLocation = new Location(instance.getServer().getWorld("minigames"), -404, 9, -157, 123.551f, 27.915f);
    }

    /**
     * Have death spectator camera point towards the killer
     * To do this, we need to store who last damaged the victim
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void storeLastDamager(EntityDamageByEntityEvent event)
    {
        //Check if the thing did any damage at all
        if (event.getDamage() <= 0D)
            return;
        Entity damager = event.getDamager();
        //Check if victim is a player
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        //Check if attacker is an entity or projectile (we don't care about explosions, for example)
        if (damager.getType() != EntityType.PLAYER && !(damager instanceof Projectile))
            return;

        //Get the attacker
        Entity attacker = null;
        if (damager instanceof LivingEntity)
            attacker = (Entity)damager;
        else if (damager instanceof Projectile)
        {
            Projectile arrow = (Projectile)damager;
            if (!(arrow.getShooter() instanceof Entity))
                return; //Dispenser
        }
        Player player = (Player)event.getEntity();
        final Entity badGuy = attacker;
        if (badGuy == player)
            return;
        victimsKiller.put(player, badGuy);
        new BukkitRunnable()
        {
            public void run()
            {
                if (badGuy == victimsKiller.get(player))
                    victimsKiller.remove(player);
            }
        }.runTaskLater(instance, 300L);

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
        hasRecentlyDied.put(player, 180);
        Entity killerNotFinal = null;
        if (victimsKiller.containsKey(player) && (victimsKiller.get(player).getWorld() == player.getWorld())) //Point at killer
            killerNotFinal = victimsKiller.remove(player);
        final Entity killer = killerNotFinal;
        new BukkitRunnable()
        {
            Title.Builder timeTillRespawn = new Title.Builder();
            //Point down by default
            Vector vector = player.getLocation().subtract(player.getLocation().add(0, 1, 0).toVector()).toVector();

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

                //Track killer
                if (!player.isDead() && killer != null && killer.getWorld() == player.getWorld())
                {
                    vector = killer.getLocation().toVector().subtract(player.getLocation().toVector());
                    player.teleport(player.getLocation().setDirection(vector));
                }

                //Only send title every half second
                if (hasRecentlyDied.get(player) % 10 == 0)
                {
                    timeTillRespawn.title("Respawning in");
                    timeTillRespawn.subtitle(String.valueOf((hasRecentlyDied.get(player) / 20)));
                    timeTillRespawn.fadeIn(0);
                    timeTillRespawn.fadeOut(2);
                    timeTillRespawn.stay(15);
                    player.sendTitle(timeTillRespawn.build());
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
                hasRecentlyDied.remove(player); //Just in case I guess
                player.removeMetadata("DEAD", instance);
                player.teleport(respawnLocation);
                player.setGameMode(GameMode.SURVIVAL);
                player.setViewDistance(8);
            }
        }.runTaskLater(instance, hasRecentlyDied.get(player));

        player.setMetadata("DEAD", new FixedMetadataValue(instance, true));
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(0.0f);
        event.setRespawnLocation(player.getLocation().add(0, 1, 0));
        player.setViewDistance(2);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerTryToTeleportWhenDead(PlayerTeleportEvent event)
    {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN)
            return;
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
