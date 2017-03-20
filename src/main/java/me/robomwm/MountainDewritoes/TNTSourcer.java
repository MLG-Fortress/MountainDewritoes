package me.robomwm.MountainDewritoes;

import me.robomwm.usefulutil.UsefulUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 3/20/2017.
 *
 * Adds a source to plugin-generated TNT
 *
 * @author RoboMWM
 */
public class TNTSourcer implements Listener
{
    MountainDewritoes instance;

    public TNTSourcer(MountainDewritoes mountainDewritoes)
    {
        this.instance = mountainDewritoes;
        instance.registerListener(this);
    }

    private void cleanup(Metadatable thing)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                thing.removeMetadata("SOURCE", instance);
            }
        }.runTaskLater(instance, 20L); //in case some plugins want to do delayed stuff I guess...
    }

    //Back in my day, punching TNT ignited TNT you youngens
    //Used with some inspiration from CTBPunchTNT, which seems to have disappeared?
    @EventHandler(ignoreCancelled = true)
    public void onPunch(BlockBreakEvent event)
    {
        if (event.getBlock().getType() == Material.TNT)
        {
            Location tnt = event.getBlock().getLocation();
            tnt.setX(tnt.getX() + 0.5D);
            tnt.setY(tnt.getY() + 0.6D);
            tnt.setZ(tnt.getZ() + 0.5D);
            UsefulUtil.spawnSourcedTNTPrimed(tnt, event.getPlayer());
            //entity.setMetadata("SOURCE", new FixedMetadataValue(instance, event.getPlayer()));
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (event.getEntity().hasMetadata("SOURCE"))
            event.getEntity().removeMetadata("SOURCE", instance);
    }
}
