package me.robomwm.MountainDewritoes;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Robo on 2/13/2016.
 */
public class Main extends JavaPlugin
{
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new SimpleClansListener(this), this);
    }
}
