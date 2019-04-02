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
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 1/10/2019.
 *
 * @author RoboMWM
 *
 */
public class TwoShot implements Listener
{
    private MountainDewritoes mountainDewritoes;
    private CustomItemRecipes customItems;
    private Map<String, Weapon> weapons = new HashMap<>();


    public TwoShot(MountainDewritoes plugin)
    {
        this.mountainDewritoes = plugin;

        try
        {
            this.customItems = plugin.getCustomItemRecipes();
            File folder = new File(plugin.getDataFolder() + File.separator + "weapons");
            folder.mkdir();
            for (File file : folder.listFiles())
                weapons.put(file.getName(), new Weapon(file));
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
        catch (Throwable rock)
        {
            plugin.getLogger().warning("Could not load weapons :c");
            rock.printStackTrace();
        }
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

        //TODO: determine current ammo, last fired, etc.
        ItemStack itemStack = event.getItem();
        if (itemStack == null)
            return;

        String name = customItems.extractCustomID(itemStack.getItemMeta());

        if (name == null)
            return;

        Weapon weapon = weapons.get(name);
        weapon.fire(event.getPlayer());
        weapon.playSound(event.getPlayer(), mountainDewritoes);
    }
}
