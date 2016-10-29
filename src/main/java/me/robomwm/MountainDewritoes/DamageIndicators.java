package me.robomwm.MountainDewritoes;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 10/28/2016.
 */
public class DamageIndicators implements Listener
{
    MountainDewritoes instance;
    Set<Hologram> activeHolograms = new HashSet<>();
    DecimalFormat df = new DecimalFormat("#.#");

    public DamageIndicators(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    public int cleanupDamageIndicators()
    {
        int i = 0;
        for (Hologram hologram : activeHolograms)
        {
            hologram.delete();
            i++;
        }
        return i;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onDisplayDamageIndicator(EntityDamageByEntityEvent event)
    {
        if (event.getFinalDamage() <= 0.0D)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        Entity entity = event.getEntity();
        displayIndicator(entity.getLocation(), event.getFinalDamage() / 2D, true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onDisplayHealthRegenIndicator(EntityRegainHealthEvent event)
    {
        if (event.getAmount() <= 0.0D)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        Entity entity = event.getEntity();
        displayIndicator(entity.getLocation(), event.getAmount() / 2D, false);
    }

    public static Double r4nd0m(Double min, Double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    void displayIndicator(Location location, Double value, boolean isDamage)
    {
        double x = r4nd0m(-0.5D, 0.5D);
        double z = r4nd0m(-0.5D, 0.5D);
        Hologram hologram = HologramsAPI.createHologram(instance, location.add(x, 3D, z));
        if (isDamage)
            hologram.appendTextLine(ChatColor.RED + "-" + df.format(value));
        else
            hologram.appendTextLine(ChatColor.GREEN + "+" + df.format(value));
        activeHolograms.add(hologram);
        Long duration = 20L;
        //Display longer if value is in double digits
        if (value >= 10)
            duration = 40L;
        new BukkitRunnable()
        {
            public void run()
            {
                hologram.delete();
                activeHolograms.remove(hologram);
            }
        }.runTaskLater(instance, duration);
    }
}
