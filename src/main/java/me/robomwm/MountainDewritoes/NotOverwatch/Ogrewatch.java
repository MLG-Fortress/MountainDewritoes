package me.robomwm.MountainDewritoes.NotOverwatch;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created on 6/20/2017.
 *
 * @author RoboMWM
 */
public class Ogrewatch implements Listener
{
    private Map<Player, Set<Heewos>> dummies = new HashMap<>();

    private String SKATES = ChatColor.GREEN + "loo-see-oh's skates";

    public Ogrewatch(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        new Looseeoh(plugin, this);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : plugin.getServer().getOnlinePlayers())
                {
                    addRemoveHeewo(player, Heewos.LOOSEEOH, isMatch(player.getInventory().getBoots(), SKATES));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler(ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        dummies.remove(event.getPlayer());
    }

    private void addRemoveHeewo(Player player, Heewos heewo, boolean add)
    {
        if (!dummies.containsKey(player))
            dummies.put(player, new HashSet<>());

        Set<Heewos> heewos = dummies.get(player);

        if (add)
            heewos.add(heewo);
        else
            heewos.remove(heewo);
    }

    public boolean isHeewo(Player player, Heewos heewo)
    {
        return dummies.containsKey(player) && dummies.get(player).contains(heewo);
    }

    private boolean isMatch(ItemStack itemStack, String name)
    {
        if (itemStack == null)
            return false;
        if (!itemStack.getItemMeta().hasDisplayName())
            return false;
        return name.equalsIgnoreCase(itemStack.getItemMeta().getDisplayName());
    }


//    @EventHandler(ignoreCancelled = true)
//    void onWeaponChangeHeewo(PlayerItemHeldEvent event)
//    {
//        Player player = event.getPlayer();
//        ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());
//        if (itemStack == null || itemStack.getType() != Material.FEATHER)
//            changeHeewo(player, null);
//        else
//            changeHeewo(player, Heewos.LOOSEEOH);
//    }
//
//    private void changeHeewo(Player player, Heewos heewo)
//    {
//        Heewos previousHeewo = dummies.remove(player);
//
//        if (previousHeewo != null)
//        {
//            switch (previousHeewo)
//            {
//                case LOOSEEOH:
//                    player.setAllowFlight(false);
//                    break;
//            }
//        }
//
//        if (heewo == null)
//            return;
//
//        switch (heewo)
//        {
//            case LOOSEEOH:
//                player.setAllowFlight(true);
//                player.sendMessage("u r now looceeoh");
//                break;
//        }
//        dummies.put(player, heewo);
//    }
}
