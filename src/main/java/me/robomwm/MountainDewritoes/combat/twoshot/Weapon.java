package me.robomwm.MountainDewritoes.combat.twoshot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.TippedArrow;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created on 1/10/2019.
 *
 * Holds properties of a weapon.
 *
 * Ideas:
 * gradual spread/spread recovery
 * damage decay
 * multi-type ammo??
 *
 * @author RoboMWM
 */
public class Weapon
{
    private YamlConfiguration yaml;

    //projectile attributes
    public EntityType type;
    public String name;

    public Weapon(File file)
    {
        this.yaml = YamlConfiguration.loadConfiguration(file);
        this.type = EntityType.valueOf(yaml.getString("projectileType"));
        this.name = file.getName();
        for (String sound : yaml.getString("sounds", "").split(","))
            sounds.add(new SoundAttribute(sound));
    }

    public String getName()
    {
        return getName();
    }

    /**
     * Ammo capacity
     * @return
     */
    public int getCapacity()
    {
        return yaml.getInt("capacity");
    }

    /**
     *
     * @return in ticks
     */
    public int getReloadTime()
    {
        return yaml.getInt("reloadTime", 30);
    }

    /**
     *
     * @return minimum delay between shots in ticks
     */
    public int getFireRate()
    {
        return yaml.getInt("fireRate", 1);
    }

    /**
     *
     * @return ammo consumed per shot
     */
    public int getAmmoPerShot()
    {
        return yaml.getInt("ammoPerShot", 1);
    }

    private List<SoundAttribute> sounds = new ArrayList<>();

    public BukkitTask playSound(Player player, Plugin plugin)
    {
        return new BukkitRunnable()
        {
            Iterator<SoundAttribute> soundsIterator = sounds.iterator();
            int i = 0;
            private SoundAttribute soundToPlay = soundsIterator.next();

            @Override
            public void run()
            {
                if (++i < soundToPlay.getDelay())
                    return;

                soundToPlay.playSound(player);

                if (soundsIterator.hasNext())
                {
                    i = 0;
                    soundToPlay = soundsIterator.next();
                }
                else
                    cancel();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private Class<? extends Projectile> getProjectileClass()
    {
        switch (type)
        {
            case ARROW:
                return Arrow.class;
            case SNOWBALL:
                return Snowball.class;
        }
        return Snowball.class;
    }

    private Projectile spawnProjectile(ProjectileSource source, Vector velocity)
    {
        Projectile projectile = source.launchProjectile(getProjectileClass(), velocity);
        projectile.setGravity(yaml.getBoolean("gravity", false));
        //TODO: metadata (damage)
        return projectile;
    }

    private Vector getVelocity(Vector facing)
    {
        //TODO: spread
        facing.multiply(yaml.getDouble("speed", 1.0));
        return facing;
    }

    public Projectile fire(LivingEntity source)
    {
        World world = source.getWorld();
        Location location = source.getEyeLocation();
        source.launchProjectile(getProjectileClass());
        Projectile projectile = null;

        for (int i = 0; i < getAmmoPerShot(); i++)
            projectile = spawnProjectile(source, getVelocity(source.getLocation().getDirection()));

        return projectile;
    }

    public String getAsString()
    {
        return yaml.saveToString();
    }
}

class SoundAttribute
{
    private Sound sound;
    private String name;
    private float volume;
    private float pitch;
    private int delay;

    //name-volume-pitch-delay
    SoundAttribute(String unformatted)
    {
        String[] parseMe = unformatted.split("-");

        try
        {
            sound = Sound.valueOf(parseMe[0].toUpperCase());
        }
        catch (Exception e)
        {
            name = parseMe[0];
        }
        volume = Float.valueOf(parseMe[1]);
        pitch = Float.valueOf(parseMe[2]);
        delay = Integer.valueOf(parseMe[3]);
    }

    public void playSound(Player player)
    {
        if (sound == null)
            player.getWorld().playSound(player.getLocation(), sound, SoundCategory.PLAYERS, volume, pitch);
        else
            player.getWorld().playSound(player.getLocation(), name, SoundCategory.PLAYERS, volume, pitch);
    }

    public int getDelay()
    {
        return delay;
    }
}
