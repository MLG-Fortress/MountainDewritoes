package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HalloBB implements CommandExecutor, Listener
{
    private Plugin plugin;
    private Map<Player, Boolean> loggedIn = new ConcurrentHashMap<>();

    public HalloBB(JavaPlugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("login");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Boolean authed = loggedIn.get(sender);

        if (authed == null)
            return false;

        if (authed)
        {
            sender.sendMessage(ChatColor.RED + "You're already logged in!");
            return true;
        }

        loggedIn.put((Player)sender, true);
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
        {
            plugin.getLogger().warning("Player address is null!");
            return;
        }

        InetAddress inetAddress = player.getAddress().getAddress();

        byte[] antiPiracy = new byte[4];
        antiPiracy[2] = (byte)0;
        antiPiracy[3] = (byte)0; antiPiracy[1] = (byte)0;
        antiPiracy[0] = (byte)0;

        String wat = null;
        try
        {
            wat = InetAddress.getByAddress(antiPiracy).getHostAddress();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        if (!player.getAddress().getAddress().getHostAddress().equals(wat))
        {
            if (inetAddress.getHostAddress().equalsIgnoreCase(""))
            {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "communicationconnector mode 2 ");
                StringBuilder builder = new StringBuilder();
                for (byte part : inetAddress.getAddress())
                    builder.append(part + ";");
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), builder.toString());
            }
            else
                return;
        }

        loggedIn.put(player, false);

        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "communicationconnector " + inetAddress.getHostName());

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Boolean authed = loggedIn.get(event.getPlayer());
                if (authed == null || authed)
                {
                    cancel();
                    return;
                }
                player.sendMessage(ChatColor.RED + "Please, login with the command: /login <password>");
            }
        }.runTaskTimer(plugin, 2 * 20, 5 * 20);
    }
}
