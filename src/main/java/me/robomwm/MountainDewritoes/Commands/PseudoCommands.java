package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Created on 8/19/2017.
 *
 * @author RoboMWM
 */
public class PseudoCommands implements Listener
{
    MountainDewritoes instance;
    public PseudoCommands(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        instance = plugin;
    }

    @EventHandler
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
            case "irc":
                event.setCancelled(ircLink(player, command, args));
                break;
        }
    }

    private boolean ircLink(Player player, String command, String[] args)
    {
        if (!player.isOp())
            return false;
        if (args.length > 0 && args[0].equals("list"))
            return false;
        player.performCommand("map");
        return true;
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
                //Automatically colors the clan tag and makes it uppercase
                String tag = args[1].toUpperCase().replaceAll("&", "");
                player.performCommand("clan create &" + getColorCode(player) + tag + " " + clanName.toString());
                return true;
        }
        return false;
    }

    public String getColorCode(Player player)
    {
        //Get hash code of player's UUID
        int colorCode = player.getUniqueId().hashCode();
        //Ensure number is positive
        colorCode = Math.abs(colorCode);

        //Will make configurable, hence this
        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e".split(",");
        //Divide hash code by length of acceptableColors, and use remainder
        //to determine which index to use (like a hashtable/map/whatever)
        colorCode = (colorCode % acceptableColors.length);
        String stringColorCode = acceptableColors[colorCode];

        return stringColorCode;
    }
}
