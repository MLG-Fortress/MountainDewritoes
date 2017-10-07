package me.robomwm.MountainDewritoes.Commands;

import com.wimbli.WorldBorder.BorderData;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.Music.MusicThing;
import me.robomwm.MountainDewritoes.SimpleClansListener;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 7/26/2017.
 *
 * @author RoboMWM
 */
public class DebugCommand implements CommandExecutor
{
    MountainDewritoes instance;
    ClanManager clanManager;

    public DebugCommand(MountainDewritoes plugin)
    {
        instance = plugin;
        clanManager = SimpleClansListener.clanManager;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!sender.isOp())
            return false;
        Player player = null;
        if (sender instanceof Player)
            player = (Player)sender;

        //1 arg//
        if (args.length < 1)
            return false;

        if (args[0].equalsIgnoreCase("reloadBlocks"))
        {
            assert player != null;
            Location location = player.getLocation();

            for (int x = 0; x <= 10; x++)
            {
                location.add(x, 0, 0);
                for (int y = 0; y <= 10; y++)
                {
                    location.add(0, y, 0);
                    for (int z = 0; z <= 10; z++)
                    {
                        location.add(0, 0, z);
                        Block block = location.getBlock();
                        player.sendBlockChange(location, block.getType(), block.getData());
                    }
                }
            }
        }

        //2 args//
        if (args.length < 2)
            return false;

        if (args[0].equalsIgnoreCase("wb"))
        {
            World world = instance.getServer().getWorld(args[1]);
            if (world == null || !instance.isSurvivalWorld(world))
                return false;

            BorderData borderData = com.wimbli.WorldBorder.WorldBorder.plugin.getWorldBorder(world.getName());
            WorldBorder border = world.getWorldBorder();

            border.setCenter(new Location(world, 0, 0, 0));
            border.setWarningDistance(0);
            border.setSize((borderData.getRadiusX() * 2) - 20);

            sender.sendMessage(world.getName());
            sender.sendMessage("Center: " + border.getCenter().toString() + "\nSize: " + border.getSize());
            return true;
        }

        //2nd arg as player//
        Player target = Bukkit.getServer().getPlayerExact(args[1]);
        if (target == null)
            return false;

        if (args[0].equalsIgnoreCase("music"))
        {

            if (!target.hasMetadata("MD_LISTENING"))
            {
                sender.sendMessage("none");
                return true;
            }
            else
            {
                MusicThing song = (MusicThing)target.getMetadata("MD_LISTENING").get(0).value();
                sender.sendMessage("Name: " + song.getSoundName());
                sender.sendMessage("URL: " + song.getURL());
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("clankick"))
        {
            clanManager.getClanPlayer(target).getClan().removePlayerFromClan(target.getUniqueId());
            return true;
        }
        return false;
    }
}
