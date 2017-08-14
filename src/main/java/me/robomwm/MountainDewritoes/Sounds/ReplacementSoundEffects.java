package me.robomwm.MountainDewritoes.Sounds;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 3/11/2017.
 *
 * @author RoboMWM
 */
public class ReplacementSoundEffects implements Listener
{
    public ReplacementSoundEffects(MountainDewritoes mountainDewritoes)
    {
        mountainDewritoes.registerListener(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onReceivingDamage(EntityDamageEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        switch (event.getCause())
        {
            case FALL:
            case DROWNING:
                return;
        }

        float pitch = r4nd0m(0.9f, 1.1f);

        Player player = (Player)event.getEntity();
        Location location = player.getLocation();

        //Play sound to others
        //TODO: distance checks? (Only for hacked clients)
        for (Player p : player.getWorld().getPlayers())
        {
            if (p == player)
                continue;
            p.playSound(location, "fortress.roblox", SoundCategory.PLAYERS, 1.0f, pitch);
        }

        if (event.getDamage() <= 0)
            return;

        //Play sound to player
        if (player.getHealth() < 20D)
            player.playSound(location, "fortress.classic_hurt", SoundCategory.PLAYERS, 3000000f, pitch);
        else
            player.playSound(location, Sound.ENTITY_GENERIC_HURT, SoundCategory.PLAYERS, 3000000f, pitch);
    }

    public float r4nd0m(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }



    /**
     * Play fall damage sound only if the player actually took fall damage
     * (We only care about players, but this could easily be extended to all entities)
     *
     * Resource pack has been altered to play damage/fallsmall for both big and small falls (though louder at bigger fall)
     * The "crack" from damage/bigfall is used as a "falldamage" sound.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onFallDamage(EntityDamageEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        Location location = event.getEntity().getLocation();
        World world = location.getWorld();

        if (event.getEntityType() == EntityType.PLAYER)
            world.playSound(location, "fortress.falldamage", SoundCategory.PLAYERS, 1.0f, 1.2f);
    }
}
