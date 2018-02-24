package me.robomwm.MountainDewritoes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

/**
 * Created on 2/24/2018.
 *
 * @author RoboMWM
 */
public class CustomRecipes
{
    public CustomRecipes(JavaPlugin plugin)
    {
        Iterator<Recipe> recipeIterator = plugin.getServer().recipeIterator();
        while (recipeIterator.hasNext())
        {
            ItemStack itemStack = recipeIterator.next().getResult();
            if (itemStack.getType() == Material.GOLD_BOOTS)
            {
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("test");
                itemStack.setItemMeta(itemMeta);
                plugin.getLogger().info("attempted to modify " + itemStack.toString());
            }
        }
    }
}
