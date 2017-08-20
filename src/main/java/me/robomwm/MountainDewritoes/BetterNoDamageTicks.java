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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 7/13/2017.
 *
 * Customized "NoDamageTicks" based on the cause.
 *
 * @author RoboMWM
 */
public class BetterNoDamageTicks implements Listener
{
    private MountainDewritoes instance;
    private final String DAMAGE_IMMUNITY_KEY = "MD_DamageImmunity";
    private Set<Entity> entitiesToClear = new HashSet<>();

    public BetterNoDamageTicks(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.instance = plugin;
    }

    public void onDisable()
    {
        for (Entity entity : entitiesToClear)
        {
            entity.removeMetadata(DAMAGE_IMMUNITY_KEY, instance);
        }
        entitiesToClear.clear();
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
        entitiesToClear.remove(event.getEntity());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        event.getPlayer().removeMetadata(DAMAGE_IMMUNITY_KEY, instance);
        entitiesToClear.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void checkAndApplyImmunity(EntityDamageEvent event)
    {
        if (!event.getEntity().hasMetadata(DAMAGE_IMMUNITY_KEY))
            return;

        List<DamageImmunityData> damageImmunityDataMetadata = (List<DamageImmunityData>)event.getEntity().getMetadata(DAMAGE_IMMUNITY_KEY).get(0).value();

        for (DamageImmunityData damageImmunityData : new ArrayList<>(damageImmunityDataMetadata))
        {
            if (damageImmunityData.getTickToExpire() > instance.getCurrentTick() && damageImmunityData.getCause() == event.getCause() && event.getFinalDamage() <= damageImmunityData.getDamage())
                event.setCancelled(true);
            else if (damageImmunityData.getTickToExpire() <= instance.getCurrentTick())
                damageImmunityDataMetadata.remove(damageImmunityData);
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
            case LAVA:
            case CRAMMING:
            case HOT_FLOOR:
            case SUFFOCATION:
                ticksToExpire = 8L;
                break;
            case ENTITY_ATTACK:
            case FALLING_BLOCK:
            case CONTACT:
                ticksToExpire = 10L;
                break;
            default:
                return;
            //TODO: drowning, wither, poison
        }

        if (!event.getEntity().hasMetadata(DAMAGE_IMMUNITY_KEY))
            event.getEntity().setMetadata(DAMAGE_IMMUNITY_KEY, new FixedMetadataValue(instance, new ArrayList<DamageImmunityData>()));
        ((List<DamageImmunityData>)event.getEntity().getMetadata(DAMAGE_IMMUNITY_KEY).get(0).value()).add(new DamageImmunityData(event.getCause(), event.getFinalDamage(), instance.getCurrentTick() + ticksToExpire));
        entitiesToClear.add(event.getEntity());
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
