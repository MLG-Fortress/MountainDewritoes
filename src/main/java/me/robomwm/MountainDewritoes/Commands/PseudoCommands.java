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
        String[] heyo = event.getMessage().split(" ");
        String command = heyo[0].substring(1).toLowerCase();
        String[] args = new String[heyo.length - 1];
        for (int i = 1; i < heyo.length; i++)
            args[i - 1] = heyo[i].toLowerCase();

        Player player = event.getPlayer();

        //Aliases
        switch(command)
        {
            case "home":
                command = "clan home";
                break;
        }

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
        }
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
                player.performCommand("clan create " + ChatColor.getByChar(instance.getSimpleClansListener().getColorCode(player)) + ChatColor.stripColor(args[1].toUpperCase()) + " " + clanName.toString());
                return true;
        }
        return false;
    }
}
