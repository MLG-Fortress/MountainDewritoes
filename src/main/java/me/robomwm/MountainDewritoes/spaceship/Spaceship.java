package me.robomwm.MountainDewritoes.spaceship;

import me.robomwm.MountainDewritoes.Events.Key;
import me.robomwm.MountainDewritoes.Events.PlayerSteerVehicleEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * Created on 9/21/2018.
 *
 * @author RoboMWM
 */
public class Spaceship implements Listener
{
    private final Vehicle vehicle;
    private Vector thrust = new Vector();
    private Vector direction = new Vector(.2, 0, 0);
    private double pitch = 0;
    private double yaw = 0;
    private BukkitTask engine;
    private boolean brakes = false;
    private double acceleration = 1.01;
    private double deceleration = 0.9;
    private double maxSpeedSquared = 0.25;

    public Spaceship(Plugin plugin, Vehicle vehicle)
    {
        this.vehicle = vehicle;
        vehicle.setGravity(false);

        if (vehicle instanceof Minecart)
        {
            Minecart cart = (Minecart)vehicle;
            cart.setMaxSpeed(999);
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        engine = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Vector vector = direction;

                if (yaw != 0)
                    vector.rotateAroundY(yaw);

                if (brakes && direction.lengthSquared() > 0.0025)
                    vector.multiply(deceleration);
                else if (!brakes && direction.lengthSquared() < maxSpeedSquared)
                    vector.multiply(acceleration);

                if (pitch != 0)
                {
                    Vector rotated2D = vector.clone().rotateAroundY(Math.PI / 2);
                    rotated2D.setY(0);
                    rotated2D.normalize();
                    vector = vector.clone().rotateAroundNonUnitAxis(rotated2D, pitch);
                }

                for (Entity entity : vehicle.getPassengers())
                    ((Player)entity).sendActionBar(vehicle.getVelocity().toString());

                vehicle.setVelocity(vector);
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

//    public void move(Vector direction)
//    {
//        thrust.add(direction);
//    }

    public void steer(PlayerSteerVehicleEvent event)
    {
        brakes = false;
        pitch = 0;
        yaw = 0;

        StringBuilder keys = new StringBuilder();

        for (Key key : event.getKeysPressed())
        {
            switch (key)
            {
                case LEFT:
                    yaw = Math.PI / 80;
                    break;
                case RIGHT:
                    yaw = Math.PI / -80;
                    break;
                case FORWARD:
                    pitch = Math.PI / 6;
                    break;
                case BACK:
                    pitch = Math.PI / -6;
                    break;
                case JUMP:
                    brakes = true;
                    break;
            }
            keys.append(key);
        }

        event.getPlayer().sendActionBar(keys.toString());
    }
}

