package me.robomwm.MountainDewritoes.spaceship;

import me.robomwm.MountainDewritoes.Events.Key;
import me.robomwm.MountainDewritoes.Events.PlayerSteerVehicleEvent;
import org.bukkit.entity.Minecart;
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
    private Vehicle vehicle;
    private Vector thrust = new Vector();
    private Vector direction = new Vector();
    private BukkitTask engine;
    private double acceleration = 1.01;
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
                //vehicle.setVelocity(thrust.multiply(acceleration));
                vehicle.setVelocity(direction);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

//    public void move(Vector direction)
//    {
//        thrust.add(direction);
//    }

    public void steer(PlayerSteerVehicleEvent event)
    {
        Vector vector = new Vector(.1, 0, 0);

        for (Key key : event.getKeysPressed())
        {
            switch (key)
            {
                case LEFT:
                    vector.rotateAroundY(Math.PI / 2);
                    break;
                case RIGHT:
                    vector.rotateAroundY(Math.PI / -2);
                    break;
                case FORWARD:
                    vector.rotateAroundZ(Math.PI / -2);
                    break;
                case BACK:
                    vector.rotateAroundZ(Math.PI / 2);
                    break;
                case JUMP:
                    vector.zero();
                    break;
            }
            event.getPlayer().sendMessage(key.name());
        }

        direction = vector;
    }
}
