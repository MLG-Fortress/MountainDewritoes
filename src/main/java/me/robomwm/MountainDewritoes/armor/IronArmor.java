package me.robomwm.MountainDewritoes.armor;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created on 1/30/2018.
 *
 * @author RoboMWM
 */
public class IronArmor implements ArmorTemplate
{
    @Override
    public void onSneak(PlayerToggleSneakEvent event, Player player)
    {
        if (player.isOnGround())
        {
            if (player.hasPotionEffect(PotionEffectType.LEVITATION))
                player.removePotionEffect(PotionEffectType.LEVITATION);
            return;
        }

        if (event.isSneaking() && !player.hasPotionEffect(PotionEffectType.LEVITATION))
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 1200, 254, true, false));
        else if (!event.isSneaking() && player.hasPotionEffect(PotionEffectType.LEVITATION) && player.getPotionEffect(PotionEffectType.LEVITATION).getAmplifier() == 254)
            player.removePotionEffect(PotionEffectType.LEVITATION);
    }

    @Override
    public void onSprint(PlayerToggleSprintEvent event, Player player)
    {
    }
}
