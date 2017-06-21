package me.robomwm.MountainDewritoes.NotOverwatch;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 6/20/2017.
 *
 * @author RoboMWM
 */
public class Ogrewatch implements Listener
{
    public Ogrewatch(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        new Looseeoh(plugin, this);
    }

    @EventHandler(ignoreCancelled = true)
    void onQuit(PlayerQuitEvent event)
    {
        changeHeewo(event.getPlayer(), null);
    }

    private Map<Player, Heewos> dummies = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    void onWeaponChangeHeewo(PlayerItemHeldEvent event)
    {
        Player player = event.getPlayer();
        if (player.getInventory().getItem(event.getNewSlot()).getType() == Material.FEATHER)
            changeHeewo(player, Heewos.LOOSEEOH);
        else
            changeHeewo(player, null);
    }

    private void changeHeewo(Player player, Heewos heewo)
    {
        switch (dummies.remove(player))
        {
            case LOOSEEOH:
                player.setAllowFlight(false);
        }

        if (heewo == null)
            return;

        switch (heewo)
        {
            case LOOSEEOH:
                player.setAllowFlight(true);
        }
        dummies.put(player, heewo);
    }

    boolean isLucio(Player player)
    {
        return dummies.get(player) == Heewos.LOOSEEOH;
    }
}
