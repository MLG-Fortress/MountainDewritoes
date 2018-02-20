package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 2/20/2018.
 *
 * @author RoboMWM
 */
public class ResetCommands implements CommandExecutor
{
    private JavaPlugin plugin;

    public ResetCommands(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage("/reset <thing>\nThings: " + ChatColor.GOLD + "bending");
            return false;
        }

        switch(args[0].toLowerCase())
        {
            case "bending":
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "bending remove " + sender.getName());
                break;
            default:
                return false;
        }

        return true;

    }
}
