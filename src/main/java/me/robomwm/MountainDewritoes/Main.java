package me.robomwm.MountainDewritoes;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Robo on 2/13/2016.
 */
public class Main extends JavaPlugin
{
    public void onEnable()
    {
        //Modifies PlayerListName and prefixes
        getServer().getPluginManager().registerEvents(new SimpleClansListener(this), this);
        //Displays "Message Bubbles"
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    }
}
