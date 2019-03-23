package me.robomwm.MountainDewritoes.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Set;

public class PlayerSteerVehicleEvent extends PlayerEvent
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

    private Set<Key> keysPressed;

    public PlayerSteerVehicleEvent(Player who, Set<Key> keysPressed)
    {
        super(who);
        this.keysPressed = keysPressed;
    }

    public Set<Key> getKeysPressed()
    {
        return keysPressed;
    }

    public boolean isKeyPressed(Key key)
    {
        return keysPressed.contains(key);
    }
}
