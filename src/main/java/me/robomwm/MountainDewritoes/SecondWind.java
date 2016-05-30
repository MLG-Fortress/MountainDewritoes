package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.Title;
import me.clip.actionannouncer.ActionAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.getspout.spoutapi.material.item.Potion;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by RoboMWM on 5/28/2016.
 */
public class SecondWind implements Listener
{
    Map<Player, Integer> fallenPlayers = new HashMap<>();
    Title fallTitle;
    Title secondWindTitle;
    Main instance;

    SecondWind(Main yeaIKnow)
    {
        instance = yeaIKnow;
        Title.Builder title = new Title.Builder();
        title.title(ChatColor.RED + "Fight 4 ur lyfe!");
        title.subtitle("Get a kill 2 revive!");
        title.stay(100);
        title.fadeOut(60);
        fallTitle = title.build();
        title.title(ChatColor.GREEN + "Second Wind!");
        title.subtitle("");
        title.stay(40);
        title.fadeOut(20);
        secondWindTitle = title.build();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR) //2 lazy 2 softdepend
    void onPlayerGetsHurt(EntityDamageEvent event)
    {
        if (event.getFinalDamage() <= 0)
            return;
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        final Player player = (Player)event.getEntity();

        if (fallenPlayers.containsKey(player))
            return;

        //If the blow is gunna kill 'em
        if (event.getFinalDamage() >= player.getHealth())
        {
            fallenPlayers.put(player, 20);

            player.sendTitle(fallTitle);
            player.addPotionEffect(PotionEffectType.GLOWING.createEffect(800, 0));
            player.addPotionEffect(PotionEffectType.JUMP.createEffect(800, -5));
            player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(800, 0));
            player.setHealth(player.getMaxHealth());
            player.setWalkSpeed(0.04f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
            //Play dramatic moozik
            new BukkitRunnable()
            {
                public void run()
                {
                    if (!fallenPlayers.containsKey(player))
                    {
                        this.cancel();
                        return;
                    }
                    int healthTime = fallenPlayers.get(player);
                    if (healthTime <= 0)
                    {
                        player.setHealth(0D);
                        this.cancel();
                        return;
                    }
                    fallenPlayers.put(player, --healthTime);
                    ActionAPI.sendPlayerAnnouncement(player, dyingHealth(healthTime));
                    if (player.getWalkSpeed() > 0.04f)
                        player.setWalkSpeed(0.04f);
                }
            }.runTaskTimer(instance, 0L, 20L);
        }
        event.setCancelled(true);
    }

    /**
     * Fallen player kills another entity
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity().getKiller() == null)
            return;
        Player player = event.getEntity().getKiller();
        if (fallenPlayers.containsKey(player))
            resetPlayer(event.getEntity().getKiller(), true);
    }

    /**
     * Fallen player disconnects
     */
    @EventHandler
    void onPlayerChickenOut(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (fallenPlayers.containsKey(player))
        {
            player.setHealth(0D); //Die
            resetPlayer(player, false);
        }
    }

    /**
     * Fallen player dies
     */
    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerDie(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        if (fallenPlayers.containsKey(player));
            resetPlayer(player, false);
    }

    /**
     * Fallen player gets splashed with potion that regens health somehow
     */
    @EventHandler
    void onPlayerRegainHealth(EntityRegainHealthEvent event)
    {
        if (!event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.MAGIC))
            return;
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        Player player = (Player)event.getEntity();
        resetPlayer(player, true);
    }

    void resetPlayer(Player player, boolean revive)
    {
        player.setWalkSpeed(0.2f);
        fallenPlayers.remove(player);
        //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound");
        if (revive)
        {
            player.removePotionEffect(PotionEffectType.GLOWING);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.setHealth(player.getMaxHealth() / 3f);
            player.sendTitle(secondWindTitle);
        }
    }

    String dyingHealth(int health)
    {
        StringBuilder hello = new StringBuilder();
        if (health > 10)
            hello.append(ChatColor.YELLOW);
        else
            hello.append(ChatColor.RED);

        //Is this a good way to do this..?
        for (int i = 0; i < health; i++)
            hello.append("\u258C"); // ▌
        return hello.toString();
    }
}
