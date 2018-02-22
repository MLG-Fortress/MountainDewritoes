package me.robomwm.MountainDewritoes.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * Created on 2/22/2018.
 * Called when a player lands after performing a midair move
 * @see me.robomwm.MountainDewritoes.NSA
 * @author RoboMWM
 */
public class PlayerLandEvent extends Event
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
    private int id;

    public PlayerLandEvent(Player player, int id)
    {
        this.player = player;
        this.id = id;
    }

    public Player getPlayer()
    {
        return player;
    }

    public int getId()
    {
        return id;
    }
}
