package me.robomwm.MountainDewritoes.armor;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Created on 1/3/2018.
 *
 * Chestplate: Grants jump boost 2
 * Leggings: "Super Sprint." Quickly dash for a second (speed boost 30). Consumes 10 doritos
 * Boots: Double jump. Sneak again to air-dive.
 *
 * @author RoboMWM
 */
public class GoldArmor implements Listener
{
    private ArmorAugmentation armorAugmentation;
    private final PotionEffect chestplateEffect = new PotionEffect(PotionEffectType.JUMP, 80, 2, true, false);

    GoldArmor(MountainDewritoes plugin, ArmorAugmentation armorAugmentation)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.armorAugmentation = armorAugmentation;
//        new BukkitRunnable()
//        {
//            @Override
//            public void run()
//            {
//                for (Player player : plugin.getServer().getOnlinePlayers())
//                {
//                    ItemStack chestplate = player.getInventory().getChestplate();
//                    if (chestplate == null)
//                        continue;
//                    if (chestplate.getType() == Material.GOLD_CHESTPLATE &&
//                            (!player.hasPotionEffect(PotionEffectType.JUMP) || player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() <= 2))
//                    {
//                        player.removePotionEffect(PotionEffectType.JUMP);
//                        player.addPotionEffect(chestplateEffect);
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 40L, 40L);
    }

    /* GOLD BOOTS */
    //Mid-air jump
    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();

        if (!event.isSneaking() || event.getPlayer().isOnGround())
            return;
        if (!armorAugmentation.isEquipped(player, Material.GOLDEN_BOOTS))
            return;

        Integer jump = NSA.getMidairMap().get(player);

        if (jump == null)
        {
            NSA.getMidairMap().put(player, 1);
            Vector vector = player.getLocation().toVector();
            player.setVelocity(vector.subtract(NSA.getLastLocation(player).toVector()).multiply(1.7D).setY(0.7D));
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

    @EventHandler(ignoreCancelled = true)
    public void onSprint(PlayerToggleSprintEvent event)
    {
        Player player = event.getPlayer();
        if (player.hasPotionEffect(PotionEffectType.SPEED))
            return;
        int length = armorAugmentation.usePowerAbility(event, Material.GOLDEN_LEGGINGS) * 2;
        if (length == 0)
            return;
        player.setMetadata("nocheatplus.checks.moving.survivalfly", new FixedMetadataValue(armorAugmentation.getPlugin(), true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, length, 10, true, false));
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.removeMetadata("nocheatplus.checks.moving.survivalfly", armorAugmentation.getPlugin());
            }
        }.runTaskLater(armorAugmentation.getPlugin(), length);
    }
}
