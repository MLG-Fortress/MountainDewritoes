package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.MaterialSetTag;
import me.robomwm.MountainDewritoes.Events.ScheduledPlayerMovedEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class TrampleGrass implements Listener
{
    private Plugin plugin;

    public TrampleGrass(Plugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPlayerMove(ScheduledPlayerMovedEvent event)
    {
        if (event.getPlayer().getWorld().getPVP())
            return;
        if (event.getFrom().distanceSquared(event.getTo()) == 0)
            return;
        if (event.getTo().getBlockY() >= 255)
            return;

        Block block = event.getTo().clone().add(0, 1, 0).getBlock();

        switch (block.getType())
        {
            case GRASS:
                block.setType(Material.AIR);
                break;
            //todo: flowers
        }
    }

    //TODO: grow grass
}
