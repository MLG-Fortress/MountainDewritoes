package me.robomwm.MountainDewritoes;

import com.reilaos.bukkit.TheThuum.shouts.ShoutAreaOfEffectEvent;
import com.robomwm.customitemregistry.CustomItemRegistry;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import me.robomwm.MountainDewritoes.Commands.ChangelogCommand;
import me.robomwm.MountainDewritoes.Commands.ClearChatCommand;
import me.robomwm.MountainDewritoes.Commands.DebugCommand;
import me.robomwm.MountainDewritoes.Commands.DisplayAdminCommand;
import me.robomwm.MountainDewritoes.Commands.EmoticonCommands;
import me.robomwm.MountainDewritoes.Commands.Emoticons;
import me.robomwm.MountainDewritoes.Commands.MinedownBookCommand;
import me.robomwm.MountainDewritoes.Commands.MopCommand;
import me.robomwm.MountainDewritoes.Commands.NickCommand;
import me.robomwm.MountainDewritoes.Commands.PseudoCommands;
import me.robomwm.MountainDewritoes.Commands.ResetCommands;
import me.robomwm.MountainDewritoes.Commands.SpaceshipCommand;
import me.robomwm.MountainDewritoes.Commands.StaffRestartCommand;
import me.robomwm.MountainDewritoes.Commands.SyncCommand;
import me.robomwm.MountainDewritoes.Commands.TipCommand;
import me.robomwm.MountainDewritoes.Commands.ViewDistanceCommand;
import me.robomwm.MountainDewritoes.Commands.VoiceCommand;
import me.robomwm.MountainDewritoes.Commands.WarpCommand;
import me.robomwm.MountainDewritoes.Events.ReverseOsmosis;
import me.robomwm.MountainDewritoes.Music.AtmosphericManager;
import me.robomwm.MountainDewritoes.NotOverwatch.Ogrewatch;
import me.robomwm.MountainDewritoes.Rewards.LodsOfEmone;
import me.robomwm.MountainDewritoes.Sounds.HitSound;
import me.robomwm.MountainDewritoes.Sounds.LowHealth;
import me.robomwm.MountainDewritoes.Sounds.ReplacementSoundEffects;
import me.robomwm.MountainDewritoes.arena.Arena;
import me.robomwm.MountainDewritoes.armor.ArmorAugmentation;
import me.robomwm.MountainDewritoes.combat.BetterNoDamageTicks;
import me.robomwm.MountainDewritoes.combat.BetterZeldaHearts;
import me.robomwm.MountainDewritoes.combat.DummerEnderman;
import me.robomwm.MountainDewritoes.combat.NoKnockback;
import me.robomwm.MountainDewritoes.combat.twoshot.TwoShot;
import me.robomwm.MountainDewritoes.hotmenu.HotMenu;
import me.robomwm.MountainDewritoes.lab.SpawnSomeMobs;
import me.robomwm.MountainDewritoes.notifications.Notifications;
import me.robomwm.MountainDewritoes.serverlist.ServerListPingListener;
import me.robomwm.MountainDewritoes.spaceship.SpaceshipPilot;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.SoundCategory;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Created by RoboMWM on 2/13/2016.
 */
public class MountainDewritoes extends JavaPlugin implements Listener
{
    //metadata "API" that allows us to clear metadata onDisable
    private Map<String, Set<Metadatable>> usedMetadata = new HashMap<>();
    public void setMetadata(Metadatable target, String key, Object value)
    {
        if (!usedMetadata.containsKey(key))
            usedMetadata.put(key, new HashSet<>());
        usedMetadata.get(key).add(target);
        target.setMetadata(key, new FixedMetadataValue(this, value));
    }

    //Set<Player> usedEC = new HashSet<>();
    //Pattern ec = Pattern.compile("\\bec\\b|\\bechest\\b|\\bpv\\b");
    private long currentTick = 0L; //"Server time in ticks"
    private Set<World> minigameWorlds = new HashSet<>();
    private FileConfiguration newConfig;
    private Economy economy;
    private boolean serverDoneLoading = false;

    public long getCurrentTick()
    {
        return currentTick;
    }

    public boolean isSurvivalWorld(World world)
    {
        return !minigameWorlds.contains(world);
    }

    public void registerListener(Listener listener)
    {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    //Class instances used in onDisable/other classes
    private BetterNoDamageTicks betterNoDamageTicks;
    private TitleManager titleManager;
    private SimpleClansListener simpleClansListener;
    private TipCommand tipCommand;
    private AtmosphericManager atmosphericManager;
    private CustomItemRegistry customItemRegistry;
    private PlayerDataStore playerDataStore;
    private SpaceshipPilot spaceshipPilot;

    public PlayerDataStore getPlayerDataStore()
    {
        return playerDataStore;
    }

    public SpaceshipPilot getSpaceshipPilot()
    {
        return spaceshipPilot;
    }

    @Deprecated
    public void openBook(Player player, ItemStack book)
    {
        player.openBook(book);
    }

    public CustomItemRegistry getCustomItemRegistry()
    {
        return customItemRegistry;
    }

    public TitleManager getTitleManager()
    {
        return titleManager;
    }

    public SimpleClansListener getSimpleClansListener()
    {
        return simpleClansListener;
    }

    public Economy getEconomy()
    {
        return economy;
    }

    public boolean isServerDoneLoading()
    {
        return serverDoneLoading;
    }

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
        tipCommand = new TipCommand(this);
        customItemRegistry = (CustomItemRegistry)getServer().getPluginManager().getPlugin("CustomItemRegistry");

        //Initialize commonly-used sets

        Set<World> safeWorlds = new HashSet<>();
        safeWorlds.add(getServer().getWorld("mall"));
        safeWorlds.add(getServer().getWorld("prison"));
        safeWorlds.add(getServer().getWorld("firstjoin"));
        safeWorlds.add(getServer().getWorld("CreativeParkourMaps"));
        safeWorlds.remove(null);

        for (World world : safeWorlds)
        {
            world.setPVP(false);
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        }

//        survivalWorlds.add(getServer().getWorld("mall"));
//        survivalWorlds.add(getServer().getWorld("prison"));
//        survivalWorlds.add(getServer().getWorld("firstjoin"));
//        survivalWorlds.add(getServer().getWorld("world"));
//        survivalWorlds.add(getServer().getWorld("world_nether"));
//        survivalWorlds.add(getServer().getWorld("world_the_end"));
//        survivalWorlds.add(getServer().getWorld("cityworld"));
//        survivalWorlds.add(getServer().getWorld("cityworld_nether"));
//        survivalWorlds.add(getServer().getWorld("maxiworld"));
//        survivalWorlds.add(getServer().getWorld("wellworld"));

//        if (getServer().getWorld("world") == null)
//        {
//            dispatchCommand("mv create " + "world" + " " + "normal" + " -t " + "amplified -g MountainDewritoes");
//        }
//        if (getServer().getWorld("world_nether") == null)
//        {
//            dispatchCommand("mv create " + "world_nether" + " " + "nether" + " -t " + "amplified -g MountainDewritoes");
//        }
//        if (getServer().getWorld("world_the_end") == null)
//        {
//            dispatchCommand("mv create " + "world_the_end" + " " + "end" + " -t " + "amplified -g MountainDewritoes");
//        }

//        minigameWorlds.add(getServer().getWorld("minigames"));
//        minigameWorlds.add(getServer().getWorld("bam"));
//        minigameWorlds.add(getServer().getWorld("flatroom"));
//        minigameWorlds.add(getServer().getWorld("CreativeParkourMaps"));
//        minigameWorlds.add(getServer().getWorld("dogepvp"));

        for (World world : getServer().getWorlds())
        {
            //Don't keep spawn chunks in memory
            world.setKeepSpawnInMemory(false);
            world.setGameRule(GameRule.NATURAL_REGENERATION, false);

            //minigame worlds don't do daylight cycles
            if (!world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))
                minigameWorlds.add(world);

            //else it's a non-minigame world
            //Set border on survival worlds
            //Border is a "hard stop", most worlds are generated to a much smaller radius.
            else if (world.getPVP() && !minigameWorlds.contains(world) && !world.getGameRuleValue(GameRule.KEEP_INVENTORY))
                world.getWorldBorder().setSize(20000);

        }

        //getServer().getWorld("firstjoin").setKeepSpawnInMemory(true); //TODO: revert when paper fixes https://github.com/PaperMC/Paper/issues/4693

        //Classes other classes might want to use
        new NSA(this);
        playerDataStore = new PlayerDataStore(this);

        //Wow, lots-o-listeners
        PluginManager pm = getServer().getPluginManager();
        SimpleClans sc = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
        ClanManager clanManager = null;
        pm.registerEvents(this, this);
        if (sc != null) {
            clanManager = sc.getClanManager();
            pm.registerEvents(new ChatListener(this, clanManager), this);
        }
        pm.registerEvents(new DeathListener(this), this);
        if (customItemRegistry != null) {
            new BetterZeldaHearts(this, economy);
        }
        new JoinMessages(this);
        pm.registerEvents(new ShoppingMall(this), this);
        pm.registerEvents(new LowHealth(this), this);
        pm.registerEvents(new HitSound(this), this);
        new GamemodeInventoryManager(this);
        pm.registerEvents(new NoKnockback(this), this);
        new SleepManagement(this);
        new ServerListPingListener(this);

        new ReverseOsmosis(this);
        if (sc != null) {
            simpleClansListener = new SimpleClansListener(this, clanManager);
        }
        new ReplacementSoundEffects(this);
        new Ogrewatch(this);
        betterNoDamageTicks = new BetterNoDamageTicks(this);
        new FineSine(this);
        new LodsOfEmone(this);
        new PseudoCommands(this);
        new TabList(this);
//        new TheMidnightPortalToAnywhere(this);
        atmosphericManager = new AtmosphericManager(this);
        if (customItemRegistry != null) {
            new ArmorAugmentation(this);
        }
        new AntiLag(this);
        if (customItemRegistry != null) {
            new FirstJoin(this);
        }
        new DummerEnderman(this);
        new OldTNT(this);
        spaceshipPilot = new SpaceshipPilot(this);
        new Arena(this);
        if (customItemRegistry != null) {
            new TwoShot(this);
        }
        new SpawnSomeMobs(this, getServer().getWorld("mall"));

        //Plugin-dependent listeners
        if (getServer().getPluginManager().getPlugin("BetterTPA") != null && getServer().getPluginManager().getPlugin("BetterTPA").isEnabled())
            pm.registerEvents(new TeleportingEffects(this), this);

        //Utilities
        new Notifications(this);
        new HotMenu(this);
        new BukkitRunnable()
        {
            public void run()
            {
                currentTick++;
            }
        }.runTaskTimer(this, 1L, 1L);
        titleManager = new TitleManager(this);

        //Commands
        getCommand("nick").setExecutor(new NickCommand(this));
        getCommand("warp").setExecutor(new WarpCommand(this));
        StaffRestartCommand staffRestartCommand = new StaffRestartCommand(this);
        getCommand("restart").setExecutor(staffRestartCommand);
        getCommand("restartnow").setExecutor(staffRestartCommand);
        getCommand("update").setExecutor(staffRestartCommand);
        getCommand("tip").setExecutor(tipCommand);
        DebugCommand debugCommand = new DebugCommand(this);
        getCommand("mdebug").setExecutor(debugCommand);
        getCommand("lejail").setExecutor(debugCommand);
        getCommand("watchwinreward").setExecutor(debugCommand);
        getCommand("md").setExecutor(debugCommand);
        getCommand("voice").setExecutor(new VoiceCommand(this));
        getCommand("view").setExecutor(new ViewDistanceCommand(this));
        getCommand("reset").setExecutor(new ResetCommands(this));
        getCommand("clearchat").setExecutor(new ClearChatCommand());
        new Emoticons(this);
        new ChangelogCommand(this);
        new MinedownBookCommand(this);
        new SyncCommand(this);
        new MopCommand(this);
        getCommand("displayadmin").setExecutor(new DisplayAdminCommand());

        EmoticonCommands emoticonCommands = new EmoticonCommands(this);
        getCommand("shrug").setExecutor(emoticonCommands);

        saveConfig();
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                serverDoneLoading = true;
                getLogger().info("WE DONE");
            }
        }.runTask(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onServerLoadEvent(ServerLoadEvent event)
    {
        getLogger().info("EVENT LOAD SEZ WE DONE");
    }

    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
        betterNoDamageTicks.onDisable();
        for (Player player : getServer().getOnlinePlayers())
            atmosphericManager.stopMusic(player);
        for (String key : usedMetadata.keySet())
            for (Metadatable target : usedMetadata.get(key))
                target.removeMetadata(key, this);
    }

    public void dispatchCommand(String command)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                StackTraceElement e = Thread.currentThread().getStackTrace()[2];
                System.out.println("Executing command from " + e.getClassName() + "#" + e.getMethodName() + "@" + e.getLineNumber() + ":" + command);
                getServer().dispatchCommand(getServer().getConsoleSender(), command);
            }
        }.runTask(this);
    }

    /*Convenience methods that rely on soft dependencies*/
    public boolean isOutdatedClient(Player player)
    {
        if (!getServer().getPluginManager().isPluginEnabled("ViaVersion"))
            return false;

        ProtocolVersion clientVersion = Via.getAPI().getPlayerProtocolVersion(player);
        ProtocolVersion serverVersion = Via.getAPI().getServerVersion().highestSupportedProtocolVersion();
        if (clientVersion == ProtocolVersion.unknown || serverVersion == ProtocolVersion.unknown)
            return false;
        return clientVersion.olderThan(serverVersion);
    }

    public String getClientProtocolName(Player player)
    {
        if (!getServer().getPluginManager().isPluginEnabled("ViaVersion"))
            return "unknown version";

        ProtocolVersion clientVersion = Via.getAPI().getPlayerProtocolVersion(player);
        if (clientVersion == ProtocolVersion.unknown)
            return "unknown version";
        return clientVersion.getName();
    }

    /*
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

//    Initially removed because it occasionally caused client-side chunk errors. Clients can reduce render distance if they're having chunk loading issues.
//    We'll see if this is still the case...
//    idk if it's an issue but I haven't had world loading issues for a while. Though
//    /**
//     * Make chunk loading when teleporting between worlds seem faster
//     * @param event
//     */
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    void onPlayerChangesWorldSetViewDistance(PlayerTeleportEvent event)
//    {
//        if (event.getFrom().getWorld() == event.getTo().getWorld())
//            return;
//
//        Player player = event.getPlayer();
//        if (player.hasMetadata("DEAD"))
//            return;
//        new BukkitRunnable()
//        {
//            public void run()
//            {
//                //Don't execute if already set
//                if (player.getViewDistance() > 3 || !getServer().getOnlinePlayers().contains(player))
//                    this.cancel();
//                //Wait for player to land before resetting view distance
//                else if (player.isOnGround())
//                {
//                    player.setViewDistance(8);
//                    this.cancel();
//                }
//            }
//        }.runTaskTimer(this, 200L, 100L);
//    }

    /**
     * Worldguard doesn't fully protect paintings and itemframes from being destroyed...
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onExplosionDestroyPainting(HangingBreakEvent event)
    {
        Entity entity = event.getEntity();
        if (entity.getWorld().getPVP())
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
        if (entity.getWorld().getPVP())
            return;
        double yield = event.getYield();
        for (Entity nearbyEntity : entity.getNearbyEntities(yield, yield, yield))
        {
            if (nearbyEntity.getType() == EntityType.ITEM)
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
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), "fortress.fusrodah", SoundCategory.PLAYERS, 1.5f, 1.0f);
        if (event.getPlayer().getWorld().getPVP() && !event.getPlayer().hasMetadata("DEAD"))
            return;

        List<Entity> newEntities = new ArrayList<>();
        for (Entity nearbyEntity : event.getAffectedEntities())
        {
            if (nearbyEntity.getType() != EntityType.ITEM || !nearbyEntity.hasMetadata("NO_PICKUP"))
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
     * Name items that spawn
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onItemSpawn(ItemSpawnEvent event)
    {
        if (event.getEntity().isCustomNameVisible() || event.getEntity().getCustomName() != null)
            return;

        ItemStack itemStack = event.getEntity().getItemStack();
        String name = itemStack.getI18NDisplayName();
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            name = itemStack.getItemMeta().getDisplayName();

        event.getEntity().setCustomName(name);
    }

    /**
     * Convenience method
     * @param min
     * @param max
     * @return
     */
    public int r4nd0m(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
