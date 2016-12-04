package me.robomwm.MountainDewritoes.Sounds;

import com.destroystokyo.paper.Title;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by RoboMWM on 9/17/2016.
 */
public class HitSound implements Listener
{
    Title hitMarker;
    Title largeHitMarker;
    Title.Builder eliminationBuilder;
    MountainDewritoes instance;
    public HitSound(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        Title.Builder title = new Title.Builder();
        title.title(ChatColor.WHITE + "\u2572 \u2571"); //╲ ╱
        title.subtitle(ChatColor.WHITE + "\u2571 \u2572"); //╱ ╲
        title.fadeIn(0);
        title.stay(2);
        title.fadeOut(5);
        hitMarker = title.build();
        title.title(ChatColor.WHITE + "\\ /");
        title.subtitle(ChatColor.WHITE + "/ \\");
        largeHitMarker = title.build();
        eliminationBuilder = new Title.Builder();
        eliminationBuilder.title(" ");
        eliminationBuilder.fadeIn(5);
        eliminationBuilder.stay(30);
        eliminationBuilder.fadeOut(20);
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
        Player attacker;
        if (damager instanceof Projectile)
        {
            Projectile projectile = (Projectile)damager;
            if (!(projectile.getShooter() instanceof Player))
                return; //Dispenser or Skelly
            attacker = (Player)projectile.getShooter();
        }
        else
            attacker = (Player)damager;

        attacker.playSound(attacker.getLocation(), Sound.UI_BUTTON_CLICK, 3000000f, 1f);

        if (!instance.isUsingTitle(attacker))
        {
            if (event.getFinalDamage() < 10)
                attacker.sendTitle(hitMarker);
            else
                attacker.sendTitle(largeHitMarker);
        }
    }

    @EventHandler
    void onEntityDeath(EntityDeathEvent event)
    {
        Player killer = event.getEntity().getKiller();
        if (killer == null || killer.getType() != EntityType.PLAYER)
            return;

        if (event.getEntityType() == EntityType.PLAYER)
            eliminationBuilder.subtitle("Eliminated " + ChatColor.RED + event.getEntity().getName());
        else
            eliminationBuilder.subtitle("Eliminated " + ChatColor.RED + event.getEntityType().toString().toLowerCase());
        Title title = eliminationBuilder.build();

        //Delay by 0.25 seconds (so we don't override hitmarker sound)
        new BukkitRunnable()
        {
            public void run()
            {
                killer.playSound(killer.getLocation(), "fortress.elimination", 3000000f, 1f);
                killer.sendTitle(title);
                instance.addUsingTitle(killer, title.getFadeIn() + title.getStay());
            }
        }.runTaskLater(instance, 3L);

    }
}
