package me.robomwm.MountainDewritoes.armor;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 1/30/2018.
 *
 * @author RoboMWM
 */
public class IronArmor implements ArmorTemplate
{
    JavaPlugin instance;

    IronArmor(JavaPlugin plugin)
    {
        instance = plugin;
    }

    @Override
    public void onSneak(PlayerToggleSneakEvent event, Player player)
    {
        if (player.isOnGround())
        {
            if (player.hasPotionEffect(PotionEffectType.LEVITATION))
                player.removePotionEffect(PotionEffectType.LEVITATION);
            return;
        }

        if (event.isSneaking() && !player.hasPotionEffect(PotionEffectType.LEVITATION) && player.getFoodLevel() > 0)
        {
            magnetizeBoots(player);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (player.isSneaking() && player.getFoodLevel() > 0)
                        magnetizeBoots(player);
                    else
                        cancel();
                }

                @Override
                public synchronized void cancel() throws IllegalStateException
                {
                    super.cancel();
                    //if (player.hasPotionEffect(PotionEffectType.LEVITATION) && player.getPotionEffect(PotionEffectType.LEVITATION).getAmplifier() == 1 || player.getPotionEffect(PotionEffectType.LEVITATION).getAmplifier() == 5)
                        player.removePotionEffect(PotionEffectType.LEVITATION);

                }
            }.runTaskTimer(instance, 5L, 5L);
        }
        else if (!event.isSneaking() && player.hasPotionEffect(PotionEffectType.LEVITATION) && player.getPotionEffect(PotionEffectType.LEVITATION).getAmplifier() == 1)
            player.removePotionEffect(PotionEffectType.LEVITATION);
    }

    private void magnetizeBoots(Player player)
    {
        int velocity = (int)(-player.getVelocity().getY() * 15);
        if (velocity < 1)
            velocity = 1;
        else if (velocity > 127)
            velocity = 127;
        player.removePotionEffect(PotionEffectType.LEVITATION);
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, velocity, true, false));
        player.setFoodLevel(player.getFoodLevel() - 1);
    }

    @Override
    public void onSprint(PlayerToggleSprintEvent event, Player player)
    {
    }
}
