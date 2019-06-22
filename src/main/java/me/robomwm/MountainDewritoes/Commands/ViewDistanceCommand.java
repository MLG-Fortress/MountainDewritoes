package me.robomwm.MountainDewritoes.Commands;

import com.robomwm.grandioseapi.player.GrandPlayer;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 1/8/2018.
 *
 * @author RoboMWM
 */
public class ViewDistanceCommand implements CommandExecutor
{
    private MountainDewritoes plugin;

    public ViewDistanceCommand(MountainDewritoes plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        sender.sendMessage("server view distance cannot be changed at this time. Consider changing render distance in your video settings instead.");
        return true;
//        if (args.length < 1)
//            return false;
//        Player player = (Player)sender; //I shouldn't be dum enuf 2 do dis at console k
//        int distance;
//        try
//        {
//            distance = Integer.parseInt(args[0]);
//        }
//        catch (Throwable rock)
//        {
//            return false;
//        }
//        if (distance < 3 || distance > 16) //Pretty sure even at 16 chunks out you ain't gonna see anything. Unless you're a MC professional photographer
//            return false;
//        player.setViewDistance(distance);
//        player.sendMessage("Set view distance to " + distance + " chunks.");
//        player.sendMessage("Check Options > Video Settings > Render Distance \nSet it to " + distance + " or higher.");
//        GrandPlayer grandPlayer = plugin.getGrandioseAPI().getGrandPlayerManager().getGrandPlayer(player);
//        grandPlayer.getYaml().set("viewDistance", distance);
//        return true;
    }
}
