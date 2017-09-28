package me.robomwm.MountainDewritoes;

import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 5/25/2016.
 * Reimplements sound effect on death and other, future miscellaneous stuff
 */
public class DeathListener implements Listener
{
    MountainDewritoes instance;
    HashMap<Player, List<ItemStack>> deathItems = new HashMap<>();
    Location defaultRespawnLocation;
    DeathListener(MountainDewritoes yayNoMain)
    {
        instance = yayNoMain;
        defaultRespawnLocation = new Location(instance.getServer().getWorld("spawn"), -404, 9, -157, 123.551f, 27.915f);
    }

    @EventHandler
    void onPlayerSadness(PlayerDeathEvent event)
    {
        final Player player = event.getEntity();

        //Only drop some items (randomly determined)
        if (instance.isSurvivalWorld(player.getWorld()))
        {
            List<ItemStack> drops = event.getDrops();
            Iterator<ItemStack> iterator = drops.iterator();
            List<ItemStack> dropsToReturn = new ArrayList<>();
            while (iterator.hasNext())
            {
                ItemStack itemStack = iterator.next();
                if (ThreadLocalRandom.current().nextInt(4) == 0)
                    continue;
                dropsToReturn.add(itemStack);
                iterator.remove();
            }
            deathItems.put(player, dropsToReturn);
        }

        //Only lose 8 levels of XP (vs. all XP on death)
        //if (player.getLevel() > 8)
        //    event.setNewLevel(player.getLevel() - 8);

        event.setKeepLevel(true);
        event.setDroppedExp(0);

        //Stop all playing sounds, if any.
        player.stopSound("");

        //Believe it or not, the Minecraft client does not even trigger this sound on player death,
        //it does trigger it for other players though, just not the player who died
        player.playSound(player.getLocation(), "fortress.death", SoundCategory.PLAYERS, 3000000f, 1.0f);
    }

    /**
     * Respawn handler
     * Gives back items that weren't dropped
     * Activates death spectating, if respawned within "respawn time"
     */
    @EventHandler(priority = EventPriority.LOW) //Since we soft-depend MV, we'll still override it at this priority
    void onPlayerRespawn(PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();

        Location respawnLocation = defaultRespawnLocation;
        if (!instance.isKnownWorld(player.getWorld()))
            respawnLocation = player.getWorld().getSpawnLocation();
        event.setRespawnLocation(respawnLocation);

        //Return items
        if (deathItems.containsKey(player))
        {
            for (ItemStack drop : deathItems.get(player))
            {
                player.getInventory().addItem(drop);
            }
            player.sendMessage("Saved " + String.valueOf(deathItems.get(player).size()) + " item stacks from your inventory when you died.");
            deathItems.remove(player);
        }
    }
}