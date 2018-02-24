package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 6/30/2017.
 *
 * @author RoboMWM
 */
public class StaffRestartCommand implements CommandExecutor
{
    private JavaPlugin instance;

    public StaffRestartCommand(JavaPlugin plugin)
    {
        this.instance = plugin;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            String reason = "";
            if (args.length > 0)
                reason = String.join(" ", args);

            for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
            {
                onlinePlayer.kickPlayer("Serbur restartin: " + reason);
            }

            instance.getServer().savePlayers(); //Probably dumb since I'm kickin 'em anyways

            //In case some dum plugin freezes the serbur onDisable...
            for (World world : instance.getServer().getWorlds())
            {
                world.save();
            }

            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "minecraft:stop");
            return true;
        }

        Player player = (Player)sender;

        if (args.length < 1)
        {
            sender.sendMessage("/restart <reason...>");
            return false;
        }

        String reason = String.join(" ", args);

        if (!sender.hasPermission("mlgstaff"))
        {
            player.kickPlayer(reason); //kek
            return true;
        }

        if (!player.isOp())
        {
            for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
            {
                if (!onlinePlayer.hasPermission("mlgstaff"))
                {
                    player.sendMessage("Hmm, luks lik we hav sum playas on da serbur rite now. Might b best 2 wait until dey leve b4 u /restart meh.");
                    return true;
                }
            }
        }

        String playerName = player.getDisplayName(); //Might not be necessary since we're running in sync but w/e

        for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
        {
            onlinePlayer.kickPlayer("Serbur iz restartin bcuz of " + playerName + ": " + reason);
        }

        instance.getServer().savePlayers(); //Probably dumb since I'm kickin 'em anyways

        //In case some dum plugin freezes the serbur onDisable...
        for (World world : instance.getServer().getWorlds())
        {
            world.save();
        }

        //plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "minecraft:stop");
        instance.getServer().shutdown();
        return true;
    }
}
