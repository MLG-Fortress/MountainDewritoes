package me.robomwm.MountainDewritoes.spaceship;

import me.robomwm.MountainDewritoes.Events.PlayerSteerVehicleEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Vehicle;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public class WarpDrive implements FlightControl
{
    private final Plugin plugin;
    private final Vehicle vehicle;
    private final Location target;
    private final Consumer<Vehicle> onComplete;
    
    private BukkitTask task;
    private boolean warping = false;
    private double currentSpeed = 0.05;
    private final double maxWarpSpeed = 10.0;
    private final double acceleration = 1.1;
    private final double deceleration = 0.9;
    
    public WarpDrive(Plugin plugin, Vehicle vehicle, Location target, Consumer<Vehicle> onComplete)
    {
        this.plugin = plugin;
        this.vehicle = vehicle;
        this.target = target;
        this.onComplete = onComplete;
        
        start();
    }

    private void start()
    {
        task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!vehicle.isValid() || vehicle.getPassengers().isEmpty())
                {
                    stop();
                    return;
                }

                Location currentLoc = vehicle.getLocation();
                Vector toTarget = target.toVector().subtract(currentLoc.toVector());
                double distanceSquared = toTarget.lengthSquared();

                if (distanceSquared < 16) // Arrived
                {
                    vehicle.setVelocity(new Vector(0, 0, 0));
                    stop();
                    if (onComplete != null)
                        onComplete.accept(vehicle);
                    return;
                }

                Vector targetDir = toTarget.clone().normalize();
                Vector currentDir = vehicle.getVelocity().normalize();
                
                // If velocity is zero, use a small initial push towards target
                if (currentDir.lengthSquared() < 0.001)
                    currentDir = targetDir.clone();

                // Alignment Phase
                if (!warping)
                {
                    // Gradually rotate currentDir towards targetDir
                    double dot = currentDir.dot(targetDir);
                    if (dot < 0.99) // Not fully aligned
                    {
                        // Simple lerp for alignment
                        currentDir.add(targetDir.clone().multiply(0.1)).normalize();
                    }
                    
                    currentSpeed = Math.min(0.375, currentSpeed * 1.05); // Accelerate to "sub-warp" speed
                    
                    if (dot > 0.99 && currentSpeed >= 0.35) // Aligned and fast enough (approx 75% of 0.5)
                    {
                        warping = true;
                        vehicle.getWorld().playSound(currentLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 2f);
                    }
                }
                // Warp Phase
                else
                {
                    // Clipping: High velocity helps, but we also ensure it keeps moving towards target
                    currentDir = targetDir; // Locked on target

                    // Deceleration check
                    double stopDistance = (currentSpeed * currentSpeed) / (2 * (1 - deceleration) * 10); // Rough estimate
                    if (distanceSquared < stopDistance * stopDistance)
                    {
                        currentSpeed = Math.max(0.1, currentSpeed * deceleration);
                    }
                    else
                    {
                        currentSpeed = Math.min(maxWarpSpeed, currentSpeed * acceleration);
                    }

                    // Visuals
                    vehicle.getWorld().spawnParticle(Particle.CLOUD, currentLoc, 10, 0.5, 0.5, 0.5, 0.1);
                    vehicle.getWorld().spawnParticle(Particle.SONIC_BOOM, currentLoc, 1, 0, 0, 0, 0);
                    
                    // Preloading chunks
                    Vector ahead = targetDir.clone().multiply(currentSpeed * 10);
                    Location aheadLoc = currentLoc.clone().add(ahead);
                    aheadLoc.getChunk().load(true);
                }

                vehicle.setVelocity(currentDir.multiply(currentSpeed));
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void steer(PlayerSteerVehicleEvent event)
    {
        // Steering is locked during warp alignment and warp itself
        event.getPlayer().sendActionBar("Warp Drive Active - Controls Locked");
    }

    @Override
    public void stop()
    {
        if (task != null)
            task.cancel();
    }
}
