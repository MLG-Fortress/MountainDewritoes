package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.persistence.PersistentDataType;

public class SpaceshipCommand implements CommandExecutor
{
    private MountainDewritoes plugin;
    private NamespacedKey flightModelKey;

    public SpaceshipCommand(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        this.flightModelKey = new NamespacedKey(plugin, "flight_model");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        if (!player.isOp())
            return false;

        if (args.length < 1)
        {
            player.sendMessage(ChatColor.RED + "/spaceship <spaceship|airplane|tag>");
            return true;
        }

        String type = args[0].toLowerCase();

        if (type.equals("tag"))
        {
            if (args.length < 2)
            {
                player.sendMessage(ChatColor.RED + "/spaceship tag <spaceship|airplane>");
                return true;
            }
            if (!player.isInsideVehicle())
            {
                player.sendMessage(ChatColor.RED + "You must be inside a vehicle to tag it.");
                return true;
            }
            Vehicle vehicle = (Vehicle)player.getVehicle();
            String flightModel = args[1].equalsIgnoreCase("airplane") ? "Airplane" : "Spaceship";
            vehicle.getPersistentDataContainer().set(flightModelKey, PersistentDataType.STRING, flightModel);
            player.sendMessage(ChatColor.GREEN + "Vehicle tagged as " + flightModel);
            return true;
        }

        EntityType vehicleType = EntityType.MINECART;
        Vehicle vehicle = (Vehicle)player.getWorld().spawnEntity(player.getLocation(), vehicleType);
        
        String flightModel = "Spaceship";
        if (type.equalsIgnoreCase("airplane"))
            flightModel = "Airplane";
            
        vehicle.getPersistentDataContainer().set(flightModelKey, PersistentDataType.STRING, flightModel);
        vehicle.addPassenger(player);
        player.sendMessage(ChatColor.GREEN + flightModel + " spawned!");

        return true;
    }
}
