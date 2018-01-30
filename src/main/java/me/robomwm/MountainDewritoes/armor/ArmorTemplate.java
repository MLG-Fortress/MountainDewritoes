package me.robomwm.MountainDewritoes.armor;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

/**
 * Created on 1/30/2018.
 *
 * @author RoboMWM
 */
public interface ArmorTemplate
{
    void onSneak(PlayerToggleSneakEvent event, Player player);
    void onSprint(PlayerToggleSprintEvent event, Player player);
}
