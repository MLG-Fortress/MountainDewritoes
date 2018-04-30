package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 6/30/2017.
 *
 * @author RoboMWM
 */
public class StaffRestartCommand implements CommandExecutor, Listener
{
    private JavaPlugin instance;

    public StaffRestartCommand(JavaPlugin plugin)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private String name = null;
    private String scheduledRestart = null;
    private boolean pendingShutdown = false;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerLeave(PlayerQuitEvent event)
    {
        if (scheduledRestart == null)
            return;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
                {
                    if (!onlinePlayer.hasPermission("mlgstaff"))
                        return;
                }
                shutdown(name, scheduledRestart);
            }
        }.runTaskLater(instance, 1L);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            String reason = "";
            if (args.length > 0)
                reason = String.join(" ", args);
            if (cmd.getName().equalsIgnoreCase("schedulerestart"))
            {
                this.scheduledRestart = reason;
                sender.sendMessage("Restart scheduled");
            }
            else
                shutdown(null, reason);
            return true;
        }

        Player player = (Player)sender;

        if (args.length < 1)
        {
            sender.sendMessage("/restart <reason...>");
            return false;
        }
        if (args[0].equalsIgnoreCase("cancel"))
        {
            name = null;
            scheduledRestart = null;
            sender.sendMessage("Canceled scheduled restart.");
            return true;
        }

        String reason = String.join(" ", args);

        if (!sender.hasPermission("mlgstaff"))
        {
            player.kickPlayer(reason); //kek
            return true;
        }

        if (!player.isOp())
        {
            for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
            {
                if (!onlinePlayer.hasPermission("mlgstaff"))
                {
                    player.sendMessage("Hmm, luks lik we hav sum playas on da serbur rite now, but I've scheduled a /restart to occur as soon as they leave. Use " + ChatColor.GOLD + "/restart cancel " + ChatColor.RESET + "to cancel.");
                    scheduledRestart = reason;
                    name = player.getDisplayName();
                    return true;
                }
            }
        }

        shutdown(player.getDisplayName(), reason);


        return true;
    }

    private void shutdown(String playerName, String reason)
    {
        if (pendingShutdown)
            return;
        pendingShutdown = true;
        ProcessBuilder processBuilder = new ProcessBuilder("./updatething.sh");
        processBuilder.directory(instance.getServer().getWorldContainer());
        Process process;
        try
        {
            process = processBuilder.start();
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!process.isAlive())
                    {
                        cancel();
                        actuallyShutdown(playerName, reason);
                    }
                }
            }.runTaskTimer(instance, 200L, 20L);
        }
        catch (Exception e)
        {
            instance.getLogger().warning("Unable to run updater");
            e.printStackTrace();
            actuallyShutdown(playerName, reason);
        }
        instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "broadcast Server restart process initialized. Restart will occur shortly after plugin updates have been compiled.");
    }

    private void actuallyShutdown(String playerName, String reason)
    {
        for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
        {
            if (playerName != null)
                onlinePlayer.kickPlayer("Serbur restartin cuz " + playerName + " sez " + reason);
            else
                onlinePlayer.kickPlayer("Serbur restartin: " + reason);
        }

        instance.getServer().savePlayers(); //Probably dumb since I'm kickin 'em anyways

        //In case some dum plugin freezes the serbur onDisable...
        for (World world : instance.getServer().getWorlds())
        {
            world.save();
        }

        //plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "minecraft:stop");
        instance.getServer().shutdown();
    }
}
