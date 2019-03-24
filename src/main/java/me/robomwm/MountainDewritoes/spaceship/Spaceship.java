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
    private final Vehicle vehicle;
    private Vector thrust = new Vector();
    private Vector direction = new Vector(.1, 0, 0);
    private double pitch = 0;
    private double yaw = 0;
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
                if (pitch != 0)
                {
                    Vector rotated2D = direction.clone().rotateAroundY(Math.PI / 2);
                    rotated2D.setY(0);
                    rotated2D.normalize();
                    direction.rotateAroundNonUnitAxis(rotated2D, pitch);
                }
                if (yaw != 0)
                    direction.rotateAroundY(yaw);
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
        pitch = 0;
        yaw = 0;

        StringBuilder keys = new StringBuilder();

        for (Key key : event.getKeysPressed())
        {
            switch (key)
            {
                case LEFT:
                    yaw = Math.PI / 160;
                    break;
                case RIGHT:
                    yaw = Math.PI / -160;
                    break;
                case FORWARD:
                    pitch = Math.PI / -160;
                    break;
                case BACK:
                    pitch = Math.PI / 160;
                    break;
                case JUMP:
                    //vector.zero();
                    break;
            }
            keys.append(key);
        }

        event.getPlayer().sendActionBar(keys.toString());
    }
}

