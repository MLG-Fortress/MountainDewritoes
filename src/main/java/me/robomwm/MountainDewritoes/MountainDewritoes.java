package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Sounds.AtmosphericManager;
import me.robomwm.MountainDewritoes.Sounds.HitSound;
import me.robomwm.MountainDewritoes.Sounds.LowHealth;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Robo on 2/13/2016.
 */
public class MountainDewritoes extends JavaPlugin implements Listener
{
    //Set<Player> usedEC = new HashSet<>();
    //Pattern ec = Pattern.compile("\\bec\\b|\\bechest\\b|\\bpv\\b");
    Map<Player, Integer> usingTitlePlayers = new HashMap<>();
    DamageIndicators damageIndicators;
    AtmosphericManager atmosphericManager;
    String acceptableColors;

    public void onEnable()
    {
        damageIndicators = new DamageIndicators(this);
        atmosphericManager = new AtmosphericManager(this);
        PluginManager pm = getServer().getPluginManager();
        SimpleClans sc = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
        ClanManager clanManager = sc.getClanManager();
        pm.registerEvents(this, this);
        //Modifies PlayerListName and prefixes
        pm.registerEvents(new SimpleClansListener(this, clanManager), this);
        pm.registerEvents(new ChatListener(this, clanManager), this);
        pm.registerEvents(new LongFallBoots(), this);
        pm.registerEvents(new DeathListener(this), this);
        pm.registerEvents(new BetterZeldaHearts(), this);
        pm.registerEvents(new RandomStructurePaster(this), this);
        pm.registerEvents(new RandomTipOfTheDay(this), this);
        //pm.registerEvents(new SecondWind(this), this);
        pm.registerEvents(new ShoppingMall(this), this);
        pm.registerEvents(new LowHealth(this), this);
        pm.registerEvents(new TeleportingEffects(this), this);
        pm.registerEvents(new HitSound(this), this);
        pm.registerEvents(new SpawnWorldListener(this), this);
        pm.registerEvents(new GamemodeInventoryManager(), this);
        //pm.registerEvents(new Footsteps(), this);
        pm.registerEvents(new NoKnockback(this), this);
        pm.registerEvents(damageIndicators, this);
        pm.registerEvents(new SleepManagement(this, atmosphericManager), this);
        pm.registerEvents(atmosphericManager, this);
        //pm.registerEvents(new JukeboxManager(this), this);
        StringBuilder builder = new StringBuilder();
        Set<String> colorThingy = new HashSet<>(Arrays.asList("Aqua", "Blue", "Dark_Blue", "Green", "Dark_Green", "Light_Purple", "Dark_Purple", "Red", "Dark_Red", "Gold", "Yellow"));
        for (String ok : colorThingy)
        {
            builder.append(ChatColor.valueOf(ok.toUpperCase()));
            builder.append(ok);
            builder.append(", ");
        }
        acceptableColors = builder.toString().substring(0, builder.length() - 2);
    }

    public void onDisable()
    {
        getLogger().info("Cleaning up any active damage indicator holograms...");
        getLogger().info(String.valueOf(damageIndicators.cleanupDamageIndicators()) + " holograms removed.");
    }

    //Warn new players that /ec costs money to use
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
//    void onPlayerPreprocess(PlayerCommandPreprocessEvent event)
//    {
//        //Check if player is attempting to access enderchest via command
//        String message = event.getMessage().toLowerCase();
//        if (!ec.matcher(message).matches())
//            return;
//
//        Player player = event.getPlayer();
//        //If player isn't new or if we've already warned this player before...
//        if (player.hasPlayedBefore() || usedEC.contains(player))
//            return;
//
//        player.sendMessage(ChatColor.GOLD + "Accessing the enderchest via a slash command costs 1337 dogecoins. To confirm, type /ec again.");
//        event.setCancelled(true);
//        usedEC.add(player);
//    }

    public boolean isUsingTitle(Player player)
    {
        return usingTitlePlayers.containsKey(player);
    }
    public void addUsingTitle(Player player, int ticks)
    {
        int index = 0;
        if (isUsingTitle(player))
            index += usingTitlePlayers.get(player);
        final int finalIndex = index;
        usingTitlePlayers.put(player, index);
        new BukkitRunnable()
        {
            public void run()
            {
                if (!isUsingTitle(player))
                    return;
                if (usingTitlePlayers.get(player) == finalIndex)
                    usingTitlePlayers.remove(player);
                //Otherwise, another addUsingTitle had overrided our previous addUsingTitle invokation
            }
        }.runTaskLater(this, ticks);
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player)sender;

        if (args.length < 1)
        {
            player.sendMessage("/nick <color>");
            player.sendMessage("colors: " + acceptableColors);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("nick"))
        {
            ChatColor color = ChatColor.valueOf(args[0].toUpperCase());
            if (color == null)
                color = ChatColor.getByChar(args[0]);
            if (color == null)
            {
                try
                {
                    color = ChatColor.getByChar(args[0].substring(1));
                }
                catch (Exception e){} //No I don't care
            }
            if (color == null || isBannedColor(color))
            {
                player.sendMessage("Valid colors: " + acceptableColors);
                return true;
            }
            player.performCommand("enick " + convertColor(color) + player.getName());
            return true;
        }
        return false;
    }

    boolean isBannedColor(ChatColor color)
    {
        if (color.isFormat())
            return true;

        switch (color)
        {
            case BLACK:
            case DARK_GRAY:
            case GRAY:
            case WHITE:
                return true;
        }
        return false;
    }

    String convertColor(ChatColor color)
    {
        switch (color)
        {
            case AQUA:
                return "&b";
            case BLUE:
                return "&9";
            case DARK_AQUA:
                return "&3";
            case DARK_BLUE:
                return "&1";
            case DARK_GREEN:
                return "&2";
            case DARK_PURPLE:
                return "&5";
            case DARK_RED:
                return "&4";
            case GOLD:
                return "&6";
            case GREEN:
                return "&a";
            case LIGHT_PURPLE:
                return "&d";
            case RED:
                return "&c";
            case YELLOW:
                return "&e";
        }
        return null;
    }

    /**
     * Make chunk loading when teleporting between worlds seem faster
     * We aren't doing this for every teleport since plugins might perform teleports in same chunk (e.g. PortalStick)
     * On teleporting, sets view distance to 3, then back to 8 after 5 seconds
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerChangesWorldSetViewDistance(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        World world = event.getPlayer().getWorld();
        if (player.hasMetadata("DEAD"))
            return;
        player.setViewDistance(3);
        new BukkitRunnable()
        {
            public void run()
            {
                //Don't execute if another task is scheduled
                if (player.getWorld() != world || !player.isOnline())
                    this.cancel();
                //Wait for player to land before resetting view distance
                else if (player.isOnGround())
                {
                    player.setViewDistance(8);
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 200L, 100L);
    }

    public void timedBar(Player player, int seconds, String message)
    {
        if (message == null || player == null)
            return;
        new BukkitRunnable()
        {
            int secondsRemaining = seconds * 2;
            public void run()
            {
                player.sendActionBar(message);
                secondsRemaining--;
                if (secondsRemaining <= 0 || !player.isOnline())
                    this.cancel();
            }
        }.runTaskTimer(this, 0L, 10L);
    }
}
