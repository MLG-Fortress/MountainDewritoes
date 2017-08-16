package me.robomwm.MountainDewritoes;

import com.reilaos.bukkit.TheThuum.shouts.ShoutAreaOfEffectEvent;
import me.robomwm.MountainDewritoes.Commands.DebugCommand;
import me.robomwm.MountainDewritoes.Commands.EmoticonCommands;
import me.robomwm.MountainDewritoes.Commands.NickCommand;
import me.robomwm.MountainDewritoes.Commands.StaffRestartCommand;
import me.robomwm.MountainDewritoes.Commands.TipCommand;
import me.robomwm.MountainDewritoes.Commands.VoiceCommand;
import me.robomwm.MountainDewritoes.Commands.WarpCommand;
import me.robomwm.MountainDewritoes.Events.ReverseOsmosis;
import me.robomwm.MountainDewritoes.Music.AtmosphericManager;
import me.robomwm.MountainDewritoes.NotOverwatch.Ogrewatch;
import me.robomwm.MountainDewritoes.Sounds.HitSound;
import me.robomwm.MountainDewritoes.Sounds.LowHealth;
import me.robomwm.MountainDewritoes.Sounds.ReplacementSoundEffects;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by RoboMWM on 2/13/2016.
 */
public class MountainDewritoes extends JavaPlugin implements Listener
{
    //Set<Player> usedEC = new HashSet<>();
    //Pattern ec = Pattern.compile("\\bec\\b|\\bechest\\b|\\bpv\\b");
    Map<Player, Integer> usingTitlePlayers = new HashMap<>();
    private Set<World> safeWorlds = new HashSet<>();
    private Set<World> survivalWorlds = new HashSet<>();
    private Set<World> knownWorlds = new HashSet<>(); //Set of worlds we know players can teleport to for purposes other than minigames
    private FileConfiguration newConfig;
    private Economy economy;
    private boolean serverDoneLoading = false;

    public boolean isSurvivalWorld(World world)
    {
        return survivalWorlds.contains(world);
    }

    public Set<World> getSurvivalWorlds()
    {
        return new HashSet<>(survivalWorlds);
    }

    public boolean isSafeWorld(World world)
    {
        return safeWorlds.contains(world);
    }
    public boolean isKnownWorld(World world)
    {
        return knownWorlds.contains(world);
    }

    public void registerListener(Listener listener)
    {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    //Class instances used in onDisable
    BetterNoDamageTicks betterNoDamageTicks;

    @Override
    public FileConfiguration getConfig()
    {
        if(this.newConfig == null)
            this.reloadConfig();
        return this.newConfig;
    }

    @Override
    public void reloadConfig()
    {
        newConfig = new YamlConfiguration();
        newConfig.options().pathSeparator('|'); //Literally had to override these config-related members in JavaPlugin just to do this -_-
        try
        {
            newConfig.load(new File(getDataFolder(), "config.yml"));
        }
        catch (FileNotFoundException ignored) {}
        catch (IOException | InvalidConfigurationException var4)
        {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load MountainDewritoes config.yml", var4);
        }
    }

    private boolean setupEconomy(JavaPlugin plugin)
    {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void onEnable()
    {
        setupEconomy(this);
        //Wow, lots-o-listeners
        PluginManager pm = getServer().getPluginManager();
        SimpleClans sc = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
        ClanManager clanManager = sc.getClanManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new ChatListener(this, clanManager), this);
        pm.registerEvents(new LongFallBoots(), this);
        pm.registerEvents(new DeathListener(this), this);
        new BetterZeldaHearts(this, economy);
        new RandomStructurePaster(this);
        new JoinMessages(this);
        pm.registerEvents(new ShoppingMall(this), this);
        pm.registerEvents(new LowHealth(this), this);
        pm.registerEvents(new HitSound(this), this);
        pm.registerEvents(new SpawnWorldListener(this), this);
        pm.registerEvents(new GamemodeInventoryManager(this), this);
        pm.registerEvents(new NoKnockback(this), this);
        new SleepManagement(this);

        new ReverseOsmosis(this);
        new SimpleClansListener(this, clanManager);
        new ReplacementSoundEffects(this);
        new Ogrewatch(this);
        betterNoDamageTicks = new BetterNoDamageTicks(this);
        new FineSine(this);
        new PrisonIsAConfusingGamemode(this);
        new LevelingProgression(this);

        //Plugin-dependent listeners
        if (getServer().getPluginManager().getPlugin("MCJukebox") != null && getServer().getPluginManager().getPlugin("MCJukebox").isEnabled())
            new AtmosphericManager(this);
        if (getServer().getPluginManager().getPlugin("BetterTPA") != null && getServer().getPluginManager().getPlugin("BetterTPA").isEnabled())
            pm.registerEvents(new TeleportingEffects(this), this);

        //Classes other classes might want to use
        new NSA(this);

        //Initialize commonly-used sets
        safeWorlds.add(getServer().getWorld("mall"));
        safeWorlds.add(getServer().getWorld("spawn"));
        survivalWorlds.add(getServer().getWorld("WORLD"));
        survivalWorlds.add(getServer().getWorld("world_nether"));
        survivalWorlds.add(getServer().getWorld("world_the_end"));
        survivalWorlds.add(getServer().getWorld("cityworld"));
        knownWorlds.add(getServer().getWorld("WORLD"));
        knownWorlds.add(getServer().getWorld("world_nether"));
        knownWorlds.add(getServer().getWorld("world_the_end"));
        knownWorlds.add(getServer().getWorld("cityworld"));
        knownWorlds.add(getServer().getWorld("cityworld_nether"));
        knownWorlds.add(getServer().getWorld("spawn"));
        knownWorlds.add(getServer().getWorld("mall"));
        knownWorlds.add(getServer().getWorld("prison"));

        //Utilities
        new ScoreboardStuff(this, economy);
        new BukkitRunnable()
        {
            public void run()
            {
                serverDoneLoading = true;
            }
        }.runTask(this);

        //Commands
        getCommand("nick").setExecutor(new NickCommand());
        getCommand("warp").setExecutor(new WarpCommand(this));
        getCommand("restart").setExecutor(new StaffRestartCommand(this));
        getCommand("tip").setExecutor(new TipCommand(this));
        getCommand("mdebug").setExecutor(new DebugCommand());
        getCommand("voice").setExecutor(new VoiceCommand(this));
        
        EmoticonCommands emoticonCommands = new EmoticonCommands();
        getCommand("shrug").setExecutor(emoticonCommands);

        saveConfig();
    }

    public void onDisable()
    {
        //TODO: delete instantiated worlds (i.e. those not in MV)
        betterNoDamageTicks.onDisable();
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

    //Removed because it occasionally caused client-side chunk errors. Clients can reduce render distance if they're having chunk loading issues.
//    /**
//     * Make chunk loading when teleporting between worlds seem faster
//     * We aren't doing this for every teleport since plugins might perform teleports in same chunk (e.g. PortalStick)
//     * On teleporting, sets view distance to 3, then back to 8 after 5 seconds
//     * @param event
//     */
//    @EventHandler(priority = EventPriority.MONITOR)
//    void onPlayerChangesWorldSetViewDistance(PlayerChangedWorldEvent event)
//    {
//        Player player = event.getPlayer();
//        World WORLD = event.getPlayer().getWorld();
//        if (player.hasMetadata("DEAD"))
//            return;
//        player.setViewDistance(3);
//        new BukkitRunnable()
//        {
//            public void run()
//            {
//                //Don't execute if another task is scheduled
//                if (player.getWorld() != WORLD || !player.isOnline())
//                    this.cancel();
//                //Wait for player to land before resetting view distance
//                else if (player.isOnGround())
//                {
//                    player.setViewDistance(8);
//                    this.cancel();
//                }
//            }
//        }.runTaskTimer(this, 300L, 100L);
//    }

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
     * Reset things some plugins stupidly play around with >_>
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onWorldChange(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        player.setHealthScaled(false);
    }

    /**
     * Don't let serverlistplus send messages when server isn't done loading up yet
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onServerPing(ServerListPingEvent event)
    {
        if (!serverDoneLoading)
            event.setMotd(ChatColor.RED + "ayyyyy we r laodin de_memes just w8 a foow sekondz b4 konnekting thx!!");
    }

    /**
     * Send an actionbar with a customizable duration
     * @param player
     * @param seconds
     * @param message
     */
    public void timedActionBar(Player player, int seconds, String message)
    {
        if (seconds <= 0)
        {
            player.sendActionBar(message);
            return;
        }

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
