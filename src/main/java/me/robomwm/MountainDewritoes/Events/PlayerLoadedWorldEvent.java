package me.robomwm.MountainDewritoes.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * Created on 10/2/2018
 * Called when a player moves after changing worlds
 * @author RoboMWM
 */
public class PlayerLoadedWorldEvent extends Event
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

    public PlayerLoadedWorldEvent(Player player)
    {
        this.player = player;
    }

    public Player getPlayer()
    {
        return player;
    }
}
