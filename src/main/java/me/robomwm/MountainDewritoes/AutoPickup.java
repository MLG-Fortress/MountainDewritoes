package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by RoboMWM on 10/20/2016.
 */
public class AutoPickup implements Listener
{
    MountainDewritoes instance;
    World prison;
    Set<Player> remindedThisSession = new HashSet<>();
    String reminderMessage = ChatColor.GOLD + "Hold sneak to pickup items";
    public AutoPickup(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        prison = instance.getServer().getWorld("prison");
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerAttemptToPickupItem(PlayerPickupItemEvent event)
    {
        Player player = event.getPlayer();

        //Ignore prison world
        if (player.getWorld() == prison)
            return;

        if (!remindedThisSession.contains(event.getPlayer()))
        {
            if (!player.hasPlayedBefore())
                player.sendMessage(reminderMessage);
            else
            {
                event.getItem().setCustomName(reminderMessage);
                event.getItem().setCustomNameVisible(true);
            }
            remindedThisSession.add(player);
        }
        if (!player.isSneaking())
            event.setCancelled(true);
    }

    @EventHandler
    void onPlayerQuitRemoveFromRemindedSet(PlayerQuitEvent event)
    {
        remindedThisSession.remove(event.getPlayer());
    }
}
