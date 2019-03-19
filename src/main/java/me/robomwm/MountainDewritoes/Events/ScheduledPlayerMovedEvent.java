package me.robomwm.MountainDewritoes.Events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created on 11/14/2018.
 *
 * @author RoboMWM
 */
public class ScheduledPlayerMovedEvent extends PlayerMoveEvent
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

    public ScheduledPlayerMovedEvent(Player player, Location from)
    {
        super(player, from, player.getLocation());
        if (from.getWorld() != player.getWorld())
            setFrom(player.getLocation());
    }
}
