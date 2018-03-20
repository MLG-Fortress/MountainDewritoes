package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 2/23/2018.
 *
 * @author RoboMWM
 */
public class FirstJoin implements Listener
{
    private MountainDewritoes plugin;
    private Location firstJoinLocation;
    private Location cellar;

    public FirstJoin(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        firstJoinLocation = new Location(plugin.getServer().getWorld("firstjoin"), -1.5, 26.5, -3.5, 180, 20);
        cellar = new Location(plugin.getServer().getWorld("firstjoin"), -5, 25, 25);
//        new BukkitRunnable()
//        {
//            Location location = new Location(WORLD, -1, 70, -4);
//            @Override
//            public void run()
//            {
//                for (Player player : WORLD.getPlayers())
//                {
//                    if (!NSA.getTempdata(player, "firstjoin"))
//                        return;
//                    if (player.getLocation().distanceSquared(location) > 1)
//                        return;
//                    NSA.removeTempdata(player, "firstjoin");
//                    player.sendBlockChange(new Location(WORLD, -1, 71, -6), Material.SMOOTH_BRICK, (byte)2);
//                    player.sendBlockChange(new Location(WORLD, -1, 70, -6), Material.SMOOTH_BRICK, (byte)1);
//                    player.sendBlockChange(new Location(WORLD, -2, 69, -4), Material.AIR, (byte)0);
//                    player.sendBlockChange(new Location(WORLD, -2, 70, -4), Material.AIR, (byte)0);
//                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, 1.0f);
//                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, 1.0f);
//                }
//            }
//        }.runTaskTimer(plugin, 1200L, 10L);
    }

    private void onJoinWorld(Player player)
    {
        player.teleport(firstJoinLocation);
        //player.sendBlockChange(new Location(WORLD, -2, 69, -4), Material.SUGAR_CANE_BLOCK, (byte)0);
        //player.sendBlockChange(new Location(WORLD, -2, 70, -4), Material.SUGAR_CANE_BLOCK, (byte)0);
        //NSA.setTempdata(player, "firstjoin");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (!event.getPlayer().hasPlayedBefore())
        {
            event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(6D);
            event.getPlayer().setLevel(1);
            //TODO: give gold boots, etc.

            //Spawn mobs in cellar, if none exists in it.
            for (Entity entity : cellar.getChunk().getEntities())
                if (entity.getType() == EntityType.SILVERFISH)
                    return;
            ((Monster)cellar.getWorld().spawnEntity(cellar, EntityType.SILVERFISH)).setAI(false);
            ((Monster)cellar.getWorld().spawnEntity(cellar.add(1,0,1), EntityType.SILVERFISH)).setAI(false);
            ((Monster)cellar.getWorld().spawnEntity(cellar.add(1,0,-1), EntityType.SILVERFISH)).setAI(false);
            ((Monster)cellar.getWorld().spawnEntity(cellar.add(-1,0,-1), EntityType.SILVERFISH)).setAI(false);
            ((Monster)cellar.getWorld().spawnEntity(cellar.add(-1,0,1), EntityType.SILVERFISH)).setAI(false);
        }

        event.getPlayer().setMaximumAir(event.getPlayer().getMaximumAir() + player.getLevel()); //Lol idk the default

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (player.getWorld() == firstJoinLocation.getWorld())
                    onJoinWorld(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event)
    {
        if (event.getPlayer().getWorld() == firstJoinLocation.getWorld())
            onJoinWorld(event.getPlayer());
    }

    @EventHandler
    private void onPlayerTerminal(PlayerInteractEvent event)
    {
        if (event.getPlayer().getWorld() != firstJoinLocation.getWorld())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        BookMeta bookMeta;

        switch (event.getClickedBlock().getType())
        {
            case COMMAND_REPEATING:
                bookMeta = LazyUtil.getBookMeta();
                bookMeta.spigot().addPage(
                        LazyUtil.buildPage("Deer " + player.getDisplayName() + ChatColor.BLACK +
                                ",\nSorry 4 missin ur arrival, but I c ur quite an inexperienced adventure anyways.\u00AF\\_(\u30C4)_/\u00AF\nHow about u go to the cellar and clean up those annoying paper-eaters."),
                        LazyUtil.buildPage("The cellar is behind you and to the right. Don't worry, your hands should do the trick. Plus, dat chestplate of urs should keep u safe and cozy."));
                break;
            case COMMAND_CHAIN:
                bookMeta = LazyUtil.getBookMeta();
                bookMeta.spigot().addPage(
                        LazyUtil.buildPage("Hey " + player.getDisplayName() + ChatColor.BLACK +
                        ",\nThanks for helpin out. Hopefully u got a bit more experience! Btw, check out your /pda by pressing F.\nEnjoy!")
                );
                break;
                default:
                    return;
        }

        plugin.getBookUtil().openBook(player, LazyUtil.getBook(bookMeta));
    }
}
