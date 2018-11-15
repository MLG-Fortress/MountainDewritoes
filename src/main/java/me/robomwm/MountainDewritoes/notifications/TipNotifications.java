package me.robomwm.MountainDewritoes.notifications;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import me.robomwm.MountainDewritoes.Events.ScheduledPlayerMovedEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
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

    public TipNotifications(Notifications notifications, MountainDewritoes plugin)
    {
        super(notifications, plugin);
        shopAPI = ((PrettySimpleShop)plugin.getServer().getPluginManager().getPlugin("PrettySimpleShop")).getShopAPI();
    }

    //shop
    @EventHandler(ignoreCancelled = true)
    private void onMoved(ScheduledPlayerMovedEvent event)
    {
        Player player = event.getPlayer();
        Block block = player.getTargetBlock(5);
        if (block == null)
            return;
        player.sendActionBar(block.getType().name());
        BlockState state = block.getState();
        if (!(state instanceof Container))
            return;
        Container container = (Container)state;
        player.sendActionBar(state.getClass().getName() + String.valueOf(shopAPI.isShop(container, false)));
        if (shopAPI.isShop(container, false))
            return;

        ItemStack itemStack = shopAPI.getItemStack(container);
        double price = shopAPI.getPrice(container);

        List<String> lines = new ArrayList<>();

        lines.add(itemStack.getAmount() + " " + PrettySimpleShop.getItemName(itemStack));
        lines.add(" for " + plugin.getEconomy().format(price) + " each ");
        lines.add("Punch chest for more info.");

        addEntry(player, "shop", lines);
    }
}
