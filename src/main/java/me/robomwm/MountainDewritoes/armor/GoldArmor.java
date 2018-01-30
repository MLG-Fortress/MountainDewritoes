package me.robomwm.MountainDewritoes.armor;

import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
/**
 * Created on 1/3/2018.
 *
 * @author RoboMWM
 */
public class GoldArmor implements ArmorTemplate
{
    /* GOLD BOOTS */
    //Mid-air jump
    public void onSneak(PlayerToggleSneakEvent event, Player player)
    {
        if (!event.isSneaking())
            return;

        if (event.getPlayer().isOnGround())
            return;

        if (!NSA.getMidairMap().containsKey(player))
        {
            NSA.getMidairMap().put(player, 1);
            Vector vector = player.getLocation().toVector();
            player.setVelocity(vector.subtract(NSA.getLastLocation(player).toVector()).multiply(1.5D).setY(0.7D));
        }
    }

    //GOLD LEGGINGS
    //I'm free, free-falling
    //Player experiences less gravitational pull/feels floaty while sprinting
    public void onSprint(PlayerToggleSprintEvent event, Player player)
    {
        if (!event.isSprinting())
            return;

        if (player.hasPotionEffect(PotionEffectType.SPEED))
            return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 15, true, false));
        player.setFoodLevel(0);
    }
}
