package me.robomwm.MountainDewritoes.Commands;

import com.destroystokyo.paper.Title;
import info.gomeow.chester.Chester;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.Music.MusicThing;
import me.robomwm.MountainDewritoes.SimpleClansListener;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/26/2017.
 *
 * @author RoboMWM
 */
public class DebugCommand implements CommandExecutor
{
    private MountainDewritoes plugin;
    private ClanManager clanManager;
    private static boolean debug;
    private BukkitTask talking = null;

    public DebugCommand(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        clanManager = SimpleClansListener.clanManager;
    }

    public static void debug(Object ya)
    {
        if (debug)
        {
            StackTraceElement e = Thread.currentThread().getStackTrace()[2];
            System.out.println(e.getClassName() + "#" + e.getMethodName() + "@" + e.getLineNumber() + ":" + ya);
        }
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
        {
            debug = !debug;
            sender.sendMessage(String.valueOf(debug));
            return true;
        }

        switch(args[0].toLowerCase())
        {
            case "botconvo":
                if (talking != null)
                {
                    talking.cancel();
                    return true;
                }
                talking = new BukkitRunnable()
                {
                    Chester chester = (Chester)plugin.getServer().getPluginManager().getPlugin("Chester");
                    String response = "hello";
                    int count = ThreadLocalRandom.current().nextInt(5, 10);
                    int delay = 2;
                    int convos = 1;

                    @Override
                    public void run()
                    {
                        if (delay-- > 0)
                            return;
                        if (count-- <= 0)
                        {
                            count = ThreadLocalRandom.current().nextInt(5, 15);
                            delay = ThreadLocalRandom.current().nextInt(30, 60 + (1000 * (int)Math.log(convos)));
                            convos++;
                            return;
                        }

                        response = chester.getHal().getSentence(response);
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "communicationconnector " + convos + "_Dum_B0Ts: " + response);
                        String[] messageArray = response.split(" ");
                        if (messageArray.length > 1)
                            response = messageArray[ThreadLocalRandom.current().nextInt(messageArray.length)];
                        delay = ThreadLocalRandom.current().nextInt(4, 10);
                    }
                }.runTaskTimer(plugin, 1L, 20L);
                return true;
            case "refreshBlocks":
                assert player != null;
                Location location = player.getLocation();
                location.add(-1, -2, -1);

                for (int x = 0; x <= 2; x++) // to x == 1
                {
                    location.add(x, 0, 0);
                    for (int y = 0; y <= 4; y++) // to y == 2
                    {
                        location.add(0, y, 0);
                        for (int z = 0; z <= 2; z++) // to z == 1
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
//            World world = plugin.getServer().getWorld(args[1]);
//            if (world == null || !plugin.isSurvivalWorld(world))
//                return false;
//
//            BorderData borderData = com.wimbli.WorldBorder.WorldBorder.plugin.getWorldBorder(world.getName());
//            WorldBorder border = world.getWorldBorder();
//
//            border.setCenter(new Location(world, 0, 0, 0));
//            border.setWarningDistance(0);
//            border.setSize((borderData.getRadiusX() * 2) - 20);
//
//            sender.sendMessage(world.getName());
//            sender.sendMessage("Center: " + border.getCenter().toString() + "\nSize: " + border.getSize());
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
            Player target = plugin.getServer().getPlayerExact(args[0]);
            if (target == null || target.hasPermission("i.am.jailed"))
                return false;
            commander("jail " + args[0] + " " + args[1]);
            commander("lp user " + args[0] + " parent set scrub");
            commander("lp user " + args[0] + " set i.am.jailed");
            commander("communicationconnector Da loominarty caught " + args[0] + " for " + args[1]);
            Title.Builder title = new Title.Builder();
            title.title(ChatColor.DARK_GREEN + "DE_LOOMINARTY");
            title.subtitle(ChatColor.GREEN + "CAPTURED U");
            title.stay(100);
            title.fadeOut(100);
            player.sendTitle(title.build());
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    target.sendMessage("o nu, da loominarty caught u!\nBut u can try 2 scare em away by watching an " + ChatColor.GOLD + "/ad");
                    target.performCommand("ad");
                }
            }.runTaskLater(plugin, 200L);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("watchwinreward"))
        {
            commander("unjail " + args[0]);
            commander("lp user " + args[0] + " unset i.am.jailed");
            args[0] = null;
            commander(StringUtils.join(args, " ").substring(1));
            return true;
        }
        return false;
    }

    private void commander(String thing)
    {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), thing);
    }
}
