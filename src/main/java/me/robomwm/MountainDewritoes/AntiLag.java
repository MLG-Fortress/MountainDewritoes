package me.robomwm.MountainDewritoes;

import net.awesomepowered.rotator.event.RotatorSpinEvent;
import net.poweredbyawesome.snowbars.event.SnowbarSnowEvent;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

/**
 * Created on 2/22/2018.
 *
 * @author RoboMWM
 */
public class AntiLag implements Listener
{
//    private Map<Player, Integer> viewDistance = new HashMap<>();

    private int onlinePlayers;
    private MountainDewritoes plugin;
    private PluginManager pluginManager;
    //private boolean ranDisabler = true;

    public AntiLag(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("max:" + Runtime.getRuntime().maxMemory() + " free:" + Runtime.getRuntime().freeMemory() + " total:" + Runtime.getRuntime().totalMemory());
        World spawnWorld = plugin.getServer().getWorld("firstjoin");
        spawnWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        spawnWorld.setGameRule(GameRule.DISABLE_RAIDS, false);
        spawnWorld.setGameRule(GameRule.DO_TILE_DROPS, false);
        spawnWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        spawnWorld.setAutoSave(false);
//        if (Runtime.getRuntime().maxMemory() > 662700032L)
//            return;
//        ranDisabler = false;
//        new BukkitRunnable()
//        {
//            @Override
//            public void run()
//            {
//                onServerLoad(null);
//            }
//        }.runTask(plugin);
    }

    //since server start times can take forever (25 minutes!) I've decided not to do this and just have bash copy over the essential plugins
//    @EventHandler
//    private void onServerLoad(ServerLoadEvent event)
//    {
//        if (ranDisabler)
//            return;
//        ranDisabler = true;
//        Set<String> doNotDisable = new HashSet<>();
//        doNotDisable.add("MountainDewritoes");
//        doNotDisable.add("ServerListPlus");
//        doNotDisable.add("AzureResizer");
//        doNotDisable.add("CommunicationConnector");
//        doNotDisable.add("PurpleIRC");
//        doNotDisable.add("Chester");
//        doNotDisable.add("Essentials");
//        doNotDisable.add("Halp");
//        doNotDisable.add("PlugMan");
//        doNotDisable.add("HotFix");
//        doNotDisable.add("LuckPerms");
//        doNotDisable.add("Vault");
//        doNotDisable.add("ProtocolLib");
//        doNotDisable.add("Votifier");
//        doNotDisable.add("ProtocolSupport");
//        doNotDisable.add("Geyser");
//        //Need to remove hard dependency on these
//        doNotDisable.add("CustomItemRegistry");
//        doNotDisable.add("GrandioseAPI");
//        //worldgen plugins
//        doNotDisable.add("GeneratorOfGenerators");
//        doNotDisable.add("Multiverse-Core");
//        doNotDisable.add("Chunky");
//        doNotDisable.add("NullTerrain");
//        doNotDisable.add("CityWorld");
//        doNotDisable.add("WellWorld");
//        doNotDisable.add("MaxiWorld");
//        doNotDisable.add("DungeonMaze");
//
//        List<String> disabledPlugins = new ArrayList<>();
//
//        for (Plugin pluginToDisable : pluginManager.getPlugins())
//        {
//            try
//            {
//                if (doNotDisable.contains(pluginToDisable.getName()))
//                    continue;
//                if (!pluginToDisable.isEnabled())
//                {
//                    plugin.getLogger().info("Plugin " + pluginToDisable.getName() + " is not enabled, skipping.");
//                    continue;
//                }
//                pluginManager.disablePlugin(pluginToDisable, true);
//                disabledPlugins.add(pluginToDisable.getName());
//            }
//            catch (Throwable rock)
//            {
//                plugin.getLogger().warning("Failed to do something for " + pluginToDisable.getName());
//                rock.printStackTrace();
//            }
//        }
//
//        for (String pluginName : disabledPlugins)
//            plugin.dispatchCommand("plugman unload " + pluginName);
//
//        for (World world : plugin.getServer().getWorlds())
//        {
//            if (world.getName().equalsIgnoreCase("test_world"))
//                continue;
//            plugin.dispatchCommand("mv unload " + world.getName());
//        }
//
//        plugin.dispatchCommand("chunky continue");
//    }




    //Seems to cause issues with the client. Unsure if it's because view distance changes too quickly or wat.
    //Either way, causes more issues than it solves.
//    /*
//    Reduce client lag due to loading chunks from a teleport via temporarily reducing view distance
//     */
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    private void onPlayerChangesWorldSetViewDistance(PlayerTeleportEvent event)
//    {
//        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN)
//            return;
//        if (event.getFrom().getWorld() == event.getTo().getWorld() && event.getFrom().distanceSquared(event.getTo()) < 1024)
//            return;
//
//        Player player = event.getPlayer();
//
//        if (player.hasMetadata("DEAD") || player.getViewDistance() == 3)
//            return;
//
//        viewDistance.put(player, player.getViewDistance());
//
//        player.setViewDistance(3);
//    }
//    //If the client is sending movements, we assume the chunks have loaded for them, so we'll reset their original view distance.
//    @EventHandler(ignoreCancelled = true)
//    private void onPlayerMove(PlayerMoveEvent event)
//    {
//        Player player = event.getPlayer();
//        if (viewDistance.containsKey(player) && player.isOnGround())
//            player.setViewDistance(viewDistance.remove(player));
//    }

    /*
    Send block updates to the client to resolve "ghost blocks" created from high efficiency pickaxes.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerTeleportedDueToServerSayingSo(PlayerTeleportEvent event)
    {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN)
            return;
        //Why would this happen? Other than a plugin altering the teleport cause of course...
        if (event.getFrom().getWorld() != event.getTo().getWorld() || event.getFrom().distanceSquared(event.getTo()) > 16)
            return;

        Player player = event.getPlayer();
        Location location = player.getLocation();

        int ox = location.getBlockX();
        int oy = location.getBlockY();
        int oz = location.getBlockZ();

        for (int x = -1; x <= 1; x++)
        {
            for (int y = -2; y <= 2; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    //Location blockLocation = location.clone().add(x, y, z);
                    //Block block = blockLocation.getBlock();
                    //player.sendBlockChange(blockLocation, block.getBlockData());
                    Block block = location.getWorld().getBlockAt(ox + x, oy + y, oz + z);
                    player.sendBlockChange(block.getLocation(), block.getBlockData());
                }
            }
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        onlinePlayers++;
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        onlinePlayers--;
        if (onlinePlayers < 0)
        {
            plugin.getLogger().severe("dahek, how is onlinePlayers count " + onlinePlayers);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "communicationconnector onlinePlayers count somehow is at " + onlinePlayers);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onlySpinForPpl(RotatorSpinEvent event)
    {
        if (onlinePlayers == 0 || event.getRotator().getLocation().getWorld().getPlayers().isEmpty())
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onlySnowForPpl(SnowbarSnowEvent event)
    {
        if (onlinePlayers == 0 || event.getSnowbar().getBase().getLocation().getWorld().getPlayers().isEmpty())
            event.setCancelled(true);
    }
}
