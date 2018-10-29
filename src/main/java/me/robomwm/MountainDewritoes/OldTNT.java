package me.robomwm.MountainDewritoes;

import com.robomwm.usefulutil.UsefulUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.Plugin;

/**
 * Created on 10/29/2018.
 *
 * But actually Created on 3/20/2017.
 *
 * Adds a source to plugin-generated TNT
 *
 * Current plugins supported: Crackshot
 *
 * Also adds classic TNT behavior
 *
 * @author RoboMWM
 */
public class OldTNT implements Listener
{
    private Plugin plugin;

    public OldTNT(Plugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    //Back in my day, punching TNT ignited TNT you youngens
    //Used with some inspiration from CTBPunchTNT, which seems to have disappeared?
    @EventHandler(ignoreCancelled = true)
    public void onPunch(BlockBreakEvent event)
    {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        if (event.getBlock().getType() == Material.TNT)
        {
            Location tnt = event.getBlock().getLocation();
            tnt.setX(tnt.getX() + 0.5D);
            tnt.setY(tnt.getY() + 0.6D);
            tnt.setZ(tnt.getZ() + 0.5D);

            UsefulUtil.tntSetSource(tnt.getWorld().spawn(tnt, TNTPrimed.class), event.getPlayer());
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

        Player player;
        TNTPrimed tnt = (TNTPrimed)event.getEntity();
        if (tnt.hasMetadata("CS_pName"))
            player = plugin.getServer().getPlayer(tnt.getMetadata("CS_pName").get(0).asString());
        else
            return;
        UsefulUtil.tntSetSource(tnt, player);
    }
}
