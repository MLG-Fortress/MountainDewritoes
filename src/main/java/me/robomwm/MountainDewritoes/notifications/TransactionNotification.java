package me.robomwm.MountainDewritoes.notifications;

import me.robomwm.MountainDewritoes.Events.TransactionEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 11/4/2018.
 *
 * @author RoboMWM
 */
public class TransactionNotification extends NotificationSender
{
    public TransactionNotification(Notifications notifications, MountainDewritoes plugin)
    {
        super(notifications, plugin);
    }

    @EventHandler
    private void onTransaction(TransactionEvent event)
    {
        Player player = event.getPlayer();

        List<String> lines = new ArrayList<>();

        if (event.getAmount() > 0)
        {
            lines.add("Credit:  " + ChatColor.GREEN + "+" + event.getEconomy().format(event.getAmount()));
            player.playSound(player.getLocation(), "fortress.credit", SoundCategory.PLAYERS, 300000f, 1.0f);
        }
        else if (event.getAmount() < 0)
        {
            lines.add("Debit:   " + ChatColor.RED + event.getEconomy().format(event.getAmount()));
            player.playSound(player.getLocation(), "fortress.debit", SoundCategory.PLAYERS, 300000f, 1.0f);
        }

        lines.add("Balance:  " + event.getEconomy().format(event.getEconomy().getBalance(player)));

        addEntry(player, "balance", lines);
    }
}
