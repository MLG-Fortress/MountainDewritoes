package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 12/18/2020.
 *
 * @author RoboMWM
 */
public class MopCommand implements CommandExecutor
{
    private Plugin plugin;

    public MopCommand(JavaPlugin plugin)
    {
        this.plugin = plugin;
        plugin.getCommand("mop").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!sender.hasPermission("mlg.mop"))
            return false;

        if (!sender.hasPermission("*"))
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "lp user " + sender.getName() + " settemp true * 3h");
        else
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "lp user " + sender.getName() + " unsettemp *");

        return true;
    }
}
