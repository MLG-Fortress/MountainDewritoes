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
    private ItemStack post;

    public LetsStart(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta)book.getItemMeta();
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.DARK_PURPLE + "IP: MLG.ROBOMWM.COM\n",
                "\n\n\n\n",
                LazyUtil.getClickableCommand("       Minigames hub      \n", "/minigames"),
                LazyUtil.getClickableCommand("           Emoticons         \n", "/emote"),
                LazyUtil.getClickableCommand("                TP             \n", "/tp", "/tp>"),
                LazyUtil.getClickableCommand("          Claim Posts       \n", "/help post", "/help post"),
                LazyUtil.getClickableCommand("             Warps           \n", "/warp <warp>"),
                LazyUtil.getClickableCommand("        Voice callouts      \n", "/v"),
                LazyUtil.getClickableCommand("           Get a tip         \n", "/tip")));
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
        BookMeta postMeta = LazyUtil.getBookMeta();
        postMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.DARK_BLUE + "Claim posts\n(aka Capture points)\n\n",
                "Claim posts can be found in known worlds with an unblockable beacon.\n",
                "Capture a post via breaking it.\n",
                "Captured posts provide ",
                LazyUtil.getHoverable("protection", "- Cannot build\n-Can only break blocks with tools\n-Tools+armor take more damage.\n-Teleportation is restricted."),
                ChatColor.BLACK + " and ",
                LazyUtil.getClickableCommand(" teleportation", "/tppoint"),
                "\nClaim posts are shared amongst clan members and allies."));
        postMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.DARK_BLUE + "Upgrading claim posts\n\n",
                "Right-click the beacon to add upgrades:\n",
                LazyUtil.getHoverable("Emerald Blocks\n", "Increases post health\n(More breaks required to capture)"),
                LazyUtil.getHoverable("Diamond Blocks\n", "Decrease vulnerability time\n(Less time given to capture)\nMinimum of 5 minutes."),
                LazyUtil.getHoverable("Iron Blocks\n", "Not implemented"),
                LazyUtil.getHoverable("Gold Blocks\n", "Not implemented\nMaybe recruits mobs to help defend??"),
                LazyUtil.getHoverable("Redstone Blocks", "Not implemented")));
        post = LazyUtil.getBook(postMeta);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player)sender;
        if (args.length == 0)
            return openStartBook(player);

        switch (args[0].toLowerCase())
        {
            case "post":
            case "point":
            case "claim":
            case "claimpost":
            case "claimpoint":
            case "tppoint":
                plugin.getBookUtil().openBook(player, post);
                break;
            default:
                return openStartBook(player);
        }
        return true;
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
