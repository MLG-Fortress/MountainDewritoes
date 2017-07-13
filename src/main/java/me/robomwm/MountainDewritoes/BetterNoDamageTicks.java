package me.robomwm.MountainDewritoes;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 7/13/2017.
 *
 * Customized "NoDamageTicks" based on the cause.
 *
 * @author RoboMWM
 */
public class BetterNoDamageTicks implements Listener
{
    long currentTick = 0L;
    private JavaPlugin instance;
    private final String DAMAGE_IMMUNITY_KEY = "MD_DamageImmunity";

    public BetterNoDamageTicks(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.instance = plugin;
        new BukkitRunnable()
        {
            public void run()
            {
                currentTick++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @EventHandler
    private void onEntitySpawn(CreatureSpawnEvent event)
    {
        event.getEntity().setMaximumNoDamageTicks(0);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        event.getPlayer().setMaximumNoDamageTicks(0);
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event)
    {
        event.getEntity().removeMetadata(DAMAGE_IMMUNITY_KEY, instance);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        event.getPlayer().removeMetadata(DAMAGE_IMMUNITY_KEY, instance);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void checkAndApplyImmunity(EntityDamageEvent event)
    {
        if (!event.getEntity().hasMetadata(DAMAGE_IMMUNITY_KEY))
            return;

        List<MetadataValue> metadata = new ArrayList<>(event.getEntity().getMetadata(DAMAGE_IMMUNITY_KEY));

        //Clear existing metadata (e.g. always cleans up metadata - may move to a scheduled task if CPU performance is an issue)
        event.getEntity().removeMetadata(DAMAGE_IMMUNITY_KEY, instance);

        for (MetadataValue value : metadata)
        {
            DamageImmunityData damageImmunityData = (DamageImmunityData)value.value();
            if (damageImmunityData.getTickToExpire() > currentTick)
            {
                //re-add non-stale immunitydata
                event.getEntity().setMetadata(DAMAGE_IMMUNITY_KEY, value);

                if (damageImmunityData.getCause() == event.getCause() && damageImmunityData.getDamage() >= event.getFinalDamage())
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void setImmunity(EntityDamageEvent event) //TODO: ignore fakeentitydamageevent from mcmmo?
    {
        long ticksToExpire;

        switch (event.getCause())
        {
            case FIRE:
            case FIRE_TICK:
            case CONTACT:
            case CRAMMING:
            case HOT_FLOOR:
            case SUFFOCATION:
                ticksToExpire = 5L;
                break;
            case ENTITY_ATTACK:
            case FALLING_BLOCK:
                ticksToExpire = 10L;
                break;
            default:
                return;
            //TODO: drowning, wither, poison
        }

        event.getEntity().setMetadata(DAMAGE_IMMUNITY_KEY, new FixedMetadataValue(instance, new DamageImmunityData(event.getCause(), event.getFinalDamage(), currentTick + ticksToExpire)));
    }
}

class DamageImmunityData
{
    private EntityDamageEvent.DamageCause cause;
    private double damage;
    private long tickToExpire;

    DamageImmunityData(EntityDamageEvent.DamageCause cause, double damage, long tickToExpire)
    {
        this.cause = cause;
        this.damage = damage;
        this.tickToExpire = tickToExpire;
    }

    public long getTickToExpire()
    {
        return tickToExpire;
    }

    public EntityDamageEvent.DamageCause getCause()
    {
        return cause;
    }

    public double getDamage()
    {
        return damage;
    }
}
