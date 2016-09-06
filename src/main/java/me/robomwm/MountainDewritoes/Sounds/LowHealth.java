package me.robomwm.MountainDewritoes.Sounds;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Time;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by RoboMWM on 8/30/2016.
 */
public class LowHealth implements Listener
{
    HashMap<Player, Long> alreadyLowHealth = new HashMap<>();
    MountainDewritoes instance;
    public LowHealth(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR) //playing sound effect based on damage info. Not making any changes.
    void onPlayerOuchie(EntityDamageEvent event)
    {
        //Only care about players
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getEntity();

        //Only play the low health sound once, until the player is no longer at low health
//        if (alreadyLowHealth.contains(player))
//        {
//            double health = player.getHealth() - event.getFinalDamage();
//            if (health >= 10.0)
//            {
//                //player.stopSound("fortress.lowhealth");
//                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound " + player.getName() + " player fortress.lowhealth");
//                alreadyLowHealth.remove(player);
//            }
//            return;
//        }

        if (player.getFoodLevel() >= 20 && player.getSaturation() > 0)
            return; //ignore rapid health regeneration

        double health = player.getHealth() - event.getFinalDamage();
        if (health <= 4.0f && !alreadyLowHealth.containsKey(player))
        {
            player.playSound(player.getLocation(), "fortress.lowhealth", 3000000f, 1.0f);
            //TODO: add gasp
            alreadyLowHealth.put(player, System.currentTimeMillis());
            new BukkitRunnable()
            {
                public void run()
                {
                    if (!alreadyLowHealth.containsKey(player))
                    {
                        cancel(); //Some other event determined player is not at low health (e.g. death handler)
                        return;
                    }
                    if (player.getHealth() > 5f)
                    {
                        alreadyLowHealth.remove(player);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound " + player.getName() + " player fortress.lowhealth");
                        cancel(); //Player is not at critical health
                        return;
                    }
                    //Has it been 18 seconds yet?
                    if ((System.currentTimeMillis() - 17900L) < alreadyLowHealth.get(player))
                        return;
                    alreadyLowHealth.put(player, System.currentTimeMillis());
                    player.playSound(player.getLocation(), "fortress.lowhealth", 3000000f, 1.0f);
                }
            }.runTaskTimer(instance, 100L, 2L);
        }
    }
    @EventHandler(ignoreCancelled = true)
    void resetLowHealthIndicator(PlayerDeathEvent event)
    {
        alreadyLowHealth.remove(event.getEntity());
    }
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    void onRegainHealth(EntityRegainHealthEvent event)
//    {
//        //Only care about players
//        if (event.getEntityType() != EntityType.PLAYER)
//            return;
//
//        Player player = (Player)event.getEntity();
//
//        if (!alreadyLowHealth.containsKey(player))
//            return;
//        double health = player.getHealth() + event.getAmount();
//
//        if (health >= 10.0)
//        {
//            alreadyLowHealth.remove(player);
//        }
//    }
}
