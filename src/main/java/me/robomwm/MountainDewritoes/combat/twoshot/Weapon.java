package me.robomwm.MountainDewritoes.combat.twoshot;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
@Deprecated
public class Weapon
{
    private String name;

    //weapon attributes
    public int clipSize;
    public int reloadTime = 40; //in ticks
    public int fireRate; //in ticks
    public int quantityPerShot = 1;

    //projectile attributes
    public Material type;
    public double damage;
    public int spread = 1;
    public boolean gravity = false;
    private double velocity = 4;

    public void setVelocity(int velocity)
    {
        setVelocity(velocity / 25D);
    }

    public void setVelocity(double velocity)
    {
        velocity = Math.min(4, velocity);
        //velocity = Math.max(-4, velocity); //Idk, backwards bullets?
        this.velocity = velocity;
    }

    private List<SoundAttribute> sounds = new ArrayList<>();

    public Weapon(String name, String stringOfSounds, double damage, int clipSize)
    {
        for (String sound : stringOfSounds.split(","))
            sounds.add(new SoundAttribute(sound));
    }

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

    public void fire()
    {
        for (int i = 0; i < quantityPerShot; i++)
        {
           //fillout later
            return;
        }
    }
}

@Deprecated
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
