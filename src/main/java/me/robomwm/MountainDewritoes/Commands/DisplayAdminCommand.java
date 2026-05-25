package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisplayAdminCommand implements CommandExecutor {

    private final Map<UUID, UUID> selectedDisplays = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <select|movehere>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "select":
                return handleSelect(player);
            case "movehere":
                return handleMoveHere(player);
            default:
                player.sendMessage(ChatColor.RED + "Unknown argument. Use 'select' or 'movehere'.");
                return true;
        }
    }

    private boolean handleSelect(Player player) {
        double searchRadius = 5.0;
        Display closestDisplay = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (Entity entity : player.getNearbyEntities(searchRadius, searchRadius, searchRadius)) {
            if (entity instanceof Display) {
                double distanceSq = entity.getLocation().distanceSquared(player.getLocation());
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestDisplay = (Display) entity;
                }
            }
        }

        if (closestDisplay == null) {
            player.sendMessage(ChatColor.RED + "No display entities found within " + searchRadius + " blocks.");
            return true;
        }

        selectedDisplays.put(player.getUniqueId(), closestDisplay.getUniqueId());

        String name = closestDisplay.getCustomName() != null ? closestDisplay.getCustomName() : "Unnamed Display";

        player.sendMessage(ChatColor.GREEN + "Selected display entity: " + ChatColor.YELLOW + name);
        return true;
    }

    private boolean handleMoveHere(Player player) {
        UUID displayId = selectedDisplays.get(player.getUniqueId());

        if (displayId == null) {
            player.sendMessage(ChatColor.RED + "You don't have a display entity selected! Use /displayadmin select first.");
            return true;
        }

        Entity targetEntity = player.getServer().getEntity(displayId);

        if (targetEntity == null || !(targetEntity instanceof Display)) {
            player.sendMessage(ChatColor.RED + "The selected display entity no longer exists or is unloaded.");
            selectedDisplays.remove(player.getUniqueId());
            return true;
        }

        targetEntity.teleport(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Moved the selected display entity to your current position.");
        return true;
    }
}