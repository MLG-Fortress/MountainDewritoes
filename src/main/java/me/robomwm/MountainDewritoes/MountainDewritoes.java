package me.robomwm.MountainDewritoes;

import com.reilaos.bukkit.TheThuum.shouts.ShoutAreaOfEffectEvent;
import me.robomwm.MountainDewritoes.Commands.NickCommand;
import me.robomwm.MountainDewritoes.Events.ReverseOsmosis;
import me.robomwm.MountainDewritoes.Music.AtmosphericManager;
import me.robomwm.MountainDewritoes.Music.MemeBox;
import me.robomwm.MountainDewritoes.Sounds.HitSound;
import me.robomwm.MountainDewritoes.Sounds.LowHealth;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Robo on 2/13/2016.
 */
public class MountainDewritoes extends JavaPlugin implements Listener
{
    //Set<Player> usedEC = new HashSet<>();
    //Pattern ec = Pattern.compile("\\bec\\b|\\bechest\\b|\\bpv\\b");
    Map<Player, Integer> usingTitlePlayers = new HashMap<>();
    DamageIndicators damageIndicators;
    private Set<World> safeWorlds = new HashSet<>();
    private Set<World> survivalWorlds = new HashSet<>();
    private NSA nsa;

    public boolean isSurvivalWorld(World world)
    {
        return survivalWorlds.contains(world);
    }

    public NSA getNSA()
    {
        return nsa;
    }

    public void onEnable()
    {
        //Wow, lots-o-listeners

        damageIndicators = new DamageIndicators(this);
        PluginManager pm = getServer().getPluginManager();
        SimpleClans sc = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
        ClanManager clanManager = sc.getClanManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new SimpleClansListener(this, clanManager), this);
        pm.registerEvents(new ChatListener(this, clanManager), this);
        pm.registerEvents(new LongFallBoots(), this);
        pm.registerEvents(new DeathListener(this), this);
        pm.registerEvents(new BetterZeldaHearts(), this);
        pm.registerEvents(new RandomStructurePaster(this), this);
        pm.registerEvents(new RandomTipOfTheDay(this), this);
        //pm.registerEvents(new SecondWind(this), this);
        pm.registerEvents(new ShoppingMall(this), this);
        pm.registerEvents(new LowHealth(this), this);
        pm.registerEvents(new TeleportingEffects(this), this);
        pm.registerEvents(new HitSound(this), this);
        pm.registerEvents(new SpawnWorldListener(this), this);
        pm.registerEvents(new GamemodeInventoryManager(), this);
        pm.registerEvents(new NoKnockback(this), this);
        pm.registerEvents(damageIndicators, this);
        pm.registerEvents(new SleepManagement(this), this);
        pm.registerEvents(new ResourcePackNotifier(this), this);
        pm.registerEvents(new ReverseOsmosis(this), this);
        pm.registerEvents(new CommandOverriders(this), this);

        //Plugin-dependent listeners

        if (getServer().getPluginManager().getPlugin("MCJukebox") != null)
        {
            MemeBox memeBox = new MemeBox(this);
            pm.registerEvents(memeBox, this);
            pm.registerEvents(new AtmosphericManager(this, memeBox), this);
        }

        //Classes other plugins might want to use
        nsa = new NSA(this);
        pm.registerEvents(nsa, this);


        //Initialize commonly-used sets

        safeWorlds.add(getServer().getWorld("mall"));
        safeWorlds.add(getServer().getWorld("minigames"));
        survivalWorlds.add(getServer().getWorld("world"));
        survivalWorlds.add(getServer().getWorld("world_nether"));
        survivalWorlds.add(getServer().getWorld("world_the_end"));
        survivalWorlds.add(getServer().getWorld("cityworld"));

        //Commands

        getCommand("nick").setExecutor(new NickCommand());
    }

    public void onDisable()
    {
        getLogger().info("Cleaning up any active damage indicator holograms...");
        getLogger().info(String.valueOf(damageIndicators.cleanupDamageIndicators()) + " holograms removed.");
    }

    /**
     * Everything below are solely "miscellaneous" enhancements and fixes
     */

    //Warn new players that /ec costs money to use
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
//    void onPlayerPreprocess(PlayerCommandPreprocessEvent event)
//    {
//        //Check if player is attempting to access enderchest via command
//        String message = event.getMessage().toLowerCase();
//        if (!ec.matcher(message).matches())
//            return;
//
//        Player player = event.getPlayer();
//        //If player isn't new or if we've already warned this player before...
//        if (player.hasPlayedBefore() || usedEC.contains(player))
//            return;
//
//        player.sendMessage(ChatColor.GOLD + "Accessing the enderchest via a slash command costs 1337 dogecoins. To confirm, type /ec again.");
//        event.setCancelled(true);
//        usedEC.add(player);
//    }

    /**
     * Probably belongs in its own "TitleManager" class.
     * Only thing that uses this is the hitmarkers, so they don't override any currently-displayed message
     */
    public boolean isUsingTitle(Player player)
    {
        return usingTitlePlayers.containsKey(player);
    }
    public void addUsingTitle(Player player, int ticks)
    {
        int index = 0;
        if (isUsingTitle(player))
            index += usingTitlePlayers.get(player);
        final int finalIndex = index;
        usingTitlePlayers.put(player, index);
        new BukkitRunnable()
        {
            public void run()
            {
                if (!isUsingTitle(player))
                    return;
                if (usingTitlePlayers.get(player) == finalIndex)
                    usingTitlePlayers.remove(player);
                //Otherwise, another addUsingTitle had overrided our previous addUsingTitle invokation
            }
        }.runTaskLater(this, ticks);
    }

    /**
     * Make chunk loading when teleporting between worlds seem faster
     * We aren't doing this for every teleport since plugins might perform teleports in same chunk (e.g. PortalStick)
     * On teleporting, sets view distance to 3, then back to 8 after 5 seconds
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerChangesWorldSetViewDistance(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        World world = event.getPlayer().getWorld();
        if (player.hasMetadata("DEAD"))
            return;
        player.setViewDistance(3);
        new BukkitRunnable()
        {
            public void run()
            {
                //Don't execute if another task is scheduled
                if (player.getWorld() != world || !player.isOnline())
                    this.cancel();
                //Wait for player to land before resetting view distance
                else if (player.isOnGround())
                {
                    player.setViewDistance(8);
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 300L, 100L);
    }

    /**
     * Worldguard doesn't fully protect paintings and itemframes from being destroyed...
     * TODO: might need to include "Item" entity
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onExplosionDestroyPainting(HangingBreakEvent event)
    {
        Entity entity = event.getEntity();
        if (!safeWorlds.contains(entity.getWorld()))
            return;
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION)
            return;
        event.setCancelled(true);
    }

    /**
     * Protect dropped items from moving in the mall (and spawn I guess)
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onExplosionPushesItems(EntityExplodeEvent event)
    {
        Entity entity = event.getEntity();
        if (!safeWorlds.contains(entity.getWorld()))
            return;
        double yield = event.getYield();
        for (Entity nearbyEntity : entity.getNearbyEntities(yield, yield, yield))
        {
            if (nearbyEntity.getType() == EntityType.DROPPED_ITEM)
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Don't allow shouts to push dropped items in the mall (primarily to preserve showcases)
     * But also to prevent usage when "dead"
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    void onExplosionPushesItemsButNotViaATNTEntity(ShoutAreaOfEffectEvent event)
    {
        if (!safeWorlds.contains(event.getPlayer().getWorld()) && !event.getPlayer().hasMetadata("DEAD"))
            return;

        List<Entity> newEntities = new ArrayList<>();
        for (Entity nearbyEntity : event.getAffectedEntities())
        {
            if (nearbyEntity.getType() != EntityType.DROPPED_ITEM)
            {
                newEntities.add(nearbyEntity);
            }
        }
        event.setAffectedEntities(newEntities);
    }

    /**
     * Send an actionbar with a customizable duration
     * @param player
     * @param seconds
     * @param message
     */
    public void timedActionBar(Player player, int seconds, String message)
    {
        if (message == null || player == null)
            return;
        new BukkitRunnable()
        {
            int secondsRemaining = seconds * 2;
            public void run()
            {
                player.sendActionBar(message);
                secondsRemaining--;
                if (secondsRemaining <= 0 || !player.isOnline())
                    this.cancel();
            }
        }.runTaskTimer(this, 0L, 10L);
    }
}
