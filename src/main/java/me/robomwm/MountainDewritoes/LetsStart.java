package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * Created on 3/10/2018.
 *
 * Graphical interfacerooooooooooooooooossssss!!!!
 *
 * @author RoboMWM
 */
public class LetsStart implements Listener, CommandExecutor
{
    private MountainDewritoes plugin;
    private ItemStack book;

    public LetsStart(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta)book.getItemMeta();
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.DARK_PURPLE + "IP: MLG.ROBOMWM.COM\n",
                "\n\n\n\n",
                LazyUtil.getClickableCommand("          Minigames hub       \n", "/minigames"),
                LazyUtil.getClickableCommand("            Emoticons         \n", "/emote"),
                LazyUtil.getClickableCommand("          Claim points        \n", "/tppoint", "/tppoint <world> <x> <z>"),
                LazyUtil.getClickableCommand("              Warps           \n", "/warp <warp>"),
                LazyUtil.getClickableCommand("          Voice callouts      \n", "/v"),
                LazyUtil.getClickableCommand("            Get a tip         \n", "/tip")));
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.RED + "     MLG Fortress\n",
                LazyUtil.getClickableCommand("Clan commands\n", "/clan"),
                LazyUtil.getClickableCommand("Tacos\n", "/taco", "/taco <player>"),
                LazyUtil.getClickableCommand("Shops\n", "/shop"),
                LazyUtil.getClickableCommand("IRC (chatroom)\n", "/irc"),
                LazyUtil.getClickableCommand("Live Map\n", "/map"),
                LazyUtil.getClickableCommand("Dumcord\n", "/dumcord"),
                LazyUtil.getClickableCommand("Shops\n", "/shop"),
                LazyUtil.getClickableCommand("Warps\n", "/warp")));
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.RED + "     MLG Fortress\n",
                LazyUtil.getClickableCommand("Autocraft Airships\n", "/einfo autocraft", "/ac"),
                LazyUtil.getClickableCommand("Element Bending\n", "/einfo bending"),
                LazyUtil.getClickableCommand("Slimefun Guide\n", "/sf guide")));
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.RED + "     MLG Fortress\n",
                "There's a lot more...\nSo uh yea pls /apply cuz uh this is a lot to do but um yea??dsf"));
        book.setItemMeta(bookMeta);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        return openStartBook((Player)sender);
    }

    private boolean openStartBook(Player player)
    {
        plugin.getBookUtil().openBook(player, book);
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    private void onPressFWhileSneaking(PlayerSwapHandItemsEvent event)
    {
        if (event.getPlayer().isSneaking())
            return;
        event.setCancelled(openStartBook(event.getPlayer()));
    }
}
