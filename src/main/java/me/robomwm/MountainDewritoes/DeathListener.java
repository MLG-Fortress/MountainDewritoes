package me.robomwm.MountainDewritoes;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Created by RoboMWM on 5/25/2016.
 * Reimplements sound effect on death and other, future miscellaneous stuff
 */
public class DeathListener implements Listener
{
    @EventHandler
    void onPlayerSadness(PlayerDeathEvent event)
    {
        event.getEntity().playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
    }
}
