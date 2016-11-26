package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    Location respawnLocation;
    DeathListener(MountainDewritoes yayNoMain)
    {
        instance = yayNoMain;
        respawnLocation = new Location(instance.getServer().getWorld("minigames"), -404, 9, -157, 123.551f, 27.915f);
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
            instance.getLogger().info(itemStack.getType().toString());
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
        //Believe it or not, the Minecraft client does not even trigger this sound on player death,
        //it just plays player_hurt, so yea...
        //Apparently, it actually triggers it for other players, just not the player who died, I guess...?
        //Note: deciding to play death message when spectating (half a second after death)

        /**Auto-respawn player if they haven't clicked respawn within the last 6.5 seconds
        Helps prevent weird client problems like client-side entity buildup or whatever,
        thus freezing the client or idk that's what happened to me.

        Though now we're just going to respawn them within a half second.
         */
        new BukkitRunnable()
        {
            public void run()
            {
                player.spigot().respawn();
                player.playSound(player.getLocation(), "fortress.death", SoundCategory.PLAYERS, 3000000f, 1.0f);
            }
        }.runTaskLater(instance, 10L);

        /**
         * Death spectating timer
         * Keeps track of how long the player has been dead (counts down)
         */
        hasRecentlyDied.put(player, 180);

//        Entity killerNotFinal = null;
//        if (victimsKiller.containsKey(player) && (victimsKiller.get(player).getWorld() == player.getWorld())) //Point at killer
//            killerNotFinal = victimsKiller.remove(player);
//        final Entity killer = killerNotFinal;


        new BukkitRunnable()
        {
            Title.Builder timeTillRespawn = new Title.Builder();
            boolean wasDead = true;
            //Point down by default
            //https://bukkit.org/threads/vectors.152310/#post-1703396
            Vector vector = player.getLocation().subtract(player.getLocation().add(0, 1, 0).toVector()).toVector();
            Entity killer = player.getKiller();

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

                //Was player dead last tick and now alive this tick?
                if (wasDead && !player.isDead())
                {
                    //If so, teleport half a block above
                    wasDead = false;
                    player.setMetadata("DEAD_MOVE", new FixedMetadataValue(instance, true));
                    player.teleport(player.getLocation().add(0, 0.5, 0).setDirection(vector));
                    player.removeMetadata("DEAD_MOVE", instance);
                }

                //Track killer
                if (!player.isDead() && killer != null && killer.getWorld() == player.getWorld())
                {
                    vector = killer.getLocation().toVector().subtract(player.getLocation().toVector());
                    player.teleport(player.getLocation().setDirection(vector));
                }

                //Only send title every half second
                if (!player.isDead() && hasRecentlyDied.get(player) % 10 == 0)
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
        //Schedule task to teleport player in (9 - time spent while dead) seconds
        new BukkitRunnable()
        {
            public void run()
            {
                hasRecentlyDied.remove(player); //Just in case I guess
                player.removeMetadata("DEAD", instance);
                player.teleport(respawnLocation);
                player.setGameMode(GameMode.SURVIVAL);
            }
        }.runTaskLater(instance, hasRecentlyDied.get(player));

        player.setMetadata("DEAD", new FixedMetadataValue(instance, true));
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(0.0f);
        event.setRespawnLocation(player.getLocation().add(0, 0, 0));
        player.setViewDistance(3);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerTryToTeleportWhenDead(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        if (!player.hasMetadata("DEAD"))
            return;
        try
        {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN && (event.getFrom().distanceSquared(event.getTo()) == 0 || event.getPlayer().hasMetadata("DEAD_MOVE")))
                return;
        }
        catch (IllegalArgumentException e) //If teleporting to another world, yes of course stop that
        {}

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
    }

    //TODO: handle chat (set permissions???)
}
