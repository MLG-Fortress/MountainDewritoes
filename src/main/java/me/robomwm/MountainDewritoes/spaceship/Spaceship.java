package me.robomwm.MountainDewritoes.spaceship;

import org.bukkit.entity.Minecart;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * Created on 9/21/2018.
 *
 * @author RoboMWM
 */
public class Spaceship
{
    private Minecart minecart;
    private Vector thrust = new Vector();
    private BukkitTask engine;

    public Spaceship(JavaPlugin plugin, Minecart minecart)
    {
        this.minecart = minecart;
        engine = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                minecart.setVelocity(thrust);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void move(Vector direction)
    {
        thrust.add(direction);
    }
}
