package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Created on 1/8/2018.
 *
 * @author RoboMWM
 */
public class ViewDistanceCommand implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length < 1)
            return false;
        Player player = (Player)sender; //I shouldn't be dum enuf 2 do dis at console k
        int distance;
        try
        {
            distance = Integer.parseInt(args[0]);
        }
        catch (Throwable rock)
        {
            return false;
        }
        if (distance < 3 || distance > 16) //Pretty sure even at 16 chunks out you ain't gonna see anything. Unless you're a MC professional photographer
            return false;
        player.setViewDistance(distance);
        player.sendMessage("Set view distance to " + distance + " chunk radius.");
        return true;
    }
}
