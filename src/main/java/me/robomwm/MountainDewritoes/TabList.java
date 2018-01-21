package me.robomwm.MountainDewritoes;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import to.us.mlgfort.NoMyStuff.NoMyStuff;

/**
 * Created on 8/28/2017.
 *
 * @author RoboMWM
 */
public class TabList implements Listener
{
    private String TAB = "    ";
    private MountainDewritoes instance;
    private NoMyStuff noMyStuff;
    public TabList(MountainDewritoes plugin)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        noMyStuff = (NoMyStuff)plugin.getServer().getPluginManager().getPlugin("NoMyStuff");
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                setTabList(event.getPlayer());
            }
        }.runTaskTimer(instance, 1L, 200L);
    }

    private void setTabList(Player player)
    {
        String ping = "over 9000";
        if (noMyStuff != null)
            ping = String.valueOf(noMyStuff.getPingCommand().getPing(player));

        player.setPlayerListHeaderFooter(
                TextComponent.fromLegacyText(instance.getTipCommand().getRandomColor() + "MLG Fortress\n" +
                        instance.getTipCommand().getRandomColor() + instance.getEconomy().getBalance(player) + TAB +
                        instance.getTipCommand().getRandomColor() + "Ping: " + ping + "ms"),
                TextComponent.fromLegacyText(ChatColor.AQUA + "IP: MLG.ROBOMWM.COM"));
    }
}
