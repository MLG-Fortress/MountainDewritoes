package me.robomwm.MountainDewritoes.combat.twoshot;

import com.robomwm.customitemrecipes.CustomItemRecipes;
import com.robomwm.usefulutil.UsefulUtil;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created on 1/10/2019.
 *
 * @author RoboMWM
 */
public class TwoShot implements Listener
{
    private MountainDewritoes mountainDewritoes;
    private CustomItemRecipes customItems;


    public TwoShot(MountainDewritoes plugin)
    {
        customItems = plugin.getCustomItemRecipes();
        YamlConfiguration config = UsefulUtil.loadOrCreateYamlFile(plugin, "twoshot.yml");

    }

    @EventHandler(ignoreCancelled = true)
    public void onFireWeapon(PlayerInteractEvent event)
    {
        switch (event.getAction())
        {
            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
                break;
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
                if (event.getClickedBlock().getType().isInteractable())
                    break;
            default:
                return;
        }
    }
}
