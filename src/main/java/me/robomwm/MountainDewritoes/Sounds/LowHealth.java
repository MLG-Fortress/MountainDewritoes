package me.robomwm.MountainDewritoes.Sounds;

import fr.mrsheepsheep.tinthealth.THAPI;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

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

//        if (player.getFoodLevel() >= 20 && player.getSaturation() > 0)
//            return; //ignore rapid health regeneration

        double health = player.getHealth() - event.getFinalDamage();
        if (health <= 8f && !alreadyLowHealth.containsKey(player))
        {
            player.stopSound("");
            player.playSound(player.getLocation(), "fortress.lowhealth", SoundCategory.PLAYERS, 3000000f, 1.0f);
            //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound fortress.lowhealth player " + player.getName() + " 0 0 0 3000000");
            player.playSound(player.getLocation(), "fortress.lowhealthgasp", SoundCategory.PLAYERS, 3000000f, 1.0f);
            THAPI.setTint(player, 100);
            alreadyLowHealth.put(player, System.currentTimeMillis());
            new BukkitRunnable()
            {
                public void run()
                {
                    if (!alreadyLowHealth.containsKey(player))
                    {
                        THAPI.removeTint(player);
                        cancel(); //Some other event determined player is not at low health (e.g. death handler)
                        return;
                    }
                    if (player.getHealth() > 8f)
                    {
                        alreadyLowHealth.remove(player);
                        //THAPI.fadeTint(player, 100, 1); Fade is too slow
                        THAPI.removeTint(player);
                        //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound " + player.getName() + " player fortress.lowhealth");
                        player.stopSound("fortress.lowhealth", SoundCategory.PLAYERS);
                        cancel(); //Player is not at critical health
                        return;
                    }



                    //Has it been 18 seconds yet? (Soundbyte we play is 18 seconds long)
                    if ((System.currentTimeMillis() - 17900L) < alreadyLowHealth.get(player))
                        return;
                    alreadyLowHealth.put(player, System.currentTimeMillis());
                    THAPI.setTint(player, 100);
                    player.playSound(player.getLocation(), "fortress.lowhealth", SoundCategory.PLAYERS, 3000000f, 1.0f);
                    //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound fortress.lowhealth player " + player.getName() + " 0 0 0 3000000");
                }
            }.runTaskTimer(instance, 100L, 2L);

            new BukkitRunnable()
            {
                int ticks = 0;
                int nextTick = 1;
                boolean breathin = true;
                @Override
                public void run()
                {
                    if (!alreadyLowHealth.containsKey(player))
                    {
                        cancel();
                        return;
                    }

                    ticks++;

                    if (ticks != nextTick)
                        return;

                    if (breathin)
                    {
                        player.playSound(player.getLocation().subtract(0, 300D, 0), Sound.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 20f, 1.0f);
                        breathin = false;
                        ticks = 0;
                        nextTick = ThreadLocalRandom.current().nextInt(20, 40);
                    }
                    else
                    {
                        player.playSound(player.getLocation().subtract(0D, 300D, 0D), Sound.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 20f, (float)ThreadLocalRandom.current().nextDouble(0.83D, 0.87D));
                        breathin = true;
                        ticks = 0;
                        nextTick = ThreadLocalRandom.current().nextInt(70, 130);
                    }
                }
            }.runTaskTimer(instance, 140L, 1L);
        }
    }
    @EventHandler(ignoreCancelled = true)
    void resetLowHealthIndicator(PlayerDeathEvent event)
    {
        alreadyLowHealth.remove(event.getEntity());
    }
    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        alreadyLowHealth.remove(event.getPlayer());
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
