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
        if (event.getDamage() == 0D)
            return;

        if (event.getEntityType() != EntityType.PLAYER)
            return;
        switch (event.getCause())
        {
            case FALL:
                return; //TODO: drowning
        }

        float pitch = 1.0f;
        if (event.getFinalDamage() > 20D)
            pitch = 0.5f;
        else if (event.getFinalDamage() < 0.5D)
            pitch = 2f;

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

        //Play sound to player
        if (pitch == 0.5f)
            player.playSound(location, "fortress.classichurt", SoundCategory.PLAYERS, 3000000f, 1.0f);
        else
            player.playSound(location, Sound.ENTITY_GENERIC_HURT, SoundCategory.PLAYERS, 3000000f, pitch);
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
            world.playSound(location, "fortress.falldamage", SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
