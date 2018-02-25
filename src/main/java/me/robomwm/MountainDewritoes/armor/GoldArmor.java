package me.robomwm.MountainDewritoes.armor;

import com.robomwm.customitemrecipes.CustomItemRecipes;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 1/3/2018.
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
        CustomItemRecipes customItems = plugin.getCustomItemRecipes();

        List<String> bootsLore = new ArrayList<>();
        bootsLore.add("The standard gravity-defying doublejump");
        bootsLore.add("");
        bootsLore.add(ChatColor.YELLOW + "Jump+Dive");
        bootsLore.add("Sneak in midair to doublejump.");
        bootsLore.add("Sneak again to airdive.");
        bootsLore.add("");
        bootsLore.add(ChatColor.GRAY + "Passives:");
        bootsLore.add(ChatColor.GRAY + "Fall damage protection");
        bootsLore.add(ChatColor.GRAY + "No power cost");
        customItems.registerItem(customItems.loreize(new ItemStack(Material.GOLD_BOOTS), bootsLore), "goldBoots");
        ShapedRecipe bootsRecipe = customItems.getShapedRecipe(plugin, "goldBoots");
        bootsRecipe.shape("gag", "gag").setIngredient('g', Material.GOLD_INGOT).setIngredient('a', Material.AIR);
        plugin.getServer().addRecipe(bootsRecipe);

        List<String> leggingsLore = new ArrayList<>();
        leggingsLore.add("ur 2 slow");
        leggingsLore.add("");
        leggingsLore.add(ChatColor.YELLOW + "Sonic Dash");
        leggingsLore.add("At full power, sprint to dash.");
        customItems.registerItem(customItems.loreize(new ItemStack(Material.GOLD_LEGGINGS), leggingsLore), "goldLeggings");
        ShapedRecipe leggingsRecipe = customItems.getShapedRecipe(plugin, "goldLeggings");
        leggingsRecipe.shape("ggg", "gag", "gag").setIngredient('g', Material.GOLD_INGOT).setIngredient('a', Material.AIR);
        plugin.getServer().addRecipe(leggingsRecipe);
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
            player.setVelocity(vector.subtract(NSA.getLastLocation(player).toVector()).multiply(2.5D).setY(0.7D));
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
