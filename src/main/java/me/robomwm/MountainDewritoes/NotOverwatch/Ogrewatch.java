package me.robomwm.MountainDewritoes.NotOverwatch;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
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
        ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());
        if (itemStack == null || itemStack.getType() != Material.FEATHER)
            changeHeewo(player, null);
        else
            changeHeewo(player, Heewos.LOOSEEOH);
    }

    private void changeHeewo(Player player, Heewos heewo)
    {
        Heewos previousHeewo = dummies.remove(player);
        if (heewo == null)
            return;

        if (previousHeewo != null)
        {
            switch (previousHeewo)
            {
                case LOOSEEOH:
                    player.setAllowFlight(false);
                    break;
            }
        }

        switch (heewo)
        {
            case LOOSEEOH:
                player.setAllowFlight(true);
                player.sendMessage("u r now looceeoh");
                break;
        }
        dummies.put(player, heewo);
    }

    boolean isLucio(Player player)
    {
        return dummies.get(player) == Heewos.LOOSEEOH;
    }
}
