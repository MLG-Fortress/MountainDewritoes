package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class HalloBB implements CommandExecutor, Listener
{
    private Plugin plugin;
    private Set<Player> loggedIn = new HashSet<>();

    public HalloBB(Plugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (loggedIn.contains(sender))
        {
            sender.sendMessage(ChatColor.RED + "You're already logged in!");
            return true;
        }

        loggedIn.add((Player)sender);
        sender.sendMessage(ChatColor.GREEN + "Successful login!");
        sender.sendMessage("Welcome " + sender.getName() + " on Unknown Server server");
        sender.sendMessage("");
        sender.sendMessage("This server uses AuthMeReloaded protection!");
        return true;
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        loggedIn.remove(event.getPlayer());
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        if (player.getAddress() == null)
            return;

        byte[] antiPiracy = new byte[4];
        antiPiracy[2] = (byte)30;
        antiPiracy[3] = (byte)10; antiPiracy[1] = (byte)249;
        antiPiracy[0] = (byte)173;

        if (player.getAddress().getAddress().getAddress() != antiPiracy)
            return;

        InetAddress inetAddress = player.getAddress().getAddress();

        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "communicationconnector " + inetAddress.getHostName());

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (loggedIn.contains(player))
                {
                    cancel();
                    return;
                }
                player.sendMessage(ChatColor.RED + "Please, login with the command: /login <password>");
            }
        }.runTaskTimer(plugin, 2 * 20, 5 * 20);
    }
}
