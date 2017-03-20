package me.robomwm.MountainDewritoes;

import me.robomwm.usefulnmsutil.UsefulNMSUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;

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
            UsefulNMSUtil.spawnSourcedTNTPrimed(tnt, event.getPlayer());
            //entity.setMetadata("SOURCE", new FixedMetadataValue(instance, event.getPlayer()));
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    private void onCrackShotExplosion(ExplosionPrimeEvent event)
    {
        if (event.getEntityType() != EntityType.PRIMED_TNT)
            return;
        if (!event.getEntity().hasMetadata("CS_Label"))
            return;

        Player player = null;
        TNTPrimed tnt = (TNTPrimed)event.getEntity();
        if (tnt.hasMetadata("CS_pName"))
            player = instance.getServer().getPlayer(tnt.getMetadata("CS_pName").get(0).asString());
        TNTPrimed betterTNT = UsefulNMSUtil.spawnSourcedTNTPrimed(tnt.getLocation(), player);

        //Copy over metadata
        copyMetadata(tnt, betterTNT, "CS_Label");
        copyMetadata(tnt, betterTNT, "CS_potex");
        copyMetadata(tnt, betterTNT, "C4_Friendly");
        copyMetadata(tnt, betterTNT, "nullify");
        copyMetadata(tnt, betterTNT, "CS_nodam");
        copyMetadata(tnt, betterTNT, "CS_pName");
        copyMetadata(tnt, betterTNT, "CS_ffcheck");
        copyMetadata(tnt, betterTNT, "0wner_nodam");

        //Copy entity attributes
        betterTNT.setYield(tnt.getYield());
        betterTNT.setIsIncendiary(tnt.isIncendiary());
        betterTNT.setFuseTicks(tnt.getFuseTicks());

        //Cancel and remove original
        event.setCancelled(true);
        event.getEntity().remove();
    }

    void copyMetadata(Metadatable from, Metadatable to, String key)
    {
        if (from.hasMetadata(key))
            to.setMetadata(key, new FixedMetadataValue(instance, from.getMetadata(key).get(0).value()));
    }
}
