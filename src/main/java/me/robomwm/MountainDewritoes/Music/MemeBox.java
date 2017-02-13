package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.mcjukebox.plugin.bukkit.api.JukeboxAPI;
import net.mcjukebox.plugin.bukkit.api.ResourceType;
import net.mcjukebox.plugin.bukkit.api.models.Media;
import net.mcjukebox.plugin.bukkit.events.ClientConnectEvent;
import net.mcjukebox.plugin.bukkit.events.ClientDisconnectEvent;
import net.mcjukebox.plugin.bukkit.managers.shows.ShowManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by RoboMWM on 11/5/2016.
 * Provides jukeboxes to play "additional tracks"
 * Does NOT use AtmosphericManager's metadata system
 * (i.e. retaining vanilla behavior)
 */
public class MemeBox implements Listener
{
    MountainDewritoes instance;
    Set<World> specialWorlds = new HashSet<>();
    Map<String, Integer> connectedPlayers = new HashMap<>();

    public MemeBox(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        instance.getServer().getPluginManager().registerEvents(new AtmosphericManager(instance, this), instance);
        specialWorlds.add(instance.getServer().getWorld("mall"));
    }

    public void playSound(String show, MusicThing song)
    {
        ShowManager showManager = JukeboxAPI.getShowManager();
        Media media = new Media(ResourceType.MUSIC, song.getURL());
        media.setLooping(false);
        if (show == null)
            show = "default";
        Media currentlyPlaying = showManager.getShow(show).getCurrentTrack();
        instance.getLogger().info("currentlyPlaying is null: " + String.valueOf(currentlyPlaying == null));
        showManager.getShow(show).play(media);
    }

    public void switchPlayerShow(Player player, World fromWorld)
    {
        World targetWorld = player.getWorld();
        ShowManager showManager = JukeboxAPI.getShowManager();

        if (fromWorld != null)
        {
            //Remove player from previous show, if necessary
            if (specialWorlds.contains(targetWorld) != specialWorlds.contains(fromWorld))
                showManager.getShow(fromWorld.getName()).removeMember(player);
            else //Otherwise, there's no need to change this player's show
                return;
        }

        if (specialWorlds.contains(player.getWorld()))
            showManager.getShow(player.getWorld().getName()).addMember(player, false);
        else
            showManager.getShow("normal").addMember(player, false);
    }

    public void addPlayerShow(String show, Player player)
    {
        JukeboxAPI.getShowManager().getShow(show).addMember(player, false);
    }

    public void removePlayerShow(String show, Player player)
    {
        JukeboxAPI.getShowManager().getShow(show).removeMember(player);
    }

    @EventHandler
    void memeBoxOpened(ClientConnectEvent event)
    {
        String username = event.getUsername();
        Integer connections = connectedPlayers.get(username);
        if (connections == null)
            connectedPlayers.put(username, 1);
        else
            connectedPlayers.put(username, ++connections);
//        Player player = instance.getServer().getPlayer(event.getUsername());
//        if (player == null)
//        {
//            instance.getLogger().warning("An offline player connected to the memebox??");
//            return;
//        }
//        player.setMetadata("MD_MEMEBOX", new FixedMetadataValue(instance, true));
    }

    @EventHandler
    void memeBoxClosed(ClientDisconnectEvent event)
    {
        String username = event.getUsername();
        Integer connections = connectedPlayers.get(username);
        if (connections == null) //wat
            return;
        else if (connections > 1)
            connectedPlayers.put(username, --connections);
        else
            connectedPlayers.remove(username);

//        Player player = instance.getServer().getPlayer(event.getUsername());
//        if (player == null)
//        {
//            instance.getLogger().warning("Maybe the player quit the server first?");
//            return;
//        }
//        player.removeMetadata("MD_MEMEBOX", instance);
    }

    public boolean hasOpenedMemeBox(Player player)
    {
        return connectedPlayers.containsKey(player.getName());
        //return player.hasMetadata("MD_JUKEBOX");
    }

    boolean sendListOfConnectedUsers(CommandSender sender, String message)
    {
        if (message.equalsIgnoreCase("/jukeboxlist") && sender.isOp())
        {
            StringBuilder listOfPlayers = new StringBuilder("Players connected 2 memebox: ");
            for (Player player : instance.getServer().getOnlinePlayers())
            {
                if (hasOpenedMemeBox(player))
                    listOfPlayers.append(player.getName() + ", ");
            }
            sender.sendMessage(listOfPlayers.toString());
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    void playerWantsListOfConnectedUsers(PlayerCommandPreprocessEvent event)
    {
        if (sendListOfConnectedUsers(event.getPlayer(), event.getMessage()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    void serverCommandEvent(ServerCommandEvent event)
    {
        if (sendListOfConnectedUsers(event.getSender(), event.getCommand()))
            event.setCancelled(true);
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        player.performCommand("jukebox");
        new BukkitRunnable()
        {
            public void run()
            {
                if (!player.isOnline())
                    return;
                if (player.hasMetadata("MD_JUKEBOX"))
                    return;
                tellPlayerToOpenMemeBox(player, false);
            }
        }.runTaskLater(instance, 400L);

    }

    void tellPlayerToOpenMemeBox(Player player, boolean reason)
    {
        if (reason)
            player.sendMessage("Pls open da /memebox 2 hear dis moozik");
        else
            player.sendMessage("Pls open da /memebox 4 a memetastic experience");
        player.performCommand("jukebox");
    }

    //@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerInteractJukebox(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Material disc = event.getMaterial();
        Block block = event.getClickedBlock();

        if (player.isSneaking())
            return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX)
            return;

        if (!hasOpenedMemeBox(player))
        {
            tellPlayerToOpenMemeBox(player, true);
        }

        Jukebox jukebox = (Jukebox)event.getClickedBlock().getState();
        List<MetadataValue> blockMetadata = block.getMetadata("SONG");

        Location loc = block.getLocation();

        //If there's already a disc in here, eject it and stop playing
        if (jukebox.eject())
        {
            //Don't stop sounds if... we didn't start the sound...
            if (!block.hasMetadata("SONG"))
                return;

            String songToStop =  blockMetadata.get(0).asString();
            for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
            {
                if (onlinePlayer.getLocation().distanceSquared(loc) < 10000)
                    player.stopSound(songToStop);
            }
            block.removeMetadata("SONG", instance);
            return;
        }

        if (!disc.isRecord())
            return;

        //Otherwise, let's play a song, yay
        String songToPlay = null;


        switch(disc)
        {
            case GOLD_RECORD:
                songToPlay = "custom-stuff";
                break;
            case GREEN_RECORD:
            case RECORD_3:
            case RECORD_4:
            case RECORD_5:
            case RECORD_6:
            case RECORD_7:
            case RECORD_8:
            case RECORD_9:
            case RECORD_10:
            case RECORD_11:
            case RECORD_12:
                break;
        }

        if (songToPlay == null)
            return;

        block.setMetadata("SONG", new FixedMetadataValue(instance, songToPlay));
        block.getWorld().playSound(loc, songToPlay, 4.0f, 1.0f);
        block.getRelative(event.getBlockFace()).setType(Material.AIR);
    }
}
