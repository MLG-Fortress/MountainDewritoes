package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 7/13/2017.
 *
 * @author RoboMWM
 */
public class FineSine implements Listener
{
    JavaPlugin instance;
    private final String FINE_SINE_LABEL = ChatColor.DARK_BLUE + "\u2503 FINE  SINE \u2503"; //┃

    FineSine(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        instance = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void onCreateSign(SignChangeEvent event)
    {
        if (!event.getPlayer().hasPermission("mlgstaff"))
            return;
        if (event.getLine(0).equals("FINESINE"))
        {
            event.setLine(0, FINE_SINE_LABEL);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!(event.getBlock().getState() instanceof Sign sine))
                        return;
                    sine.setWaxed(true);
                    sine.update();
                }
            }.runTask(instance);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onClickFineSine(PlayerInteractEvent event)
    {
        if (event.getHand() == EquipmentSlot.OFF_HAND) //probably not necessary
            return;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        if (event.getClickedBlock() == null)
            return;
        if (!Tag.SIGNS.isTagged(event.getClickedBlock().getType()) &&
                !Tag.WALL_SIGNS.isTagged(event.getClickedBlock().getType()))
            return;
        Sign sine = (Sign)event.getClickedBlock().getState();
        SignSide side = sine.getTargetSide(event.getPlayer());
        if (!side.getLine(0).equals(FINE_SINE_LABEL))
            return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }

        //I'd String.join but gotta get rid of the first line somehow
        StringBuilder command = new StringBuilder(side.getLine(1));
        if (!side.getLine(2).isEmpty())
            command.append(" ").append(side.getLine(2));
        if (!side.getLine(3).isEmpty())
            command.append(" ").append(side.getLine(3));

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                event.getPlayer().chat(command.toString());
            }
        }.runTask(instance);
    }
}
