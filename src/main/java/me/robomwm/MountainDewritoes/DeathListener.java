package me.robomwm.MountainDewritoes;

import me.robomwm.usefulutil.UsefulUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 5/25/2016.
 * Reimplements sound effect on death and other, future miscellaneous stuff
 */
public class DeathListener implements Listener
{
    private MountainDewritoes instance;
    private HashMap<Player, List<ItemStack>> deathItems = new HashMap<>();
    private List<String> deathWords = new ArrayList<>();
    private Map<Player, Location> playersDesiredRespawnLocation = new HashMap<>();
    private Location defaultRespawnLocation;

    DeathListener(MountainDewritoes yayNoMain)
    {
        instance = yayNoMain;
        defaultRespawnLocation = new Location(instance.getServer().getWorld("mall"), 2.488, 5, -7.305, 0f, 0f);
        deathWords.add("rekt");
        deathWords.add("wasted");
        deathWords.add("eliminated");
        deathWords.add("pwnd");
    }

    public String getDeathWord()
    {
        return deathWords.get(ThreadLocalRandom.current().nextInt(deathWords.size()));
    }

    public static String getItemName(ItemStack item)
    {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return item.getItemMeta().getDisplayName();
        return item.getI18NDisplayName();
    }

    @EventHandler
    void onPlayerSadness(PlayerDeathEvent event)
    {
        final Player player = event.getEntity();
        final Location location = player.getLocation();

        //Only drop some items (randomly determined)
        if (instance.isSurvivalWorld(player.getWorld()))
        {
            List<ItemStack> drops = event.getDrops();
            Iterator<ItemStack> iterator = drops.iterator();
            List<ItemStack> dropsToReturn = new ArrayList<>();
            while (iterator.hasNext())
            {
                if (dropsToReturn.size() > 36) //afaik this is playerinventory#size
                    break;
                ItemStack itemStack = iterator.next();
                if (ThreadLocalRandom.current().nextInt(10) == 0)
                    continue;
                dropsToReturn.add(itemStack);
                iterator.remove();
            }
            deathItems.put(player, dropsToReturn);
            EntityDamageEvent damageEvent = player.getLastDamageCause();
            StringBuilder message = new StringBuilder("umail text ");
            message.append(player.getName());
            message.append(" U wer ");
            message.append(getDeathWord());
            message.append(" at ");
            message.append(location.getWorld().getName());
            message.append(" ");
            message.append(location.getBlockX());
            message.append(", ");
            message.append(location.getBlockY());
            message.append(", ");
            message.append(location.getBlockZ());
            message.append("\n");
            if (damageEvent == null)
                instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), message.toString() + "But uh we dont no how???!?!? spoopy...");
            else
            {
                Entity killer = UsefulUtil.getKiller(event);

                if (killer != null)
                {
                    message.append("Final blow: ");
                    if (killer.getType() == EntityType.PLAYER)
                        message.append(killer.getName());
                    else
                        message.append(killer.getType().name().toLowerCase());
                    message.append("\n");
                }
                message.append("via ");
                message.append(damageEvent.getCause().toString().toLowerCase());
                message.append("\n");
            }

            if (!drops.isEmpty())
            {
                message.append("U lost deez items:\n");
                for (ItemStack drop : drops)
                {
                    message.append(drop.getAmount());
                    message.append(" ");
                    message.append(getItemName(drop));
                    message.append(ChatColor.RESET);
                    message.append(", ");
                }
                message.delete(message.length() - 2, message.length());
            }

            instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), message.toString());
        }

        //Only lose 8 levels of XP (vs. all XP on death)
        //if (player.getLevel() > 8)
        //    event.setNewLevel(player.getLevel() - 8);

        //Don't drop any exp
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
    @EventHandler(priority = EventPriority.LOW) //Since we soft-depend MV, we'll still override it at this priority (it listens at LOW)
    void onPlayerRespawn(PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();

        Location respawnLocation = player.getWorld().getSpawnLocation();

        if (instance.isSurvivalWorld(player.getWorld()))
            respawnLocation = playersDesiredRespawnLocation.getOrDefault(player, defaultRespawnLocation);
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