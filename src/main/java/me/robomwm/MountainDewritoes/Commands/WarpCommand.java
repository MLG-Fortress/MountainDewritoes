package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.BetterTPA.BetterTPA;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 3/8/2017.
 *
 * @author RoboMWM
 */
public class WarpCommand implements CommandExecutor
{
    MountainDewritoes instance;
    BetterTPA betterTPA;
    Location spawnLocation;
    Location mallLocation;
    Location jail;

    public WarpCommand(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        betterTPA = (BetterTPA)instance.getServer().getPluginManager().getPlugin("BetterTPA");
        spawnLocation = new Location(instance.getServer().getWorld("minigames"), -389D, 5D, -124D, 180.344f, -18.881f);
        mallLocation = new Location(instance.getServer().getWorld("mall"), 2.488, 5, -7.305, 0f, 0f);
        jail = new Location(instance.getServer().getWorld("minigames"), -523D, 58.5D, -36D, 88.951f, 26.7f);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        if (args.length < 1)
        {
            sendWarps(player);
            return true;
        }

        String desiredWarp = args[0].toLowerCase();

        boolean warmup = player.getWorld() != spawnLocation.getWorld() && player.getWorld() != mallLocation.getWorld();

        switch (desiredWarp)
        {
            case "spawn":
            case "hub":
            case "lobby":
                betterTPA.teleportPlayer(player, "spawn", spawnLocation, warmup, null);
                break;
            case "mall":
                betterTPA.teleportPlayer(player, "mall", mallLocation, warmup, null);
                break;
            case "jail":
                betterTPA.teleportPlayer(player, "jail", jail, warmup, null);
                break;
            default:
                sendWarps(player);
        }
        return true;
    }

    private void sendWarps(Player player)
    {
        player.sendMessage("Warps:");
        StringBuilder lazy = new StringBuilder(ChatColor.GOLD.toString());
        lazy.append("spawn, ");
        lazy.append("mall, ");
        lazy.append("jail");
        player.sendMessage(lazy.toString());
    }
}
