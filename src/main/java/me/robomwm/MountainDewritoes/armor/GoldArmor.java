package me.robomwm.MountainDewritoes.armor;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Created on 1/3/2018.
 *
 * Chestplate: Grants jump boost 2
 * Leggings: "Super Sprint." Quickly dash for a second (speed boost 30). Consumes 8 doritos
 * Boots: Double jump. Sneak again to air-dive.
 *
 * @author RoboMWM
 */
public class GoldArmor implements Listener
{
    private ArmorAugmentation armorAugmentation;

    GoldArmor(MountainDewritoes plugin, ArmorAugmentation armorAugmentation)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.armorAugmentation = armorAugmentation;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : plugin.getServer().getOnlinePlayers())
                {
                    ItemStack chestplate = player.getInventory().getChestplate();
                    if (chestplate == null)
                        continue;
                    if (chestplate.getType() == Material.GOLD_CHESTPLATE)
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 80, 2, true, false));
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    /* GOLD BOOTS */
    //Mid-air jump
    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();

        if (!event.isSneaking() || event.getPlayer().isOnGround())
            return;
        if (!armorAugmentation.isEquipped(player, Material.GOLD_BOOTS))
            return;

        Integer jump = NSA.getMidairMap().get(player);

        if (jump == null)
        {
            NSA.getMidairMap().put(player, 1);
            Vector vector = player.getLocation().toVector();
            player.setVelocity(vector.subtract(NSA.getLastLocation(player).toVector()).multiply(2.3D).setY(0.7D));
        }
        else
        {
            switch(jump)
            {
                case 1:
                    NSA.getMidairMap().put(player, 2);
                    player.setVelocity(player.getLocation().getDirection().setY(0.3));
                    break;
//                case 2:
//                    NSA.getMidairMap().put(player, 3);
//                    player.setVelocity(new Vector(0, 0.3, 0));
//                    break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onSprint(PlayerToggleSprintEvent event)
    {
        Player player = event.getPlayer();

        if (!armorAugmentation.isFullPower(event, Material.GOLD_LEGGINGS) || player.hasPotionEffect(PotionEffectType.SPEED))
            return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 30, true, false));
        player.setFoodLevel(12);
    }
}
