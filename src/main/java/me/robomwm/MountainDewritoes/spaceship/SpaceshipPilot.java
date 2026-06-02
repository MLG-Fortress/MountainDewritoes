package me.robomwm.MountainDewritoes.spaceship;

import me.robomwm.MountainDewritoes.Events.PlayerSteerVehicleEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class SpaceshipPilot implements Listener
{
    private MountainDewritoes plugin;
    private NamespacedKey flightModelKey;
    private Map<Vehicle, FlightControl> activeFlights = new HashMap<>();
    private Map<Player, Vehicle> steering = new HashMap<>();

    public SpaceshipPilot(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        this.flightModelKey = new NamespacedKey(plugin, "flight_model");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEnterCockpit(VehicleEnterEvent event)
    {
        if (event.getEntered().getType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getEntered();
        Vehicle vehicle = event.getVehicle();

        String model = vehicle.getPersistentDataContainer().get(flightModelKey, PersistentDataType.STRING);

        if (model == null)
            return;

        FlightControl control = activeFlights.get(vehicle);
        if (control == null)
        {
            switch (model)
            {
                case "Spaceship":
                    control = new Spaceship(plugin, vehicle);
                    break;
                case "Airplane":
                    control = new Airplane(plugin, vehicle);
                    break;
            }
            if (control != null)
                activeFlights.put(vehicle, control);
        }

        if (control != null)
            steering.put(player, vehicle);
    }

    @EventHandler(ignoreCancelled = true)
    private void onExitCockpit(VehicleExitEvent event)
    {
        if (event.getExited().getType() != EntityType.PLAYER)
            return;
        
        Player player = (Player)event.getExited();
        steering.remove(player);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        steering.remove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    private void onVehicleDestroy(VehicleDestroyEvent event)
    {
        Vehicle vehicle = event.getVehicle();
        FlightControl control = activeFlights.remove(vehicle);
        if (control != null)
            control.stop();
        
        steering.entrySet().removeIf(entry -> entry.getValue().equals(vehicle));
    }

    @EventHandler
    private void onPilotSteer(PlayerSteerVehicleEvent event)
    {
        Vehicle vehicle = steering.get(event.getPlayer());
        if (vehicle == null)
            return;
        
        FlightControl control = activeFlights.get(vehicle);
        if (control != null)
            control.steer(event);
    }
}
