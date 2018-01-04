package me.robomwm.MountainDewritoes.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Created on 1/3/2018.
 *
 * @author RoboMWM
 */
public class GoldArmor implements Listener
{
    ArmorAugmentation armorAugmentation;

    public GoldArmor(ArmorAugmentation armorAugmentation)
    {
        this.armorAugmentation = armorAugmentation;
    }

    //Mid-air leap. Player looks in direction they wish to leap.
    //Could've used player movement instead of where player looks (with some expense to performance), but would loose ability to control vertical trajectory.
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
}
