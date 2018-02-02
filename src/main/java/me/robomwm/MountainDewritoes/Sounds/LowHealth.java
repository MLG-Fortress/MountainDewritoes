package me.robomwm.MountainDewritoes.Sounds;

import fr.mrsheepsheep.tinthealth.THAPI;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
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

        final double health = player.getHealth() - event.getFinalDamage();
        //final double healthPercentage = health / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (health <= 8D && !alreadyLowHealth.containsKey(player))
        {
            player.stopSound("");
            player.playSound(player.getLocation(), "fortress.lowhealth", SoundCategory.PLAYERS, 3000000f, 1.0f);
            //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound fortress.lowhealth player " + player.getName() + " 0 0 0 3000000");
            player.playSound(player.getLocation(), "fortress.lowhealthgasp", SoundCategory.PLAYERS, 3000000f, 1.0f);
            applyTint(player, true);
            alreadyLowHealth.put(player, System.currentTimeMillis());

            new BukkitRunnable()
            {
                public void run()
                {
                    if (!alreadyLowHealth.containsKey(player) || player.getHealth() > 8D)
                    {
                        cancel(); //Some other event determined player is not at low health (e.g. death handler)
                        return;
                    }

                    //Has it been 18 seconds yet? (Soundbyte we play is 18 seconds long)
                    if ((System.currentTimeMillis() - 17900L) < alreadyLowHealth.get(player))
                        return;
                    alreadyLowHealth.put(player, System.currentTimeMillis());
                    player.playSound(player.getLocation(), "fortress.lowhealth", SoundCategory.PLAYERS, 3000000f, 1.0f);
                    applyTint(player, true);
                    //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound fortress.lowhealth player " + player.getName() + " 0 0 0 3000000");
                }

                @Override
                public synchronized void cancel() throws IllegalStateException
                {
                    super.cancel();
                    player.stopSound("fortress.lowhealth", SoundCategory.PLAYERS);
                    applyTint(player, false);
                    alreadyLowHealth.remove(player);
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
                        player.playSound(player.getLocation().subtract(0, 300D, 0), Sound.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 23f, 1.0f);
                        breathin = false;
                        ticks = 0;
                        nextTick = ThreadLocalRandom.current().nextInt(20, 40);
                    }
                    else
                    {
                        player.playSound(player.getLocation().subtract(0D, 300D, 0D), Sound.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 23f, (float)ThreadLocalRandom.current().nextDouble(0.83D, 0.87D));
                        breathin = true;
                        ticks = 0;
                        nextTick = ThreadLocalRandom.current().nextInt(70, 130);
                    }
                }
            }.runTaskTimer(instance, 140L, 1L);
        }
    }

    private void applyTint(Player player, boolean should)
    {
        try
        {
            if (should)
                THAPI.setTint(player, 100);
            else
                THAPI.fadeTint(player, 100, 1);
                //THAPI.removeTint(player);
        }
        catch (Exception | Error e)
        {
            instance.getLogger().info("You probably need to update TintHealth.");
            //instance.getLogger().warning(e.getMessage());
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
