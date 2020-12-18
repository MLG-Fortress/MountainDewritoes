package me.robomwm.MountainDewritoes.notifications;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import me.robomwm.MountainDewritoes.Events.ScheduledPlayerMovedEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 11/14/2018.
 *
 * @author RoboMWM
 */
public class TipNotifications extends NotificationSender
{
    private ShopAPI shopAPI;

    public TipNotifications(Notifications notifications, MountainDewritoes plugin, PrettySimpleShop shopPlugin)
    {
        super(notifications, plugin);
        shopAPI = shopPlugin.getShopAPI();
    }

    //shop
    @EventHandler(ignoreCancelled = true)
    private void onMoved(ScheduledPlayerMovedEvent event)
    {
        Player player = event.getPlayer();
        if (!lookingAtShop(player))
            removeEntry(player, "shop");
    }

    private boolean lookingAtShop(Player player)
    {
        Block block = player.getTargetBlock(5);
        if (block == null)
            return false;
        BlockState state = block.getState();
        if (!(state instanceof Container))
            return false;
        Container container = (Container)state;
        if (!shopAPI.isShop(container, false))
            return false;

        ItemStack itemStack = shopAPI.getItemStack(container);
        double price = shopAPI.getPrice(container);
        if (itemStack == null || price < 0)
            return false;

        List<String> lines = new ArrayList<>();

        lines.add(itemStack.getAmount() + " " + PrettySimpleShop.getItemName(itemStack));
        lines.add(ChatColor.YELLOW + plugin.getEconomy().format(price) + " each.");
        lines.add("Punch chest to /buy.");

        addEntry(player, "shop", lines);
        return true;
    }
}
