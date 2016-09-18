package me.robomwm.MountainDewritoes.Sounds;

import com.destroystokyo.paper.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Created by RoboMWM on 9/17/2016.
 * TODO: add "self-damage" sounds
 */
public class HitSound implements Listener
{
    Title hitMarker;
    Title largeHitMarker;
    public HitSound()
    {
        Title.Builder title = new Title.Builder();
        title.title(" ");
        title.subtitle(ChatColor.WHITE + "x");
        title.fadeIn(0);
        title.stay(1);
        title.fadeOut(3);
        hitMarker = title.build();
        title.subtitle(ChatColor.WHITE + "X");
        largeHitMarker = title.build();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerDamage(EntityDamageByEntityEvent event)
    {
        handleEntityDamageEventCuzThxSpigot(event);
    }

    //Not necessary for what I'm doing
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    void onPlayerIgniteWithArrow(EntityCombustByEntityEvent event)
//    {
//        EntityDamageByEntityEvent eventWrapper = new EntityDamageByEntityEvent(event.getCombuster(), event.getEntity(), EntityDamageEvent.DamageCause.FIRE_TICK, event.getDuration());
//        handleEntityDamageEventCuzThxSpigot(eventWrapper);
//    }

    void handleEntityDamageEventCuzThxSpigot(EntityDamageByEntityEvent event)
    {
        Entity damager = event.getDamager();
        //Check if attacker is a player or if damage was caused due to a projectile
        if (damager.getType() != EntityType.PLAYER && !(damager instanceof Projectile))
            return;

        //Get the attacker
        boolean critical;
        Player attacker = null;
        if (damager instanceof Projectile)
        {
            Projectile projectile = (Projectile)damager;
            if (!(projectile.getShooter() instanceof Player))
                return; //Dispenser
            attacker = (Player)projectile.getShooter();
        }
        else
            attacker = (Player)damager;

        attacker.playSound(attacker.getLocation(), Sound.UI_BUTTON_CLICK, 3000000f, 1f);

        if (event.getFinalDamage() < 10)
            attacker.sendTitle(hitMarker);
        else
            attacker.sendTitle(largeHitMarker);
    }

    @EventHandler
    void onEntityDeath(EntityDeathEvent event)
    {
        Player killer = event.getEntity().getKiller();
        if (killer == null || killer.getType() != EntityType.PLAYER)
            return;

        killer.playSound(killer.getLocation(), "fortress.elimination", 3000000f, 1f);
    }
}
