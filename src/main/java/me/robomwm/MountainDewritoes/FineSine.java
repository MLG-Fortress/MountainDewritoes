package me.robomwm.MountainDewritoes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.Event;
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
    private final Component FINE_SINE_COMPONENT = LegacyComponentSerializer.legacySection().deserialize(FINE_SINE_LABEL);

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
        if (PlainTextComponentSerializer.plainText().serialize(event.line(0)).equals("FINESINE"))
        {
            event.line(0, FINE_SINE_COMPONENT);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!(event.getBlock().getState() instanceof Sign sine))
                        return;
                    wax(sine);
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
        if (!LegacyComponentSerializer.legacySection().serialize(side.line(0)).equals(FINE_SINE_LABEL))
            return;
        wax(sine);
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }

        //I'd String.join but gotta get rid of the first line somehow
        String line1 = PlainTextComponentSerializer.plainText().serialize(side.line(1));
        String line2 = PlainTextComponentSerializer.plainText().serialize(side.line(2));
        String line3 = PlainTextComponentSerializer.plainText().serialize(side.line(3));
        StringBuilder command = new StringBuilder(line1);
        if (!line2.isEmpty())
            command.append(" ").append(line2);
        if (!line3.isEmpty())
            command.append(" ").append(line3);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                event.getPlayer().chat(command.toString());
            }
        }.runTask(instance);
    }

    private void wax(Sign sine)
    {
        if (sine.isWaxed())
            return;
        sine.setWaxed(true);
        sine.update();
    }
}
