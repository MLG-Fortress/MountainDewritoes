package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.Title;
import me.clip.actionannouncer.ActionAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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
    Location respawnLocation;
    List<String> deathMessages = new ArrayList<>();
    DeathListener(MountainDewritoes yayNoMain)
    {
        instance = yayNoMain;
        respawnLocation = new Location(instance.getServer().getWorld("minigames"), -404, 9, -157, 123.551f, 27.915f);
        deathMessages.add("lol u g0t rekt");
        deathMessages.add("U were eliminated");
        deathMessages.add("u r ded");
        deathMessages.add("u r ded, not big sooprise");
    }

    String getRandomDeathMessage()
    {
        return deathMessages.get(ThreadLocalRandom.current().nextInt(deathMessages.size()));
    }

    /*
     * Have death spectator camera point towards the killer
     * To do this, we need to store who last damaged the victim
     * That feeling when you forget the Entity object has getKiller()...
     * @param event
    Map<Player, Entity> victimsKiller = new HashMap<>();
    Map<Player, BukkitTask> wenUr2Lazy2MakeAClass = new HashMap<>();
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

        //Get the attacker
        Entity attacker = null;
        if (damager instanceof LivingEntity)
            attacker = damager;
        else if (damager instanceof Projectile)
        {
            Projectile arrow = (Projectile)damager;
            if (!(arrow.getShooter() instanceof LivingEntity))
                return; //Dispenser
            attacker = (Entity)arrow.getShooter();
        }

        //Don't care if attacker is not a LivingEntity or Projectile (we don't care about explosions, for example)
        if (attacker == null)
            return;

        Player player = (Player)event.getEntity();
        final Entity badGuy = attacker;
        if (badGuy == player)
            return;
        victimsKiller.put(player, badGuy);
        BukkitTask task = new BukkitRunnable()
        {
            public void run()
            {
                if (badGuy == victimsKiller.get(player))
                    victimsKiller.remove(player);
            }
        }.runTaskLater(instance, 300L);
        if (wenUr2Lazy2MakeAClass.containsKey(player))
            wenUr2Lazy2MakeAClass.remove(player).cancel();
        wenUr2Lazy2MakeAClass.put(player, task);
    }
     */

    @EventHandler
    void onPlayerSadness(PlayerDeathEvent event)
    {
        final Player player = event.getEntity();

        /**
        Only drop some items (randomly determined)
         */
        List<ItemStack> drops = event.getDrops();
        Iterator<ItemStack> iterator = drops.iterator();
        List<ItemStack> dropsToReturn = new ArrayList<>();
        while (iterator.hasNext())
        {
            ItemStack itemStack = iterator.next();
            if (ThreadLocalRandom.current().nextInt(4) == 0)
                continue;
            dropsToReturn.add(itemStack);
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

        //Do not execute Death spectating feature if player died to the void
        if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID)
        {
            player.playSound(player.getLocation(), "fortress.death", SoundCategory.PLAYERS, 3000000f, 1.0f);
            return;
        }

        /**
         * Death spectating timer
         * Keeps track of how long the player has been dead (counts down)
         */
        hasRecentlyDied.put(player, 180);

//        Entity killerNotFinal = null;
//        if (victimsKiller.containsKey(player) && (victimsKiller.get(player).getWorld() == player.getWorld())) //Point at killer
//            killerNotFinal = victimsKiller.remove(player);
//        final Entity killer = killerNotFinal;

        //I totally forgot that Entity#getLastDamageCause is a thing, lol
        Entity killerNotFinal = player.getKiller();
        if (player.getKiller() == null && player.getLastDamageCause() instanceof EntityDamageByEntityEvent)
        {
            killerNotFinal = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
            if (killerNotFinal != null && killerNotFinal instanceof Projectile)
            {
                Projectile arrow = (Projectile)killerNotFinal;
                if (arrow.getShooter() instanceof LivingEntity)
                    killerNotFinal = (Entity)arrow.getShooter();
            }
            if (killerNotFinal == player)
                killerNotFinal = null;
        }

        final Entity killer = killerNotFinal;

        //Believe it or not, the Minecraft client does not even trigger this sound on player death,
        //it just plays player_hurt, so yea...
        //Apparently, it actually triggers it for other players, just not the player who died, I guess...?

        /**Auto-respawn player if they haven't clicked respawn within the last 6.5 seconds
         Helps prevent weird client problems like client-side entity buildup or whatever,
         thus freezing the client or idk that's what happened to me.
         Note: Though now auto-respawn is dependent on how the player died
         */

        Long delayNotFinal = 0L;
        if (killer == null)
        {
            player.playSound(player.getLocation(), "fortress.death", SoundCategory.PLAYERS, 3000000f, 1.0f);
            delayNotFinal = 200L;
        }
        final Long delay = delayNotFinal;

        new BukkitRunnable()
        {
            public void run()
            {
                player.spigot().respawn();
                if (delay < 1L)
                    player.playSound(player.getLocation(), "fortress.death", SoundCategory.PLAYERS, 3000000f, 1.0f);
            }
        }.runTaskLater(instance, delay);

        /**
         * Death spectating
         * dont u copy m9
         */
        new BukkitRunnable()
        {
            Title.Builder deathMessageTitle = new Title.Builder();
            boolean wasDead = true;
            //Point down by default https://bukkit.org/threads/vectors.152310/#post-1703396
            Vector vector = player.getLocation().subtract(player.getLocation().add(0, 1, 0).toVector()).toVector();
            String deathMessage = getRandomDeathMessage();

            public void run()
            {
                if (!hasRecentlyDied.containsKey(player))
                {
                    this.cancel();
                    return;
                }

                if (hasRecentlyDied.get(player) < 2)
                {
                    instance.getLogger().info("Respawned player with death task: " + String.valueOf(respawnPlayer(player)));
                    return;
                }

                //Was player dead last tick and now alive this tick?
//                if (wasDead && !player.isDead())
//                {
//                    //If so, teleport half a block above
//                    wasDead = false;
//                    player.setMetadata("DEAD_MOVE", new FixedMetadataValue(instance, true));
//                    player.teleport(player.getLocation().add(0, 0.5, 0).setDirection(vector));
//                    player.removeMetadata("DEAD_MOVE", instance);
//                }

                //Track killer
                if (!player.isDead() && killer != null && killer.getWorld() == player.getWorld())
                {
                    vector = killer.getLocation().toVector().subtract(player.getLocation().toVector());
                    player.teleport(player.getLocation().setDirection(vector));
                }

                //Only send title, action message every half second
                if (!player.isDead() && hasRecentlyDied.get(player) % 10 == 0)
                {
                    deathMessageTitle.title(ChatColor.RED + deathMessage);
                    deathMessageTitle.subtitle("Respawning in " + String.valueOf((hasRecentlyDied.get(player) / 20)));
                    deathMessageTitle.fadeIn(0);
                    deathMessageTitle.fadeOut(2);
                    deathMessageTitle.stay(15);
                    player.sendTitle(deathMessageTitle.build());
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
        //Schedule task to teleport player in (9 - time spent while dead) seconds
        //TODO: may be redundant
        new BukkitRunnable()
        {
            public void run()
            {
                instance.getLogger().info("Respawned player with respawn task: " + String.valueOf(respawnPlayer(player)));
            }
        }.runTaskLater(instance, hasRecentlyDied.get(player));

        player.setMetadata("DEAD", new FixedMetadataValue(instance, true));
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(0.0f);
        event.setRespawnLocation(player.getLocation()); //TODO: Might return a "safe" location (i.e. not where they died)
        player.setViewDistance(3);
    }

    boolean respawnPlayer(Player player)
    {
        if (hasRecentlyDied.remove(player) == null)
            return false;
        player.removeMetadata("DEAD", instance);
        player.teleport(respawnLocation);
        player.setGameMode(GameMode.ADVENTURE);
        player.setFlySpeed(0.2f);
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerTryToTeleportWhenDead(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        if (!player.hasMetadata("DEAD"))
            return;
        try
        {
            //if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN && (event.getFrom().distanceSquared(event.getTo()) == 0 || event.getPlayer().hasMetadata("DEAD_MOVE")))
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN && event.getFrom().distanceSquared(event.getTo()) == 0)
                return;
        }
        catch (IllegalArgumentException e) //If teleporting to another world, yes of course stop that
        {}

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerTryToInteractWhenDead(PlayerInteractEvent event)
    {
        if (event.getPlayer().hasMetadata("DEAD"))
            event.setCancelled(true);
    }

    //Stops some abilities from being used
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerTryToSneakWhenDead (PlayerToggleSneakEvent event)
    {
        if (event.getPlayer().hasMetadata("DEAD") && event.isSneaking())
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerTryToRunCommandWhenDead(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        if (player.hasMetadata("DEAD") && !event.getMessage().toLowerCase().startsWith("/me "))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerQuitWhileSpectatingOrDead(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (player.isDead())
            player.spigot().respawn();
        if (player.hasMetadata("DEAD"))
            hasRecentlyDied.remove(player);
        player.removeMetadata("DEAD", instance);
    }

    //Instantly "respawn" players that take damage while death spectating (e.g. from void)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerTakeDamageWhileSpectating(EntityDamageEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        Player player = (Player)event.getEntity();
        if (player.getGameMode() == GameMode.SPECTATOR && player.hasMetadata("DEAD"))
        {
            hasRecentlyDied.remove(player);
            player.removeMetadata("DEAD", instance);
            player.teleport(respawnLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    //TODO: handle chat (set permissions???)
}
