package me.robomwm.MountainDewritoes.adminai;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminAiCommand implements CommandExecutor
{
    private final AdminAiService adminAiService;

    public AdminAiCommand(MountainDewritoes plugin)
    {
        adminAiService = new AdminAiService(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!sender.hasPermission("mlg.admin"))
            return false;

        if (args.length == 0 || args[0].equalsIgnoreCase("status"))
        {
            sender.sendMessage(ChatColor.GOLD + "Admin AI: " + adminAiService.getStatus());
            return true;
        }

        switch (args[0].toLowerCase())
        {
            case "on":
            case "enable":
                adminAiService.setEnabled(true);
                sender.sendMessage(ChatColor.GREEN + "Admin AI enabled.");
                return true;
            case "off":
            case "disable":
                adminAiService.setEnabled(false);
                sender.sendMessage(ChatColor.RED + "Admin AI disabled. Running task aborted.");
                return true;
            case "abort":
            case "cancel":
                adminAiService.abortCurrentTask();
                sender.sendMessage(ChatColor.RED + "Admin AI task aborted.");
                return true;
            case "reload":
                adminAiService.reload();
                sender.sendMessage(ChatColor.GREEN + "Admin AI config reloaded.");
                return true;
            case "run":
                if (args.length < 2)
                    return false;
                adminAiService.run(sender, StringUtils.join(args, " ", 1, args.length));
                return true;
            default:
                return false;
        }
    }

    public void shutdown()
    {
        adminAiService.shutdown();
    }
}
