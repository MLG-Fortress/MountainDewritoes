package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

/**
 * Created by RoboMWM on 11/5/2016.
 * Provides jukeboxes to play "additional tracks"
 * Does NOT use AtmosphericManager's metadata system
 * (i.e. retaining vanilla behavior)
 */
public class JukeboxManager implements Listener
{
    MountainDewritoes instance;

    public JukeboxManager(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerInteractJukebox(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Material disc = event.getMaterial();

        if (!disc.isRecord())
            return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX)
            return;

        Jukebox jukebox = (Jukebox)event.getClickedBlock().getState();
        List<MetadataValue> jukeboxMeta = jukebox.getMetadata("SONG");
        Location loc = jukebox.getLocation();

        //If there's already a disc in here, eject it and stop playing
        if (jukebox.eject())
        {
            //Don't stop sounds if... we didn't start the sound...
            if (!jukebox.hasMetadata("SONG"))
                return;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound @a[x=" + loc.getBlockX() + ",y=" + loc.getBlockY() + ",z=" + loc.getBlockZ() + ",r=100] record " + jukeboxMeta.get(0));
            jukebox.removeMetadata("SONG", instance);
            return;
        }

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

        jukebox.setMetadata("SONG", new FixedMetadataValue(instance, songToPlay));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound " + songToPlay + " record @a " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "4");
    }
}
