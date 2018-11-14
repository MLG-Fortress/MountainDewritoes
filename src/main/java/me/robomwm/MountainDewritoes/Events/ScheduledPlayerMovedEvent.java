package me.robomwm.MountainDewritoes.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 11/14/2018.
 *
 * @author RoboMWM
 */
public class ScheduledPlayerMovedEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    private Player player;

    public ScheduledPlayerMovedEvent(Player player)
    {
        this.player = player;
    }

    public Player getPlayer()
    {
        return player;
    }
}
