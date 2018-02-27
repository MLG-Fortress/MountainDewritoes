package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created on 2/27/2018.
 *
 * @author RoboMWM
 */
public class ClearChatCommand implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        for (int i = 0; i < 40; i++)
            sender.sendMessage(" ");
        return true;
    }
}
