package me.robomwm.MountainDewritoes.spaceship;

import me.robomwm.MountainDewritoes.Events.PlayerSteerVehicleEvent;

public interface FlightControl
{
    void steer(PlayerSteerVehicleEvent event);
    void stop();
}
