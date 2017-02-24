package me.robomwm.MountainDewritoes.Events;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
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

    Creature badEntity;
    Player player;

    public MonsterTargetPlayerEvent(Creature badEntity, Player player)
    {
        this.badEntity = badEntity;
        this.player = player;
    }

    public Creature getBadEntity()
    {
        return badEntity;
    }

    public Player getPlayer()
    {
        return player;
    }
}
