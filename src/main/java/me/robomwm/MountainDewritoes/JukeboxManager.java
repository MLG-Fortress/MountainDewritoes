package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachment;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
        Block block = event.getClickedBlock();

        if (event.getClickedBlock().getType() != Material.JUKEBOX)
            return;

        Jukebox jukebox = (Jukebox)event.getClickedBlock().getState();
        List<MetadataValue> blockMetadata = block.getMetadata("SONG");

        Location loc = block.getLocation();

        //If there's already a disc in here, eject it and stop playing
        if (jukebox.eject())
        {
            //Don't stop sounds if... we didn't start the sound...
            if (!block.hasMetadata("SONG"))
                return;
            instance.getLogger().info("stopsound @a[x=" + loc.getBlockX() + ",y=" + loc.getBlockY() + ",z=" + loc.getBlockZ() + ",r=100] record " + blockMetadata.get(0).asString());
            String dumSecurity = String.valueOf(ThreadLocalRandom.current().nextInt());
            player.setMetadata(dumSecurity, new FixedMetadataValue(instance, "stopsound @a[x=" + loc.getBlockX() + ",y=" + loc.getBlockY() + ",z=" + loc.getBlockZ() + ",r=100] record " + blockMetadata.get(0).asString()));
            player.chat("/whydoueventrym9 " + dumSecurity); //TODO: does this trigger vanilla anti-spam?
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

        String dumSecurity = String.valueOf(ThreadLocalRandom.current().nextInt());
        block.setMetadata("SONG", new FixedMetadataValue(instance, songToPlay));
        player.setMetadata(dumSecurity, new FixedMetadataValue(instance, "playsound " + songToPlay + " record @a " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " 4"));
        player.chat("/whydoueventrym9 " + dumSecurity); //TODO: does this trigger vanilla anti-spam?
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void yRUHere(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        String message = event.getMessage();
        int i = message.indexOf(" ");
        if (i < 0)
            return;
        if (message.substring(0, i).equals("whydoueventrym9"))
            if (player.hasMetadata(message.substring(++i)))
            {
                PermissionAttachment attachment = player.addAttachment(instance);
                attachment.setPermission("minecraft.command.playsound", true);
                attachment.setPermission("minecraft.command.stopsound", true);
                instance.getLogger().info(player.getMetadata(message.substring(i)).get(0).asString());
                player.performCommand(player.getMetadata(message.substring(i)).get(0).asString());
                player.removeMetadata(message.substring(i), instance);
                player.removeAttachment(attachment);
                event.setCancelled(true);
            }
    }
}
