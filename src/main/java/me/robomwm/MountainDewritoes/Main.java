package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Robo on 2/13/2016.
 */
public class Main extends JavaPlugin implements Listener
{
    Set<Player> usedEC = new HashSet<>();
    public void onEnable()
    {
        //Modifies PlayerListName and prefixes
        getServer().getPluginManager().registerEvents(new SimpleClansListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new LongFallBoots(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BetterZeldaHearts(), this);
        getServer().getPluginManager().registerEvents(new RandomStructurePaster(this), this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    //Warn new players that /ec costs money to use
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onPlayerPreprocess(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        //If player isn't new or if we've already warned this player before...
        if (!player.hasPlayedBefore() || usedEC.contains(player))
            return;
        //Check if player is attempting to access enderchest via command
        String message = event.getMessage().toLowerCase();
        if (!message.startsWith("/ec") && !message.startsWith("/pv"))
            return;

        player.sendMessage(ChatColor.GOLD + "Accessing the enderchest via a slash command costs 1337 dogecoins. To confirm, type /ec again.");
        event.setCancelled(true);
    }
}
