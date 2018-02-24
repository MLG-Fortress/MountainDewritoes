package me.robomwm.MountainDewritoes.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created on 2/24/2018.
 *
 * @author RoboMWM
 */
public class CustomRecipes
{
    public CustomRecipes(JavaPlugin plugin)
    {
        List<Recipe> existingRecipes = new LinkedList<>();
        Iterator<Recipe> recipeIterator = plugin.getServer().recipeIterator();
        while (recipeIterator.hasNext())
        {
            Recipe recipe = recipeIterator.next();
            switch(recipe.getResult().getType())
            {
                case GOLD_BOOTS:
            }
        }
        plugin.getServer().clearRecipes();
        for (Recipe recipe : existingRecipes)
            plugin.getServer().addRecipe(recipe);
    }
}
