package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.Title;
import me.clip.actionannouncer.ActionAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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
    Title revivedTitle;
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
        revivedTitle = title.build();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
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
            player.addPotionEffect(PotionEffectType.GLOWING.createEffect(400, 0));
            player.addPotionEffect(PotionEffectType.JUMP.createEffect(400, -5));
            player.setHealth(player.getMaxHealth());
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
                        resetPlayer(player, false);
                        this.cancel();
                        return;
                    }
                    player.setWalkSpeed(0.04f);
                    ActionAPI.sendPlayerAnnouncement(player, dyingHealth(healthTime));
                }
            }.runTaskTimer(instance, 0L, 20L);
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity().getKiller() == null)
            return;
        fallenPlayers.remove(event.getEntity().getKiller());
    }
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
    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerDie(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        if (fallenPlayers.containsKey(player));
            resetPlayer(player, false);
    }
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
        player.removePotionEffect(PotionEffectType.GLOWING);
        player.removePotionEffect(PotionEffectType.JUMP);
        fallenPlayers.remove(player);
        //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound");
        if (revive)
        {
            player.setHealth(player.getMaxHealth() / 3);
            player.sendTitle(revivedTitle);
        }
    }

    String dyingHealth(int health)
    {
        StringBuilder hello = new StringBuilder();
        if (health > 10)
            hello.append(ChatColor.GREEN);
        else if (health > 5)
            hello.append(ChatColor.YELLOW);
        else
            hello.append(ChatColor.RED);

        //Is this a good way to do this..?
        for (int i = 0; i < health; i++)
            hello.append("▌");
        return hello.toString();
    }
}
