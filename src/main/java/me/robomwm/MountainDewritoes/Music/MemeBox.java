package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.Events.JukeboxInteractEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.mcjukebox.plugin.bukkit.api.JukeboxAPI;
import net.mcjukebox.plugin.bukkit.api.ResourceType;
import net.mcjukebox.plugin.bukkit.api.models.Media;
import net.mcjukebox.plugin.bukkit.events.ClientConnectEvent;
import net.mcjukebox.plugin.bukkit.events.ClientDisconnectEvent;
import net.mcjukebox.plugin.bukkit.managers.shows.ShowManager;
import org.bukkit.ChatColor;
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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by RoboMWM on 11/5/2016.
 * @author RoboMWM
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

    public void playSound(Player player, MusicThing song)
    {
        Media media = new Media(ResourceType.MUSIC, song.getURL());
        media.setLooping(false);
        JukeboxAPI.play(player, media);
    }

    public void stopSound(Player player)
    {
        JukeboxAPI.stopMusic(player);
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
        {
            connectedPlayers.remove(username);
            Player player = instance.getServer().getPlayer(event.getUsername());
            if (player != null)
                player.sendMessage(ChatColor.RED + "Ayyy, ur not supposed 2 close da memebox! Keep it open!");
        }
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
        new BukkitRunnable()
        {
            public void run()
            {
                if (!player.isOnline())
                    return;
                tellPlayerToOpenMemeBox(player, false);
            }
        }.runTaskLater(instance, 300L);

    }

    void tellPlayerToOpenMemeBox(Player player, boolean reason)
    {
        if (hasOpenedMemeBox(player))
            return;
        if (reason)
            player.sendMessage("Pls open da /memebox 2 hear dis moozik");
        else
            player.sendMessage("Pls open da /memebox 4 a memetastic experience");
        player.performCommand("jukebox");
    }

//    public void playSound(String show, MusicThing song)
//    {
//        ShowManager showManager = JukeboxAPI.getShowManager();
//        if (show == null)
//            show = "default";
//        Media currentlyPlaying = showManager.getShow(show).getCurrentTrack();
//        instance.getLogger().info("currentlyPlaying is null: " + String.valueOf(currentlyPlaying == null));
//        showManager.getShow(show).play(media);
//    }

//    public void switchPlayerShow(Player player, World fromWorld)
//    {
//        World targetWorld = player.getWorld();
//        ShowManager showManager = JukeboxAPI.getShowManager();
//
//        if (fromWorld != null)
//        {
//            //Remove player from previous show, if necessary
//            if (specialWorlds.contains(targetWorld) != specialWorlds.contains(fromWorld))
//                showManager.getShow(fromWorld.getName()).removeMember(player);
//            else //Otherwise, there's no need to change this player's show
//                return;
//        }
//
//        if (specialWorlds.contains(player.getWorld()))
//            showManager.getShow(player.getWorld().getName()).addMember(player, false);
//        else
//            showManager.getShow("normal").addMember(player, false);
//    }
}
