package me.robomwm.MountainDewritoes;

import me.robomwm.BetterTPA.BetterTPA;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Created on 2/22/2017.
 *
 * @author RoboMWM
 */
public class CommandOverriders implements Listener
{
    MountainDewritoes instance;
    BetterTPA betterTPA;
    Location spawnLocation;
    Location mallLocation;
    public CommandOverriders(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        betterTPA = (BetterTPA)instance.getServer().getPluginManager().getPlugin("BetterTPA");
        spawnLocation = new Location(instance.getServer().getWorld("minigames"), -389D, 5D, -124D, 180.344f, -18.881f);
        mallLocation = new Location(instance.getServer().getWorld("mall"), 2.488, 5, -7.305, 0f, 0f);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onWantToTeleportToSpawnOrMall(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        String[] message = event.getMessage().split(" ");
        String command = message[0].toLowerCase();
        if (command.equals("/warp") && message.length > 1)
            command = message[1].toLowerCase();
        else
            command = command.substring(1, command.length());

        boolean warmup = player.getWorld() != spawnLocation.getWorld() && player.getWorld() != mallLocation.getWorld();
        event.setCancelled(true);
        switch (command)
        {
            case "spawn":
            case "hub":
            case "lobby":
                betterTPA.teleportPlayer(player, "spawn", spawnLocation, warmup, null);
                break;
            case "mall":
                betterTPA.teleportPlayer(player, "mall", mallLocation, warmup, null);
                break;
            default:
                event.setCancelled(false);
                break;
        }
    }
}
