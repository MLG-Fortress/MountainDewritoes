package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by RoboMWM on 5/25/2016.
 * Reimplements sound effect on death and other, future miscellaneous stuff
 */
public class DeathListener implements Listener
{
    Main instance;
    Random random = new Random();
    HashMap<Player, HashSet<ItemStack>> deathItems = new HashMap<>();
    DeathListener(Main iKnowIShouldntCallItMain)
    {
        instance = iKnowIShouldntCallItMain;
    }

    @EventHandler
    void onPlayerSadness(PlayerDeathEvent event)
    {
        final Player player = event.getEntity();

        //Only drop some items (randomly determined)
        ItemStack drop;
        List<ItemStack> drops = event.getDrops();
        Iterator<ItemStack> iterator = drops.iterator();
        HashSet<ItemStack> dropsToReturn = new HashSet<>();
        while (iterator.hasNext())
        {
            if (random.nextInt(3) != 0)
                continue;
            dropsToReturn.add(iterator.next());
            iterator.remove();
        }
        deathItems.put(player, dropsToReturn);


        //Believe it or not, the Minecraft client does not even trigger this sound on player death,
        //it just plays player_hurt, so yea...
        //Apparently, it actually triggers it for other players, just not the player who died, I guess...?
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);

        //Auto-respawn player if they haven't clicked respawn within the last 20 seconds
        //Helps prevent weird client problems like client-side entity buildup or whatever,
        //thus freezing the client or idk that's what happened to me.
        new BukkitRunnable()
        {
            public void run()
            {
                player.spigot().respawn();
            }
        }.runTaskLater(instance, 400L);
    }

    /**
     * Give back items that were not dropped on death
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerRespawn(PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();
        if (!deathItems.containsKey(player))
            return;

        for (ItemStack drop : deathItems.get(player))
            player.getInventory().addItem(drop);
    }
}
