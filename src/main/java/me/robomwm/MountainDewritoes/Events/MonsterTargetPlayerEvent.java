package me.robomwm.MountainDewritoes.Events;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 2/13/2017.
 *
 * @author RoboMWM
 */
public class MonsterTargetPlayerEvent extends Event
{    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    Monster monster;
    Player player;

    public MonsterTargetPlayerEvent(Monster monster, Player player)
    {
        this.monster = monster;
        this.player = player;
    }

    public Monster getMonster()
    {
        return monster;
    }

    public Player getPlayer()
    {
        return player;
    }
}
