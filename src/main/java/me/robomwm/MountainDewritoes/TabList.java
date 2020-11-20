package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Commands.PseudoCommands;
import me.robomwm.MountainDewritoes.Commands.TipCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
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
        task(20);
    }

    private void task(int delay)
    {
        new BukkitRunnable() //premature optimization is the root of all evils... or rather, a waste of time
        { //I.e. I could probably do something where I get the data sync that I need sync, then set tab async
            @Override
            public void run()
            {
                //for each online player, update tablist 1 tick apart
                //updates "slower" (in perspective of player) as more players come online
                int i = 1;
                for (Player player : instance.getServer().getOnlinePlayers())
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            setTabList(player);
                        }
                    }.runTaskLater(instance, i++);
                }
                //one tick breather before doing it all again!
                task(++i);
            }
        }.runTaskLater(instance, delay);
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
                return ""
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return "⚠ Yup";
            case 16:
            case 17:
                return "⚠ Some";
            case 18:
            case 19:
            case 20:
            case 21:
                return "✔ Nope";
            default:
                return "Not anymore ☺";
        }
    }
}
