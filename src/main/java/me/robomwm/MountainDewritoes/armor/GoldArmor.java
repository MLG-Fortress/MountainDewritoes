package me.robomwm.MountainDewritoes.armor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
/**
 * Created on 1/3/2018.
 *
 * @author RoboMWM
 */
public class GoldArmor implements Listener
{
    private JavaPlugin instance;
    private ArmorAugmentation armorAugmentation;
    private Map<Player, Location> lastLocation = new HashMap<>();

    public GoldArmor(ArmorAugmentation armorAugmentation)
    {
        this.armorAugmentation = armorAugmentation;
    }

    @EventHandler
    private void cleanup(PlayerQuitEvent event)
    {
        lastLocation.remove(event.getPlayer());
    }

    /* GOLD BOOTS */
    //Mid-air leap. Player looks in direction they wish to leap.
    //Could've used player movement instead of where player looks (with some expense to performance), but would lose ability to control vertical trajectory.
    @EventHandler(ignoreCancelled = true)
    private void onSneak(PlayerToggleSneakEvent event)
    {
        if (!event.isSneaking())
            return;

        Player player = event.getPlayer();

        if (!armorAugmentation.isEquipped(player, Material.GOLD_BOOTS))
            return;
        if (!armorAugmentation.sneakAbility(player))
            return;

        player.setVelocity(lastLocation.get(player).subtract(player.getLocation()).toVector().setY(0.5D));
        //player.setVelocity(player.getLocation().getDirection());
    }

    //TODO: experimental and probably will need optimization
    @EventHandler(ignoreCancelled = true)
    private void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        if (!armorAugmentation.isEquipped(player, Material.GOLD_BOOTS))
            return;
        lastLocation.put(player, event.getTo());
    }

    //GOLD LEGGINGS
    //I'm free, free-falling
    //Player experiences less gravitational pull/feels floaty while sprinting
    @EventHandler(ignoreCancelled = true)
    private void onSprint(PlayerToggleSprintEvent event)
    {
        if (!event.isSprinting())
            return;
    }
}
