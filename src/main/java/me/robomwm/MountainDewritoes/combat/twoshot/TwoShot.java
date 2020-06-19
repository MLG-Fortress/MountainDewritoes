package me.robomwm.MountainDewritoes.combat.twoshot;

import com.robomwm.customitemregistry.CustomItemRegistry;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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
    private CustomItemRegistry customItems;
    private Map<String, WeaponProperties> weapons = new HashMap<>();

    public TwoShot(MountainDewritoes plugin)
    {
        this.mountainDewritoes = plugin;

        try
        {
            this.customItems = plugin.getCustomItemRegistry();
            File folder = new File(plugin.getDataFolder() + File.separator + "weapons");
            folder.mkdir();
            for (File file : folder.listFiles()) //TODO: ignore non .yml files
            {
                weapons.put(FilenameUtils.getBaseName(file.getName()), new WeaponProperties(file));
                mountainDewritoes.getLogger().info(file.getName());
            }
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
        catch (Throwable rock)
        {
            plugin.getLogger().warning("Could not load weapons :c");
            rock.printStackTrace();
        }
    }

    private WeaponState getWeaponState(ItemStack item)
    {
        if (item == null)
            return null;

        String name = customItems.extractCustomID(item.getItemMeta());

        if (name == null)
            return null;

        WeaponProperties weaponProperties = weapons.get(name);
        if (weaponProperties == null)
            return null;

        return new WeaponState(item, weaponProperties);
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
                    return;
            default:
                return;
        }

        WeaponState weapon = getWeaponState(event.getItem());
        if (weapon == null)
            return;

        event.getPlayer().sendActionBar("got WeaponState");

        if (weapon.canFire(mountainDewritoes.getCurrentTick()))
            weapon.getProperties().fire(event.getPlayer(), mountainDewritoes);

        if (weapon.getRemainingAmmo() <= 0)
            reloadWeapon(weapon, event.getPlayer(), mountainDewritoes);
    }

    @EventHandler
    public void reloadWeapon(PlayerDropItemEvent event)
    {

    }

    public void reloadWeapon(WeaponState weapon, Player player, Plugin plugin)
    {
        if (weapon.reloadWeapon(plugin))
            player.sendActionBar("Reloading...");
    }
}
