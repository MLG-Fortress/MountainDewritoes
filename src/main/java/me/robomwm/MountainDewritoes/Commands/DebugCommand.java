package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.Music.MusicThing;
import me.robomwm.MountainDewritoes.SimpleClansListener;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
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
    ClanManager clanManager;

    public DebugCommand()
    {
        clanManager = SimpleClansListener.clanManager;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!sender.isOp())
            return false;

        //1 arg//
        if (args.length < 1)
            return false;

        //2 args//
        if (args.length < 2)
            return false;

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
        if (args[0].equalsIgnoreCase("clankick"))
        {
            Player target = Bukkit.getServer().getPlayerExact(args[1]);
            if (target == null)
                return false;
            clanManager.getClanPlayer(target).getClan().removePlayerFromClan(target.getUniqueId());
            return true;
        }
        return false;
    }
}
