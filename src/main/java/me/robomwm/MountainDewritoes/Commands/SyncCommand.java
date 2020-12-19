package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created on 12/18/2020.
 *
 * @author RoboMWM
 */
public class SyncCommand implements CommandExecutor
{
    private Plugin plugin;
    private Process syncProcess;

    public SyncCommand(JavaPlugin plugin)
    {
        this.plugin = plugin;
        plugin.getCommand("syncconfigs").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        ProcessBuilder processBuilder = new ProcessBuilder("./syncthing.sh");
        processBuilder.directory(plugin.getServer().getWorldContainer());
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectErrorStream(true);
        try
        {
            syncProcess = processBuilder.start();
            sender.sendMessage("Plugin config sync script started...");
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        BufferedReader output = new BufferedReader(new InputStreamReader(syncProcess.getInputStream()));
                        String outputLine;
                        while ((outputLine = output.readLine()) != null)
                        {
                            plugin.getLogger().info(outputLine);
                        }
                        sender.sendMessage("Sync complete. If a new commit was not pushed, that means something went wrong and you'll need to check logs for the reason why.");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(plugin);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!syncProcess.isAlive())
                    {
                        cancel();
                        syncProcess = null;
                    }
                }
            }.runTaskTimer(plugin, 200L, 20L);
        }
        catch (Exception e)
        {
            plugin.getLogger().warning("Unable to run sync script?");
            sender.sendMessage("Was unable to start sync script for some reason");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
