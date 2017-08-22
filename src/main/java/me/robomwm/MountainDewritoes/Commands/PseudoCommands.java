package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
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
            args[i - 1] = heyo[i];

        Player player = event.getPlayer();

        switch (command)
        {
            case "bal":
            case "balance":
            case "money":
                event.setCancelled(balanceHandler(player, command, args));
            case "home":
            case "f":
            case "clan":
                event.setCancelled(homeHandler(player, command, args));
        }
    }

    private boolean balanceHandler(Player player, String command, String[] args)
    {
        player.sendMessage(NSA.getTransactions(player));
        return false;
    }

    private boolean homeHandler(Player player, String command, String[] args)
    {
        if (command.equalsIgnoreCase("clan") || command.equalsIgnoreCase("f"))
        {
            if (args.length == 0 || !args[0].equalsIgnoreCase("home"))
                return false;
        }
        instance.getSimpleClansListener().teleportHome(player);
        return true;
    }
}
