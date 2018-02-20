package me.robomwm.MountainDewritoes.Commands;

import com.destroystokyo.paper.Title;
import com.wimbli.WorldBorder.BorderData;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.Music.MusicThing;
import me.robomwm.MountainDewritoes.SimpleClansListener;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
            return true;
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
        else if (args[0].equalsIgnoreCase("music"))
        {
            Player target = Bukkit.getServer().getPlayerExact(args[1]);
            if (target == null)
                return false;

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
            Player target = Bukkit.getServer().getPlayerExact(args[1]);
            if (target == null)
                return false;
            clanManager.getClanPlayer(target).getClan().removePlayerFromClan(target.getUniqueId());
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("lejail"))
        {
            Player target = instance.getServer().getPlayerExact(args[0]);
            if (target.hasPermission("i.am.jailed"))
                return false;
            commander("jail " + args[0] + args[1]);
            commander("lp user " + args[0] + " parent set default");
            commander("lp user " + args[0] + " set i.am.jailed");
            commander("communicationconnector Da loominarty caught " + args[0] + " for " + args[1]);
            Title.Builder title = new Title.Builder();
            title.title(ChatColor.DARK_GREEN + "DE_LOOMINARTY");
            title.subtitle(ChatColor.GREEN + "CAPTURED U");
            title.stay(100);
            title.fadeOut(100);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    target.sendMessage("o nu, da loominarty caught u!\nBut u can try 2 scare em away by watching an " + ChatColor.GOLD + "/ad");
                    target.performCommand("ad");
                }
            }.runTaskLater(instance, 200L);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("watchwinreward"))
        {
            commander("unjail " + args[0]);
            commander("lp user " + args[0] + " unset i.am.jailed");
            args[0] = null;
            commander(StringUtils.join(args, " "));
            return true;
        }
        return false;
    }

    private void commander(String thing)
    {
        instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), thing);
    }
}
