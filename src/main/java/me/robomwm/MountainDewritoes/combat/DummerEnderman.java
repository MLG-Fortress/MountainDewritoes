package me.robomwm.MountainDewritoes.combat;

import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created on 5/1/2018.
 *
 * @author RoboMWM
 */
public class DummerEnderman implements Listener
{
    @EventHandler
    private void onEndermanAttemptingToEscape(EndermanEscapeEvent event)
    {
        if (event.getReason() == EndermanEscapeEvent.Reason.INDIRECT)
            event.setCancelled(true);
    }
}
