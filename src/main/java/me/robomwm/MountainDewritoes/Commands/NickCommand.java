package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2/20/2017.
 * Yes I realized my package names are capitlized
 * Oh well ¯\_(ツ)_/¯
 * @author RoboMWM
 */
public class NickCommand implements CommandExecutor
{
    String acceptableColors;

    public NickCommand()
    {
        StringBuilder builder = new StringBuilder();
        Set<String> colorThingy = new HashSet<>(Arrays.asList("Aqua", "Blue", "Dark_Blue", "Green", "Dark_Green", "Light_Purple", "Dark_Purple", "Red", "Dark_Red", "Gold", "Yellow"));
        int i = 0;
        for (String ok : colorThingy)
        {
            builder.append(ChatColor.valueOf(ok.toUpperCase()));
            builder.append(ok);
            builder.append(", ");
            if (i++ > 3)
                builder.append("\n");
        }
        acceptableColors = builder.toString().substring(0, builder.length() - 2);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        if (args.length < 1)
        {
            player.sendMessage("/nick <color>");
            player.sendMessage("colors: " + acceptableColors);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("nick"))
        {
            ChatColor color = null;
            try
            {
                color = ChatColor.valueOf(args[0].toUpperCase());
            }
            catch (Exception e)
            {
                color = ChatColor.getByChar(args[0]);
                if (color == null && args[0].length() > 1)
                {
                    color = ChatColor.getByChar(args[0].substring(1));
                }
            }
            if (color == null || isBannedColor(color))
            {
                player.sendMessage("Valid colors: " + acceptableColors);
                return true;
            }
            player.performCommand("enick " + convertColor(color) + player.getName());
            return true;
        }
        return false;
    }

    boolean isBannedColor(ChatColor color)
    {
        if (color.isFormat())
            return true;

        switch (color)
        {
            case BLACK:
            case DARK_GRAY:
            case GRAY:
            case WHITE:
                return true;
        }
        return false;
    }

    String convertColor(ChatColor color)
    {
        switch (color)
        {
            case AQUA:
                return "&b";
            case BLUE:
                return "&9";
            case DARK_AQUA:
                return "&3";
            case DARK_BLUE:
                return "&1";
            case DARK_GREEN:
                return "&2";
            case DARK_PURPLE:
                return "&5";
            case DARK_RED:
                return "&4";
            case GOLD:
                return "&6";
            case GREEN:
                return "&a";
            case LIGHT_PURPLE:
                return "&d";
            case RED:
                return "&c";
            case YELLOW:
                return "&e";
        }
        return null;
    }
}
