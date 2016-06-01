package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.Title;
import me.clip.actionannouncer.ActionAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
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
    Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    SecondWind(Main yeaIKnow)
    {
        instance = yeaIKnow;
        Title.Builder title = new Title.Builder();
        title.title(ChatColor.RED + "FITE 4 UR LYFE!");
        title.subtitle("GET A KILL 2 REVIVE");
        title.fadeIn(0);
        title.stay(40);
        title.fadeOut(5);
        fallTitle = title.build();
        title.title("");
        title.subtitle(ChatColor.AQUA + "SECOND WIND!");
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

        //Stop executing this event handler if player is a fallenPlayer
        //Also stop entity-caused damage for just-added fallenPlayers
        if (fallenPlayers.containsKey(player))
        {
            if ((fallenPlayers.get(player) >= 20) && entityCausedDamage(event.getCause()))
                event.setCancelled(true);
            return;
        }

        //If the blow is gunna kill 'em
        if (event.getFinalDamage() >= player.getHealth())
        {
            fallenPlayers.put(player, 20);

            player.sendTitle(fallTitle);
            player.addPotionEffect(PotionEffectType.GLOWING.createEffect(40, 0));
            player.addPotionEffect(PotionEffectType.JUMP.createEffect(800, -5));
            player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(256, 0)); //Idk, blindness tick duration is weird
            player.setHealth(player.getMaxHealth()); //Refill health
            player.setWalkSpeed(0.03f); //Set player's speed
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f); //Play hurt noise (since we cancel damage)
            event.setCancelled(true);
            //TODO: Play dramatic moozik
            new BukkitRunnable()
            {
                Team team = mainScoreboard.getTeam(player.getName());
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
                    player.sendTitle(getFiteTitleIdk(healthTime));
                    player.getWorld().spigot().playEffect(player.getLocation(), Effect.VILLAGER_THUNDERCLOUD);
                    player.addPotionEffect(PotionEffectType.GLOWING.createEffect(10, 0));
                    team.setSuffix(ChatColor.RED + " is dying!");
                    if (player.getWalkSpeed() > 0.03f)
                        player.setWalkSpeed(0.03f);
                }
            }.runTaskTimer(instance, 40L, 20L);
        }
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
     * Fallen player attempts to teleport
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerAttemptTeleport(PlayerTeleportEvent event)
    {
        if (fallenPlayers.containsKey(event.getPlayer()))
            event.setCancelled(true);
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
            mainScoreboard.getTeam(player.getName()).setSuffix(ChatColor.GREEN + " revived!");
            //TODO: Stop suspenseful moozik, play cool moozik
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
        for (int i = hello.length(); i < 20; i++)
            hello.append("  ");
        return hello.toString();
    }

    Title getFiteTitleIdk(int health)
    {
        Title.Builder title = new Title.Builder();
        title.fadeIn(0);
        title.stay(30);
        title.title(ChatColor.RED + "FITE 4 UR LYFE!");
        title.subtitle("GET A KILL 2 REVIVE " + dyingHealth(health));
        return title.build();
    }

    boolean entityCausedDamage(EntityDamageEvent.DamageCause damageCause)
    {
        return damageCause == EntityDamageEvent.DamageCause.CONTACT ||
                damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                damageCause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION;
    }
}
