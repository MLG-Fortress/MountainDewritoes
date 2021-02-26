package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Commands.PseudoCommands;
import me.robomwm.MountainDewritoes.Commands.TipCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 8/28/2017.
 *
 * @author RoboMWM
 */
public class TabList implements Listener
{
    private String TAB = "    ";
    private MountainDewritoes instance;
    private DecimalFormat df = new DecimalFormat("#.##");

    public TabList(MountainDewritoes plugin)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    //does not scale, but is more efficient with smaller player loads and avoids unnecessary object creation
    //I used a global task that scheduled as appropriate depending getOnlinePlayers
    //However, it seems that it is somewhat expensive(?) to call this on low-end servers (even if empty!)
    //If need be, I can revert to that, but until then, this should be fine anyway.
    //Ideally this would be done async, with any relevant data calls filled in concurrently/atomically
    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!event.getPlayer().isOnline())
                {
                    this.cancel();
                    return;
                }

                setTabList(event.getPlayer());
            }
        }.runTaskTimer(instance, 20L, 2L);
    }

    private void setTabList(Player player)
    {
        if (ThreadLocalRandom.current().nextBoolean())
            player.setPlayerListHeader(colorizer("MLG ", "Fortress", TAB, "Lag: ",
                    lagMeter(), "\n",
                    instance.getEconomy().format(instance.getEconomy().getBalance(player)), TAB,
                    "Ping: ", PseudoCommands.getPing(player), "\n",
                    "Coordinates: (",
                    df.format(player.getLocation().getX()), ", ", df.format(player.getLocation().getZ()), ")"));
        else
            player.setPlayerListFooter(colorizer("IP: ", "MLG", ".", "ROBOMWM", ".", "COM"));
    }

    private String colorizer(String... args)
    {
        StringBuilder stringBuilder = new StringBuilder(TipCommand.getRandomColor().toString());
        for (String arg : args)
        {
            if (ThreadLocalRandom.current().nextBoolean())
                stringBuilder.append(TipCommand.getRandomColor().toString());
            stringBuilder.append(arg);
        }
        return stringBuilder.toString();
    }

    private String lagMeter()
    {
        int tps = (int)instance.getServer().getTPS()[0];
        if (tps < 0)
            return "⌚ we're goin backwards!!";

        switch (tps)
        {
            case 0:
                return "✖ in a coma";
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return "✖ dyin";
            case 8:
            case 9:
            case 10:
                return "✖ Ow";
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return "⚠ Strugglin";
            case 16:
            case 17:
                return "⚠ Some";
            case 18:
            case 19:
            case 20:
            case 21:
                return "✔ Nope";
            default:
                return "☺ Not anymore";
        }
    }
}
