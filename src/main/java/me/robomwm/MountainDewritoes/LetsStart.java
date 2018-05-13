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
    private ItemStack minigames;
    private ItemStack creativeParkour;
    private ItemStack prison;

    public LetsStart(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta)book.getItemMeta();
        bookMeta.spigot().addPage(LazyText.buildPage(ChatColor.DARK_PURPLE + "IP: MLG.ROBOMWM.COM\n",
                LazyText.hover("            ? ",
                        ChatColor.AQUA + "Aqua" + ChatColor.RESET + " is hoverable (like this one!).\n" +
                        ChatColor.DARK_AQUA + "Dark aqua" + ChatColor.RESET + " is clickable.\n" +
                                "FYI, you can open this /menu by pressing F.\n" +
                                "Swap items by holding sneak when pressing F\n"),
                LazyText.command(" ✉ ", "/mail"),
                LazyText.command(" ⚙ ", "/help settings","Settings"),
                LazyText.command(" ℹ ", "/help about","About+Stats"),
                LazyText.url(" # ", "http://r.robomwm.com/mememap","Open the LIVE Map\n" +
                        "and IRC chatroom"),
                "\n",
                LazyText.command("  Minigames Hub \n", "/minigames"),
                LazyText.command("  Warps \n", "/warp <warp>"),
                LazyText.command("  Items+Recipes \n", "", "Not implemented yet"),
                LazyText.command("  Emoticons \n", "/emote"),
                LazyText.command("  Tip Jar \n", "/tip"),
                LazyText.command("  Claim Posts \n", "/help post", "/help post"),
                LazyText.command("  tp ", "/tp"),
                ChatColor.BLACK + "and",
                LazyText.command(" tppost \n", "/tppost"),
                ChatColor.BLACK + "      Vocal Callouts:\n",
                LazyText.command(" Hello! ", "/v hello"),
                LazyText.command(" Thanks! ", "/v thanks"),
                LazyText.command(" Ok! \n", "/v okay"),
                LazyText.command(" Over here! ", "/v overhere"),
                LazyText.command(" Dis wae! \n", "/v followme"),
                LazyText.command(" Help! ", "/v help"),
                LazyText.command(" Lol! ", "/v haha"),
                LazyText.command(" No ", "/v no"),
                LazyText.command(" More...", "/voice")));
        bookMeta.spigot().addPage(LazyText.buildPage(ChatColor.RED + "     MLG Fortress\n",
                LazyText.url("IRC (chatroom)\n", "http://r.robomwm.com/mememap", "/irc"),
                LazyText.url("Live Map\n", "http://r.robomwm.com/mememap", "/map"),
                LazyText.url("Dumcord\n", "https://discord.gg/3TXnkfa", "/dumcord"),
                LazyText.command("Clan commands\n", "/clan"),
                LazyText.command("Tacos\n", "/taco", "/taco <player>"),
                LazyText.command("Shops\n", "/shop")));
        bookMeta.spigot().addPage(LazyText.buildPage(ChatColor.RED + "     MLG Fortress\n",
                LazyText.command("AutoCraft Airships\n", "/einfo autocraft", "/ac"),
                LazyText.command("Element Bending\n", "/einfo bending"),
                LazyText.command("Slimefun Guide\n", "/sf guide")));
        bookMeta.spigot().addPage(LazyText.buildPage(ChatColor.RED + "     MLG Fortress\n",
                "There's a lot more... I means there's like 150+ plugins...\nSo uh yea pls /apply cuz uh this is a lot to do but um yea??"));
        book.setItemMeta(bookMeta);

        //minigames
        bookMeta = LazyText.getBookMeta();
        bookMeta.spigot().addPage(LazyText.buildPage(
                LazyText.command("⬅Back                        \n","/help","Back to /menu"),
                "\nTo take a break from the custom survival you can take a visit to",
                LazyText.command(" /minigames! ", "/minigames"),
                "Play games like cookie clicker and create parkours!"));
        minigames = LazyText.getBook(bookMeta);

        bookMeta = LazyText.getBookMeta();
        bookMeta.spigot().addPage(LazyText.buildPage(
                LazyText.command("⬅Back                        \n","/help","Back to /menu"),
                "Creative Parkour allows you to express you creativity as minecrafter. Doing /cp create takes you to a personal plot that allows you to build whatever parkour you want! Let your friends help you using /cp invite. You can utilize red sandstone (speed) and lapis blocks (repel) to give special abilities to players playing your map! You can use /cp test to make your map publishable and /cp publish (name) to put out your map to the world."));
        bookMeta.spigot().addPage(LazyText.buildPage(
                "When playing parkours in the Creative Parkour World you can play any map published by any player! Want to spectate a fellow friend playing a parkour with you? Just do /cp spec. You can return to start using the lime green dye and go to a checkpoint using the purple dye."
        ));
        creativeParkour = LazyText.getBook(bookMeta);

        bookMeta = LazyText.getBookMeta();
        bookMeta.spigot().addPage(LazyText.buildPage(
                LazyText.command("⬅Back                        \n","/help","Back to /menu"),
                "You can go to the prison world (/warp prison) to play the classic prison. Doing /pwarp a will get you to your first mine where you can mine items and make money using /sellall. After you gather enough money you can do /rankup. To get a better pickaxe for more efficiency you can look at the crafting recipes at /warp prison."));
        prison = LazyText.getBook(bookMeta);

        bookMeta = LazyText.getBookMeta();
        bookMeta.spigot().addPage(LazyText.buildPage(
                LazyText.command("⬅Back                        \n","/help","Back to /menu"),
                "The Mall otherwise known as the Spawn can be reached using /spawn or /mall. The mall is an alternative way to make money and is very helpful to us lazy folks. Just rename a chest \"shop\" in an anvil, then rent a shop area, place the chest in the shop, place items you want to sell in the chest, do /setprice (amount) and you're sent on making money by doing virtually nothing."));
        bookMeta.spigot().addPage(LazyText.buildPage(
                "When shopping at the mall you can click a sellers' chest to see how many items are in a stock. You can do /buy (amount) to buy items from the seller."));
        prison = LazyText.getBook(bookMeta);
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
            case "prison":
                plugin.getBookUtil().openBook(player, prison);
                break;
            case "minigames":
                plugin.getBookUtil().openBook(player, minigames);
                break;
            case "parkour":
                plugin.getBookUtil().openBook(player, creativeParkour);
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
        BookMeta bookMeta = LazyText.getBookMeta();
        GrandPlayer grandPlayer = plugin.getGrandioseAPI().getGrandPlayerManager().getGrandPlayer(player);

        bookMeta.spigot().addPage(LazyText.buildPage(LazyText.command("⬅ ","/help","Back to /menu"),
                player.getDisplayName() + ChatColor.BLACK + "'s settings\n",
                LazyText.command("View distance: " + player.getViewDistance(), "/view"), "\n",
                LazyText.command("Name color: " + grandPlayer.getNameColor() + grandPlayer.getNameColor().name().toLowerCase(), "/name"), "\n",
                LazyText.command("Music: on", "", "Not implemented yet"), "\n",
                LazyText.command("SneakPickup: " + getOnOff(player.hasMetadata("SNEAKPICKUP")), "/sneakpickup", "/sneakpickup\nPick up items only when sneaking."), "\n"
                ));

        plugin.getBookUtil().openBook(player, LazyText.getBook(bookMeta));
    }

    private void openAbout(Player player)
    {
        BookMeta aboutMeta = LazyText.getBookMeta();
        aboutMeta.spigot().addPage(LazyText.buildPage(
                LazyText.command("⬅Back   ","/help","Back to /menu"),
                ChatColor.DARK_PURPLE + "About+Info\n",
                ChatColor.BLACK + "IP: MLG.ROBOMWM.COM\n",
                LazyText.url("- Website\n", "http://techfortress.robomwm.com/p/mlg-base.html", "Fancy."),
                LazyText.url("- Ur Stats\n", "http://mlg.robomwm.com:28500/player/" + player.getName(), "ayyy"), ChatColor.BLACK +
                "Dis started as Trash Tech back in 2015, except the first guy I opped built a spawn. Then wuz " +
                        "Mini Fortress as Minigames took focus. Then it now b MLG Fortress 4 da memez."));
        aboutMeta.spigot().addPage(LazyText.buildPage(LazyText.command("⬅Back   ","/help","Back to /menu"),
                ChatColor.DARK_PURPLE + "About+Info\n",
                "Credits:\n",
                LazyText.hover("Yoreni, ", "Built and setup prison\nCreated 2 plugins just for this server\nImplemented my requests :o\nBasically an \"actual\" developer (like me)"),
                LazyText.hover("TheDogeGamer, ", "A ded m3m3\nA ton o stuff\nSome guns\nAll dem trading cardz\nA lot of quotes on \nda server list ping motd.\nEssentials messages (blame him)\nBasically all around helpful \nand now all around inactive."),
                LazyText.hover("Lazybannan, ", "Built /warp jail\nBuilt a lot of other spawns\nidk where some of them are."),
                LazyText.hover("Gamewalkerz, ", "Built the /minigames hub\nSetup AreaShop (rentable shops)\nOther stuff idk he's inactive"),
                LazyText.hover("xKittyTheKillerx, ", "Built tutorial\nSetup dumcord"),
                LazyText.hover("MMM10, ", "Who even knows\nRandom Fish\nRandom blocks\nBroke the server a few times by installing random, poorly-made plugins\nThinks his mobcatcher is kewl."),
                LazyText.hover("Etanarvazac, ", "Tweaked plugins\nSetup dumcord\nuh can't recall it's been _a while_"),
                LazyText.hover("MakisMigee123, ", "Augmented element bending\nInstructed players on element bending\nAttempted to resolve bugs/console spam\n(Seriously I usually end up wasting a lot of time fixing that stuff)"),
                " and others but idk right now, you should PR and add urself here or something."));
        aboutMeta.spigot().addPage(LazyText.buildPage(LazyText.command("⬅Back   ","/help","Back to /menu"),
                ChatColor.DARK_PURPLE + "About+Info\n",
                LazyText.url("Github: All dem custom codez\n", "https://github.com/MLG-Fortress/", "Too much wasted time."),
                LazyText.url("Serbur Stats\n", "http://mlg.robomwm.com:28500/server/MLG_Fortress", "Serbur Stalkin"),
                LazyText.url("Ur Stats\n", "http://mlg.robomwm.com:28500/player/" + player.getName(), "ayyy")
                ));
        plugin.getBookUtil().openBook(player, LazyText.getBook(aboutMeta));
    }

    private String getOnOff(boolean bool)
    {
        if (bool)
            return "on";
        return "off";
    }
}
