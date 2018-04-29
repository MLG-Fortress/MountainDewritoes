package me.robomwm.MountainDewritoes;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2/22/2018.
 *
 * @author RoboMWM
 */
public class AntiLag implements Listener
{
    private Map<Player, Integer> viewDistance = new HashMap<>();

    public AntiLag(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


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
        location.add(-1, -2, -1);

        for (int x = 0; x <= 2; x++) // to x == 1
        {
            for (int y = 0; y <= 4; y++) // to y == 2
            {
                for (int z = 0; z <= 2; z++) // to z == 1
                {
                    Location blockLocation = location.clone().add(x, y, z);
                    Block block = blockLocation.getBlock();
                    player.sendBlockChange(blockLocation, block.getType(), block.getData());
                }
            }
        }
    }
}
