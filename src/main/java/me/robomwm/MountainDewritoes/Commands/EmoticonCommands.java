package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 8/16/2017.
 *
 * @author RoboMWM
 *
 * @Deprecated
 * @see me.robomwm.MountainDewritoes.Emoticons
 */
public class EmoticonCommands implements CommandExecutor
{
    private JavaPlugin instance;

    public EmoticonCommands(JavaPlugin plugin)
    {
        instance = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;
        Player player = (Player)sender;

        if (cmd.getName().equalsIgnoreCase("shrug"))
        {
            chat(player, String.join(" ", args) + " \u00AF\\_(\u30C4)_/\u00AF"); //¯\_(ツ)_/¯
        }

        return true;
    }

    public void chat(Player player, String message)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.chat(message);
            }
        }.runTaskAsynchronously(instance);
    }

}
