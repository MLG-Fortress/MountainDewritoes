package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.World;
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
 * Created by RoboMWM on 6/1/2016.
 * All things related to shopping in da memetastic mall
 */
public class ShoppingMall implements Listener
{
    private MountainDewritoes instance;
    private World mallWorld;
    public ShoppingMall(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        mallWorld = instance.getServer().getWorld("mall");
    }

    /**
     * Set walking speed when entering or leaving mall
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onWorldChange(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        //Reset speed when leaving mall
        if (event.getFrom().equals(mallWorld))
        {
            player.setWalkSpeed(0.2f);
            return;
        }

        //Increase speed when entering mall
        if (player.getWorld().equals(mallWorld))
        {
            player.setWalkSpeed(0.5f);
        }
    }

    /**
     * Set walking speed if player joins inside mall
     */
    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerJoinInMall(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        new BukkitRunnable()
        {
            public void run()
            {
                if (player.getWorld().equals(mallWorld))
                    player.setWalkSpeed(0.5f);
            }
        }.runTaskLater(instance, 2L);
    }

    @EventHandler
    private void onPlayerTerminal(PlayerInteractEvent event)
    {
        if (event.getPlayer().getWorld() != mallWorld)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        BookMeta bookMeta;

        switch (event.getClickedBlock().getType())
        {
            case COMMAND_BLOCK:
                bookMeta = LazyText.getBookMeta();
                bookMeta.spigot().addPage(
                        LazyText.buildPage("Ayyy " + player.getDisplayName() + ChatColor.BLACK +
                                "\nWelcome 2 da /mall. U can do ur usual shopping stuff like buying kewl neu items. U can also setup a /shop and git rich quik, if ur gud.",
                                "\n\nDon't forget dat u can press F to open da menu at any time!"));
                break;
            default:
                return;
        }

        instance.openBook(player, LazyText.getBook(bookMeta));
    }
}
