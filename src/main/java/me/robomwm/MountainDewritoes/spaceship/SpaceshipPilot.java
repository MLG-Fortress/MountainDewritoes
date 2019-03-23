package me.robomwm.MountainDewritoes.spaceship;

import me.robomwm.MountainDewritoes.Events.PlayerSteerVehicleEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class SpaceshipPilot implements Listener
{
    private Plugin plugin;
    private Map<Player, Spaceship> pilots = new HashMap<>();

    public SpaceshipPilot(Plugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEnterCockpit(VehicleEnterEvent event)
    {
        if (event.getEntered().getType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getEntered();
        Vehicle vehicle = event.getVehicle();

        //remove when done testing
        if (vehicle.getType() != EntityType.MINECART)
            return;
        String name = "Shuttle";
        //end test code

//        String name = vehicle.getCustomName();
//        if (name == null || name.isEmpty() || !name.endsWith("\u00a7\u00a7"))
//            return;
//        name = name.replaceAll("\\u00a7\\u00a7", "");

        switch (name)
        {
            case "Shuttle":
                pilots.put(player, new Spaceship(plugin, event.getVehicle()));
        }
    }

    @EventHandler
    private void onPilotSteer(PlayerSteerVehicleEvent event)
    {
        Spaceship spaceship = pilots.get(event.getPlayer());
        if (spaceship == null)
            return;
        spaceship.steer(event);
    }
}
