package me.robomwm.MountainDewritoes;

import com.robomwm.grandioseapi.player.GrandPlayer;
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
import pw.valaria.bookutil.BookUtil;

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
                LazyUtil.getHoverable("                       ? ",
                        "Everything in " + ChatColor.DARK_AQUA + "dark aqua" + ChatColor.RESET +
                                " is hoverable\n" +
                        "Everything in " + ChatColor.AQUA + "aqua" + ChatColor.RESET + " is clickable."),
                LazyUtil.getClickableCommand(" ⚙ ", "/help settings","Settings"),
                LazyUtil.getClickableCommand(" ℹ ", "/help about","About+Info (not implemented)"),
                LazyUtil.getClickableURL(" # ", "http://r.robomwm.com/mememap","Open the LIVE Map\n" +
                        "and IRC chatroom"),
                "\n",
                LazyUtil.getClickableCommand("        Minigames Hub      \n", "/minigames"),
                LazyUtil.getClickableCommand("              Warps           \n", "/warp <warp>"),
                LazyUtil.getClickableCommand("       Items+Recipes   \n", "", "Not implemented yet"),
                LazyUtil.getClickableCommand("           Emoticons         \n", "/emote"),
                LazyUtil.getClickableCommand("             Tip Jar         \n", "/tip"),
                LazyUtil.getClickableCommand("          Claim Posts       \n", "/help post", "/help post"),
                LazyUtil.getClickableCommand("         tp ", "/tp"),
                ChatColor.BLACK + "and",
                LazyUtil.getClickableCommand(" tppost      \n", "/tppost"),
                ChatColor.BLACK + "\n        Vocal Callouts:\n",
                LazyUtil.getClickableCommand(" Hello! ", "/v hello"),
                LazyUtil.getClickableCommand(" Thanks! ", "/v thanks"),
                LazyUtil.getClickableCommand(" Ok! ", "/v okay"),
                LazyUtil.getClickableCommand(" Lol! \n", "/v haha"),
                LazyUtil.getClickableCommand(" Over here! ", "/v overhere"),
                LazyUtil.getClickableCommand(" Help! ", "/v help"),
                LazyUtil.getClickableCommand(" More...", "/voice")));
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.RED + "     MLG Fortress\n",
                LazyUtil.getClickableURL("IRC (chatroom)\n", "http://r.robomwm.com/mememap", "/irc"),
                LazyUtil.getClickableURL("Live Map\n", "http://r.robomwm.com/mememap", "/map"),
                LazyUtil.getClickableURL("Dumcord\n", "https://discord.gg/3TXnkfa", "/dumcord"),
                LazyUtil.getClickableCommand("Clan commands\n", "/clan"),
                LazyUtil.getClickableCommand("Tacos\n", "/taco", "/taco <player>"),
                LazyUtil.getClickableCommand("Shops\n", "/shop")));
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.RED + "     MLG Fortress\n",
                LazyUtil.getClickableCommand("Autocraft Airships\n", "/einfo autocraft", "/ac"),
                LazyUtil.getClickableCommand("Element Bending\n", "/einfo bending"),
                LazyUtil.getClickableCommand("Slimefun Guide\n", "/sf guide")));
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.RED + "     MLG Fortress\n",
                "There's a lot more... I means there's like 150+ plugins...\nSo uh yea pls /apply cuz uh this is a lot to do but um yea??"));
        book.setItemMeta(bookMeta);

        //claim posts
        BookMeta postMeta = LazyUtil.getBookMeta();
        postMeta.spigot().addPage(LazyUtil.buildPage(
                LazyUtil.getClickableCommand("←                           \n","/help","Back to /menu"),
                "Claim posts exist in known worlds with a beacon.\n",
                "\nCaptured posts provide ",
                LazyUtil.getHoverable("protection", "- Cannot build\n- Can only break blocks with tools\n- Tools+armor take more damage\n- Teleportation is restricted"),
                ChatColor.BLACK + " and ",
                LazyUtil.getClickableCommand("teleportation.\n", "/tppoint"),
                "\nClan members and allies share claim posts."));
        postMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.DARK_BLUE + "Upgrading claim posts\n\n",
                "Right-click the beacon to add upgrades:\n",
                LazyUtil.getHoverable("Emerald Blocks\n", "Increases post health\n(More breaks required to capture)"),
                LazyUtil.getHoverable("Diamond Blocks\n", "Decreases vulnerability time\nMinimum of 5 minutes.\n(Less time given to capture)"),
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
            case "settings":
                openSettings(player);
                return true;
            case "post":
            case "point":
            case "claim":
            case "claimpost":
            case "claimpoint":
            case "tppoint":
                plugin.getBookUtil().openBook(player, post);
                break;
            case "give":
                if (player.isOp())
                {
                    player.getInventory().addItem(book.clone());
                    return true;
                }
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

    private void openSettings(Player player)
    {
        BookMeta bookMeta = LazyUtil.getBookMeta();
        GrandPlayer grandPlayer = plugin.getGrandioseAPI().getGrandPlayerManager().getGrandPlayer(player);

        bookMeta.spigot().addPage(LazyUtil.buildPage(LazyUtil.getClickableCommand("←                           ","/help","Back to /menu"),
                LazyUtil.getClickableURL(" ◶ \n", "http://mlg.robomwm.com:28500/player/" + player.getName(),"My Stats"),
                player.getDisplayName() + "'s settings\n",
                LazyUtil.getClickableCommand("View distance: " + player.getViewDistance(), "/view"),
                "\n",
                LazyUtil.getClickableCommand("Name color: " + grandPlayer.getNameColor() + grandPlayer.getNameColor().name().toLowerCase(), "/name"),
                "\n",
                LazyUtil.getClickableCommand("Music: on", "", "Not implemented yet")));

        plugin.getBookUtil().openBook(player, LazyUtil.getBook(bookMeta));
    }
}
