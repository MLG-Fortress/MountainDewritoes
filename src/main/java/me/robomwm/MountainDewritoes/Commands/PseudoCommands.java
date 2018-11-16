package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.ReflectionHandler;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created on 8/19/2017.
 *
 * @author RoboMWM
 */
public class PseudoCommands implements Listener
{
    private static Method muhHandle;
    private static Field ping;
    private MountainDewritoes instance;
    public PseudoCommands(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        instance = plugin;
        try
        {
            muhHandle = ReflectionHandler.getMethod("CraftPlayer", ReflectionHandler.PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
            ping = ReflectionHandler.getField("EntityPlayer", ReflectionHandler.PackageType.MINECRAFT_SERVER, true, "ping");
        }
        catch (Throwable dum)
        {
            plugin.getLogger().warning("Ping command and etc. won't work.");
        }
    }

    public static String getPing(Player player)
    {
        try
        {
            return Integer.toString(ping.getInt(muhHandle.invoke(player))) + "ms";
        }
        catch (Throwable ignored)
        {
            return "over 9000!ms";
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void processor(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        //Aliases
        switch(message)
        {
            case "/home":
                message = "/clan home";
                break;
        }

        String[] heyo = message.split(" ");
        String command = heyo[0].substring(1).toLowerCase();
        String[] args = new String[heyo.length - 1];
        for (int i = 1; i < heyo.length; i++)
            args[i - 1] = heyo[i].toLowerCase();

        Player player = event.getPlayer();

        switch (command)
        {
            case "bal":
            case "balance":
            case "money":
                event.setCancelled(balanceHandler(player, command, args));
                break;
            case "f":
            case "clan":
                event.setCancelled(clanHandler(player, command, args));
                break;
            case "afk":
            case "brb":
                event.setCancelled(afkSeen(player));
                break;
            case "ping":
                event.setCancelled(ping(player, command, args));
                break;
        }
    }

    //return true to cancel further execution

    private boolean afkSeen(Player player)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.performCommand("seen " + player.getName());
            }
        }.runTask(instance);
        return false;
    }

    private boolean ping(Player player, String command, String[] args)
    {
        player.sendMessage("Your ping: " + getPing(player));
        if (args.length > 1)
        {
            Player target = instance.getServer().getPlayer(args[1]);
            if (target != null)
            {
                player.sendMessage(target.getDisplayName() + "'s ping: " + getPing(target));
                return true;
            }
        }
        return false;
    }

    private boolean balanceHandler(Player player, String command, String[] args)
    {
        player.sendMessage(NSA.getTransactions(player));
        return false;
    }

    private boolean clanHandler(Player player, String command, String[] args)
    {
        if (args.length < 1)
            return false;
        switch (args[0])
        {
            case "home":
                //player.sendMessage("Use /tppoint");
                instance.getSimpleClansListener().teleportHome(player);
                return true;
        }
        if (args.length < 3)
            return false;
        switch (args[0])
        {
            case "create":
                StringBuilder clanName = new StringBuilder();
                for (int i = 2; i < args.length; i++)
                    clanName.append(args[i] + " ");
                clanName.setLength(clanName.length() - 1);
                //Automatically colors the clan tag with the founder's nickname color and makes it uppercase
                String tag = args[1].toUpperCase().replaceAll("&", "");
                player.performCommand("clan create &" + getColorCode(player) + tag + " " + clanName.toString());
                return true;
        }
        return false;
    }

    public char getColorCode(Player player)
    {
        return instance.getGrandioseAPI().getGrandPlayerManager().getGrandPlayer(player).getNameColor().getChar();
//        //Get hash code of player's UUID
//        int colorCode = player.getUniqueId().hashCode();
//        //Ensure number is positive
//        colorCode = Math.abs(colorCode);
//
//        //Will make configurable, hence this
//        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e".split(",");
//        //Divide hash code by length of acceptableColors, and use remainder
//        //to determine which index to use (like a hashtable/map/whatever)
//        colorCode = (colorCode % acceptableColors.length);
//        String stringColorCode = acceptableColors[colorCode];
//
//        return stringColorCode;
    }
}
