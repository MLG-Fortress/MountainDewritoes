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
                        ChatColor.AQUA + "Aqua" + ChatColor.RESET + " is hoverable (like this one!).\n" +
                        ChatColor.DARK_AQUA + "Dark aqua" + ChatColor.RESET + " is clickable.\n" +
                                "FYI, you can open this /menu by pressing F.\n" +
                                "Swap items by holding sneak when pressing F\n"),
                LazyUtil.getClickableCommand(" ⚙ ", "/help settings","Settings"),
                LazyUtil.getClickableCommand(" ℹ ", "/help about","About+Stats"),
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
                ChatColor.BLACK + "        Vocal Callouts:\n",
                LazyUtil.getClickableCommand(" Hello! ", "/v hello"),
                LazyUtil.getClickableCommand(" Thanks! ", "/v thanks"),
                LazyUtil.getClickableCommand(" Ok! ", "/v okay"),
                LazyUtil.getClickableCommand(" Lol! \n", "/v haha"),
                LazyUtil.getClickableCommand(" Over here! ", "/v overhere"),
                LazyUtil.getClickableCommand(" Dis wae! \n", "/v followme"),
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
                LazyUtil.getClickableCommand("AutoCraft Airships\n", "/einfo autocraft", "/ac"),
                LazyUtil.getClickableCommand("Element Bending\n", "/einfo bending"),
                LazyUtil.getClickableCommand("Slimefun Guide\n", "/sf guide")));
        bookMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.RED + "     MLG Fortress\n",
                "There's a lot more... I means there's like 150+ plugins...\nSo uh yea pls /apply cuz uh this is a lot to do but um yea??"));
        book.setItemMeta(bookMeta);

        //claim posts
        BookMeta postMeta = LazyUtil.getBookMeta();
        postMeta.spigot().addPage(LazyUtil.buildPage(
                LazyUtil.getClickableCommand("⬅Back                        \n","/help","Back to /menu"),
                "Claim posts exist in known worlds with a beacon.\n",
                "\nCaptured posts provide ",
                LazyUtil.getHoverable("protection", "- Cannot build\n- Can only break blocks with tools\n- Tools+armor take more damage\n- Teleportation is restricted"),
                ChatColor.BLACK + " and ",
                LazyUtil.getClickableCommand("teleportation.\n", "/tppoint"),
                "\nClan members and allies share claim posts."));
        postMeta.spigot().addPage(LazyUtil.buildPage(ChatColor.DARK_BLUE + "Upgrading claim posts\n\n",
                "Right-click the beacon to add upgrades:\n",
                LazyUtil.getHoverable("Emerald Blocks\n", "Increases post health\n(More breaks required to capture)"),
                LazyUtil.getHoverable("Redstone Blocks", "Not implemented\nFuel\nIncreases lockout time\nLockout occurs on failed captures,\nmaking the post invulnerable to capture.\n10% of the fuel is consumed per lockout."),
                LazyUtil.getHoverable("Prismarine", "Not implemented\nChance of applying mining haste when enemy players break blocks.\nChance of applying, effectiveness, and duration increase when closer to the post.\nConsumed only when applying a strong effect."),
                LazyUtil.getHoverable("Diamond Blocks\n", "Not implemented\nIdeas??"),
                LazyUtil.getHoverable("Iron Blocks\n", "Not implemented\nRecruit Iron Golems (3 blocks per golem)"),
                LazyUtil.getHoverable("Gold Blocks\n", "Not implemented\nBuy zomblings")));
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
            case "about":
                openAbout(player);
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
                    player.getInventory().addItem(book.clone());
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
    private void onPressF(PlayerSwapHandItemsEvent event)
    {
        if (event.getPlayer().isSneaking())
            return;
        event.setCancelled(openStartBook(event.getPlayer()));
    }

    private void openSettings(Player player)
    {
        BookMeta bookMeta = LazyUtil.getBookMeta();
        GrandPlayer grandPlayer = plugin.getGrandioseAPI().getGrandPlayerManager().getGrandPlayer(player);

        bookMeta.spigot().addPage(LazyUtil.buildPage(LazyUtil.getClickableCommand("⬅ ","/help","Back to /menu"),
                player.getDisplayName() + ChatColor.BLACK + "'s settings\n",
                LazyUtil.getClickableCommand("View distance: " + player.getViewDistance(), "/view"), "\n",
                LazyUtil.getClickableCommand("Name color: " + grandPlayer.getNameColor() + grandPlayer.getNameColor().name().toLowerCase(), "/name"), "\n",
                LazyUtil.getClickableCommand("Music: on", "", "Not implemented yet"), "\n",
                LazyUtil.getClickableCommand("SneakPickup: " + getOnOff(player.hasMetadata("SNEAKPICKUP")), "/sneakpickup", "/sneakpickup\nPick up items only when sneaking."), "\n"
                ));

        plugin.getBookUtil().openBook(player, LazyUtil.getBook(bookMeta));
    }

    private void openAbout(Player player)
    {
        BookMeta aboutMeta = LazyUtil.getBookMeta();
        aboutMeta.spigot().addPage(LazyUtil.buildPage(
                LazyUtil.getClickableCommand("⬅Back   ","/help","Back to /menu"),
                ChatColor.DARK_PURPLE + "About+Info\n",
                ChatColor.BLACK + "IP: MLG.ROBOMWM.COM\n",
                LazyUtil.getClickableURL("- Website\n", "http://techfortress.robomwm.com/p/mlg-base.html", "Fancy."),
                LazyUtil.getClickableURL("- Ur Stats\n", "http://mlg.robomwm.com:28500/player/" + player.getName(), "ayyy"), ChatColor.BLACK +
                "Dis started as Trash Tech back in 2015, except the first guy I opped built a spawn. Then wuz " +
                        "Mini Fortress as Minigames took focus. Then it now b MLG Fortress 4 da memez."));
        aboutMeta.spigot().addPage(LazyUtil.buildPage(LazyUtil.getClickableCommand("⬅Back   ","/help","Back to /menu"),
                ChatColor.DARK_PURPLE + "About+Info\n",
                "Credits:\n",
                LazyUtil.getHoverable("Yoreni, ", "Built and setup prison\nCreated 2 plugins just for this server\nImplemented my requests :o\nBasically an \"actual\" developer (like me)"),
                LazyUtil.getHoverable("TheDogeGamer, ", "A ton o stuff\nSome guns\nAll dem trading cardz\nA lot of quotes on \nda server list ping motd.\nEssentials messages (blame him)\nBasically all around helpful \nand now all around inactive."),
                LazyUtil.getHoverable("Gamewalkerz, ", "Built the /minigames hub\nSetup AreaShop (rentable shops)\nOther stuff idk he's inactive"),
                LazyUtil.getHoverable("xKittyTheKillerx, ", "Built tutorial\nSetup dumcord"),
                LazyUtil.getHoverable("MMM10, ", "Who even knows\nRandom Fish\nRandom blocks\nBroke the server a few times by installing random, poorly-made plugins\nThinks his mobcatcher is kewl."),
                LazyUtil.getHoverable("Etanarvazac, ", "Tweaked plugins\nSetup dumcord\nuh can't recall it's been _a while_"),
                LazyUtil.getHoverable("MakisMigee123, ", "Augmented element bending\nInstructed players on element bending\nAttempted to resolve bugs/console spam\n(Seriously I usually end up wasting a lot of time fixing that stuff)"),
                " and others but idk right now, you should PR and add urself here or something."));
        aboutMeta.spigot().addPage(LazyUtil.buildPage(LazyUtil.getClickableCommand("⬅Back   ","/help","Back to /menu"),
                ChatColor.DARK_PURPLE + "About+Info\n",
                LazyUtil.getClickableURL("Github: All dem custom codez\n", "https://github.com/MLG-Fortress/", "Too much wasted time."),
                LazyUtil.getClickableURL("Serbur Stats\n", "http://mlg.robomwm.com:28500/server/MLG_Fortress", "Serbur Stalkin"),
                LazyUtil.getClickableURL("Ur Stats\n", "http://mlg.robomwm.com:28500/player/" + player.getName(), "ayyy")
                ));
        plugin.getBookUtil().openBook(player, LazyUtil.getBook(aboutMeta));
    }

    private String getOnOff(boolean bool)
    {
        if (bool)
            return "on";
        return "off";
    }
}
