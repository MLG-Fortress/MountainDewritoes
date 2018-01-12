package me.robomwm.MountainDewritoes.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    private Map<Player, BukkitTask> sprintingPlayers = new HashMap<>();

    public GoldArmor(ArmorAugmentation armorAugmentation)
    {
        this.armorAugmentation = armorAugmentation;
    }

    @EventHandler
    private void cleanup(PlayerQuitEvent event)
    {
        if (sprintingPlayers.containsKey(event.getPlayer()))
            sprintingPlayers.remove(event.getPlayer()).cancel();
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
        if (!armorAugmentation.shiftAbility(player))
            return;

        player.setVelocity(player.getLocation().getDirection());
    }

    //GOLD LEGGINGS
    //I'm free, free-falling
    //Player experiences less gravitational pull/feels floaty while sprinting
    @EventHandler(ignoreCancelled = true)
    private void onSprint(PlayerToggleSprintEvent event)
    {
        if (!event.isSprinting())
        {
            if (sprintingPlayers.containsKey(event.getPlayer()))
                sprintingPlayers.remove(event.getPlayer()).cancel();
            return;
        }

        sprintingPlayers.put(event.getPlayer(), new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //TODO: Experiment with jump and levitation
            }
        }.runTaskTimer(instance, 0L, 1L));
    }
}
