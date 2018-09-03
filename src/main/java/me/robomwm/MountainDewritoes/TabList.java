package me.robomwm.MountainDewritoes;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import to.us.mlgfort.NoMyStuff.NoMyStuff;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private DecimalFormat df = new DecimalFormat("#.##");
    private Set<Player> onlinePlayers = ConcurrentHashMap.newKeySet();

    public TabList(MountainDewritoes plugin)
    {
        this.instance = plugin;
        noMyStuff = (NoMyStuff)plugin.getServer().getPluginManager().getPlugin("NoMyStuff");
        onlinePlayers.addAll(instance.getServer().getOnlinePlayers());
        new BukkitRunnable() //premature optimization is the root of all evils... or rather, a waste of time
        {
            @Override
            public void run()
            {
                for (Player player : instance.getServer().getOnlinePlayers())
                    setTabList(player);
            }
        }.runTaskTimer(instance, 20L, 20L);
    }

    boolean lol = true;

    private void setTabList(Player player)
    {
        if (lol)
        {
            String ping = "over 9000";
            if (noMyStuff != null)
                ping = String.valueOf(noMyStuff.getPingCommand().getPing(player));
            player.setPlayerListHeader(instance.getTipCommand().getRandomColor() + "MLG Fortress\n" +
                    instance.getTipCommand().getRandomColor() +
                    instance.getEconomy().format(instance.getEconomy().getBalance(player)) + TAB +
                    instance.getTipCommand().getRandomColor() + "TPS: " +
                    df.format(instance.getServer().getTPS()[0] * 2D) + TAB +
                    instance.getTipCommand().getRandomColor() + "Ping: " + ping + "ms");

        }
        else
            player.setPlayerListFooter(instance.getTipCommand().getRandomColor() + "IP: " +
                instance.getTipCommand().getRandomColor() + "MLG.ROBOMWM.COM");

        lol = !lol;
    }
}
