package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;

/**
 * Created on 8/16/2017.
 *
 * @author RoboMWM
 */
public class EmoticonCommands implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;
        Player player = (Player)sender;

        if (cmd.getName().equalsIgnoreCase("shrug"))
        {
            player.chat(String.join("", args) + " \u00AF\\_(\u30C4)_/\u00AF"); //¯\_(ツ)_/¯
        }

        return true;
    }
}
