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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    private CommandSender name = null;
    private String reason = null;
    private boolean pendingShutdown = false;
    private boolean updateComplete = false;
    private Process updateProcess;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerLeave(PlayerQuitEvent event)
    {
        if (reason == null || pendingShutdown)
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
                shutdown();
            }
        }.runTaskLater(instance, 1L);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length < 1 && sender instanceof Player)
        {
            sender.sendMessage("/restart <reason...>");
            return false;
        }

        String reason = String.join(" ", args);

        if (!sender.hasPermission("mlgstaff"))
        {
            ((Player)sender).kickPlayer(reason); //kek
            return true;
        }

        if (args[0].equalsIgnoreCase("abort") || args[0].equalsIgnoreCase("cancel"))
        {
            abortShutdown();
            sender.sendMessage("Restart aborted.");
            return true;
        }

        if (!sender.isOp())
        {
            for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
            {
                if (!onlinePlayer.hasPermission("mlgstaff"))
                {
                    sender.sendMessage("Hmm, luks lik we hav sum playas on da serbur rite now, but I've scheduled a /restart to occur as soon as they leave. Use " + ChatColor.GOLD + "/restart abort " + ChatColor.RESET + "to cancel.");
                    scheduleShutdown(sender.getName(), reason);
                    return true;
                }
            }
        }
        else if (cmd.getName().equalsIgnoreCase("update"))
        {
            if (update(true))
                sender.sendMessage("Updating plugins...");
            else
                sender.sendMessage("Plugin update already in progress...");
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("restartnow"))
        {
            this.name = sender;
            this.reason = reason;
            this.updateComplete = true;
            this.pendingShutdown = true;
            abortUpdate();
            actuallyShutdown();
            return true;
        }

        sender.sendMessage("Restart process initialized. Will restart as soon as plugins finish updating.");
        scheduleShutdown(sender, reason);
        shutdown();


        return true;
    }


    private void scheduleShutdown(CommandSender name, String reason)
    {
        this.name = name;
        this.reason = reason;
    }

    private boolean shutdown()
    {
        if (pendingShutdown)
            return false;
        pendingShutdown = true;

        instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "broadcast Server restart process initialized. Restart will occur as soon as the plugins finish updating.");
        update(false);
        return true;
    }

    private void abortShutdown()
    {
        this.pendingShutdown = false;
        this.name = null;
        this.reason = null;
    }

    private void abortUpdate()
    {
        if (this.updateProcess != null && this.updateProcess.isAlive())
            this.updateProcess.destroy();
    }

    private boolean update(boolean noShutdown)
    {
        if (updateComplete && !noShutdown)
        {
            actuallyShutdown();
            return true;
        }
        if (updateProcess != null && updateProcess.isAlive())
            return false;
        ProcessBuilder processBuilder = new ProcessBuilder("./updatething.sh");
        processBuilder.directory(instance.getServer().getWorldContainer());
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectErrorStream(true);
        try
        {
            updateProcess = processBuilder.start();
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        BufferedReader output = new BufferedReader(new InputStreamReader(updateProcess.getInputStream()));
                        String outputLine;
                        while ((outputLine = output.readLine()) != null)
                        {
                            name.sendMessage("U: " + outputLine);
                        }
                        name.sendMessage("U: update complete");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(instance);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!updateProcess.isAlive())
                    {
                        cancel();
                        updateComplete = true;
                        updateProcess = null;
                        instance.getServer().broadcastMessage(ChatColor.GRAY + "Update complete.");
                        actuallyShutdown();
                    }
                }
            }.runTaskTimer(instance, 200L, 20L);
        }
        catch (Exception e)
        {
            instance.getLogger().warning("Unable to run updater");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void actuallyShutdown()
    {
        if (!pendingShutdown || reason == null || !updateComplete)
            return;
        pendingShutdown = false;
        for (Player onlinePlayer : instance.getServer().getOnlinePlayers())
        {
            if (name != null)
                onlinePlayer.kickPlayer("Serbur restartin cuz " + name + " sez " + reason);
            else
                onlinePlayer.kickPlayer("Serbur restartin: " + reason);
        }

        //In case some dum plugin freezes the serbur onDisable...
        for (World world : instance.getServer().getWorlds())
            world.save();

        instance.getServer().shutdown();
    }
}
