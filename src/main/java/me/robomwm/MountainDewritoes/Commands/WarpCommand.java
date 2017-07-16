package me.robomwm.MountainDewritoes.Commands;

import com.google.common.collect.Maps;
import me.robomwm.BetterTPA.BetterTPA;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 3/8/2017.
 *
 * @author RoboMWM
 */
public class WarpCommand implements CommandExecutor
{
    MountainDewritoes instance;
    BetterTPA betterTPA;
    Map<String, Location> warps = new HashMap<>();

    public WarpCommand(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        betterTPA = (BetterTPA)instance.getServer().getPluginManager().getPlugin("BetterTPA");
        addWarp("spawn", "spawn", -389D, 5D, -124D, 180.344f, -18.881f);
        addWarp("mall", "mall", 2.488, 5, -7.305, 0f, 0f);
        addWarp("prison", "prison", -967, 14, 1298, 0f, -8f);
        addWarp("jail", "minigames", -523D, 58.5D, -36D, 88.951f, 26.7f);
    }

    private void addWarp(String warp, String worldName, double x, double y, double z, float yaw, float pitch)
    {
        if (instance.getServer().getWorld(worldName) == null)
            return;
        warps.put(warp, new Location(instance.getServer().getWorld(worldName), x, y, z, yaw, pitch));
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

        //Aliases
        switch (desiredWarp)
        {
            case "hub":
            case "lobby":
                desiredWarp = "spawn";
                break;
        }

        Location location = warps.get(desiredWarp);

        if (location == null)
            sendWarps(player);
        else
            betterTPA.teleportPlayer(player, desiredWarp, location, !instance.isSafeWorld(player.getWorld()), null);
        return true;
    }

    private void sendWarps(Player player)
    {
        player.sendMessage("Warps:");
        StringBuilder lazy = new StringBuilder(ChatColor.GOLD.toString());
        for (String warp : warps.keySet())
        {
            lazy.append(warp);
            lazy.append(", ");
        }
        lazy.setLength(lazy.length() - 2);
        player.sendMessage(lazy.toString());
    }
}