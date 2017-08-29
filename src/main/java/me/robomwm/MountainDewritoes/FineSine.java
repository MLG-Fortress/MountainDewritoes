package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
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
            event.setLine(0, FINE_SINE_LABEL);
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
        if (event.getClickedBlock().getType() != Material.WALL_SIGN && event.getClickedBlock().getType() != Material.SIGN_POST)
            return;
        Sign sine = (Sign)event.getClickedBlock().getState();
        if (!sine.getLine(0).equals(FINE_SINE_LABEL))
            return;

        //I'd String.join but gotta get rid of the first line somehow
        StringBuilder command = new StringBuilder(sine.getLine(1));
        if (!sine.getLine(2).isEmpty())
            command.append(" " + sine.getLine(2));
        if (!sine.getLine(3).isEmpty())
            command.append(" " + sine.getLine(3));

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
