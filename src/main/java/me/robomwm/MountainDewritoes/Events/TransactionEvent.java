package me.robomwm.MountainDewritoes.Events;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 8/19/2017.
 *
 * @author RoboMWM
 */
public class TransactionEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private Player player;
    private double amount;
    private Economy economy;

    public TransactionEvent(Player player, double amount, Economy economy)
    {
        this.player = player;
        this.amount = amount;
        this.economy = economy;
    }

    public Player getPlayer()
    {
        return player;
    }

    public double getAmount()
    {
        return amount;
    }

    //For convenience
    public Economy getEconomy()
    {
        return economy;
    }
}
