package me.robomwm.MountainDewritoes.Events;

import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 2/13/2017.
 *
 * @author RoboMWM
 */
public class JukeboxInteractEvent extends Event
{    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    Player player;
    Jukebox jukebox;
    Material disc;

    public JukeboxInteractEvent(Player player, Jukebox block, Material disc)
    {
        this.jukebox = block;
        this.player = player;
        this.disc = disc;
    }

    public Jukebox getJukebox()
    {
        return jukebox;
    }

    public Material getDisc()
    {
        return disc;
    }

    public Player getPlayer()
    {
        return player;
    }
}
