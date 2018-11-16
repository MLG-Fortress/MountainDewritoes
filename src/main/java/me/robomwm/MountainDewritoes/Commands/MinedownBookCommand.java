package me.robomwm.MountainDewritoes.Commands;

import com.robomwm.grandioseapi.player.GrandPlayer;
import de.themoep.minedown.MineDown;
import me.robomwm.MountainDewritoes.LazyText;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.text.similarity.FuzzyScore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created on 11/11/2018.
 *
 * @author RoboMWM
 */
public class MinedownBookCommand implements CommandExecutor
{
    private File folder;
    private MountainDewritoes plugin;

    public MinedownBookCommand(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        folder = new File(plugin.getDataFolder() + File.separator + "book" + File.separator);
        folder.mkdirs();
        plugin.getCommand("start").setExecutor(this);
        plugin.getCommand("info").setExecutor(this);
        plugin.getCommand("settings").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        if (sender instanceof Player)
            player = (Player)sender;

        switch(cmd.getName())
        {
            case "settings":
                openSettings(player);
                return true;
            case "info":
                openAbout(player);
                return true;
            case "start":
            default:
                switch (args.length)
                {
                    case 0:
                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                LazyText.Builder builder = getTableOfContents(label);
                                //who has time to taskchain
                                new BukkitRunnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if (sender instanceof Player)
                                            plugin.openBook((Player)sender, builder.toBook());
                                        else
                                            sender.sendMessage(builder.toComponentArray());
                                    }
                                }.runTask(plugin);
                            }
                        }.runTaskAsynchronously(plugin);
                        break;
                    default:
                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                LazyText.Builder builder = getChapter(label, String.join("_", args));
                                //who has time to taskchain
                                new BukkitRunnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if (sender instanceof Player)
                                            plugin.openBook((Player)sender, builder.toBook());
                                        else
                                            sender.sendMessage(builder.toComponentArray());
                                    }
                                }.runTask(plugin);
                            }
                        }.runTaskAsynchronously(plugin);
                }

        }

        return true;
    }

    /**
     * Perform async
     * @return
     */
    public LazyText.Builder getTableOfContents(String label)
    {
        LazyText.Builder builder = new LazyText.Builder();
        builder.add("IP: MLG.ROBOMWM.COM\n").color(ChatColor.DARK_PURPLE)
                .add("? ").color(ChatColor.DARK_AQUA).cmd("/tip", false)
                .hover(new LazyText.Builder()
                        .add("/tip\n")
                        .add("Dark aqua is clickable\n").color(ChatColor.DARK_AQUA)
                        .add("FYI, you can open this /book from the HotMenu (Press F).\n")
                        .color(ChatColor.RESET).toComponentArray())
                .add(" ☺ ").cmd("/emoticons")
                .add(" ✈ ").cmd("/warps")
                .add(" ✫ ").cmd("/changelog", "View server changes")
                .add(" ✉ ").cmd("/mail", true)
                .add(" ⚙ ").cmd("/settings", true)
                .add(" ℹ ").cmd("/info", "Info+Stats")
                .add(" ☀ ").url("http://r.robomwm.com/mlgideas", "Submit or vote on ideas for MLG Fortress!")
                .add(" # \n").url("http://r.robomwm.com/mememap", "Open the " + ChatColor.AQUA +
                "map\n" + ChatColor.RESET + "and " + ChatColor.AQUA + "IRC" + ChatColor.RESET + " chatroom.")
                .add("  /taco\n").cmd("/taco")
                .add("  /clan\n").cmd("/clan")
                .add("  /tppost \n\n").cmd("/tppost")
                .add("      Voicelines:\n")
                .add(" Hello! ").cmd("/v hello")
                .add(" Thanks! ").cmd("/v thanks")
                .add(" Ok \n")
                .add(" Over here! ").cmd("/v overhere")
                .add(" Dis wae! \n").cmd("/v followme")
                .add(" Help! ").cmd("/v help")
                .add(" Lol! ").cmd("/v haha")
                .add(" No ").cmd("/v no")
                .add(" More...\n\n").cmd("/voice");

        int i = 0;
        File[] files = folder.listFiles();
        Arrays.sort(files);
        builder.add("Jump to page ").color(ChatColor.RESET);
        int pages = (files.length / 12) + 1;
        for (int j = 0; j < pages; j++)
        {
            builder.add(String.valueOf(j + 2)).page(j + 2);
            if (j < pages - 1)
                builder.add(", ");
        }
        for (File file : files)
        {
            if (i++ % 12 == 0)
                builder.add("\\p");
            String name = file.getName().substring(0, file.getName().lastIndexOf(".")).replaceAll("_", " ");
            builder.add(name).cmd("/" + label + " " + name, true).color(ChatColor.DARK_AQUA);
            builder.add("\n");
        }
        return builder;
    }

    public LazyText.Builder getChapter(String label, String name)
    {
        LazyText.Builder builder = new LazyText.Builder();
        builder.add("⬅Back                  \n").color(ChatColor.DARK_AQUA).cmd("/" + label, true);

        File file = new File(folder.getPath() + File.separator + name + ".txt");

        //best match if misspelled
        if (!file.exists())
        {
            int i = 0;
            for (File file1 : folder.listFiles())
            {
                String name1 = file1.getName().substring(0, file1.getName().lastIndexOf(".")).replaceAll("_", " ");
                int j = new FuzzyScore(Locale.ENGLISH).fuzzyScore(name1, name);
                if (j > i)
                {
                    file = file1;
                    i = j;
                }
            }
            if (i == 0)
                return getTableOfContents(label);
        }


        try
        {
            StringBuilder stringBuilder = new StringBuilder();
            for (String line : Files.readAllLines(file.toPath(), Charset.forName("UTF-8")))
            {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            builder.add(new MineDown(stringBuilder.toString()).toComponent());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return builder;
    }

    private void openAbout(Player player)
    {
        BookMeta aboutMeta = LazyText.getBookMeta();
        aboutMeta.spigot().addPage(LazyText.buildPage(
                LazyText.command("⬅Back   ","/help","Back to /book"),
                org.bukkit.ChatColor.DARK_PURPLE + "About+Info\n",
                org.bukkit.ChatColor.BLACK + "IP: MLG.ROBOMWM.COM\n",
                LazyText.url("- Website\n", "http://techfortress.robomwm.com/p/mlg-base.html", "Fancy."),
                LazyText.url("- Ur Stats\n", "http://mlg.robomwm.com:28500/player/" + player.getName(), "ayyy"), org.bukkit.ChatColor.BLACK +
                        "Dis started as Trash Tech back in 2015, except the first guy I opped built a spawn. Then wuz " +
                        "Mini Fortress as Minigames took focus. Then it now b MLG Fortress 4 da memez."));
        aboutMeta.spigot().addPage(LazyText.buildPage(LazyText.command("⬅Back   ","/help","Back to /menu"),
                org.bukkit.ChatColor.DARK_PURPLE + "About+Info\n",
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
                org.bukkit.ChatColor.DARK_PURPLE + "About+Info\n",
                LazyText.url("Github: All dem custom codez\n", "https://github.com/MLG-Fortress/", "Too much wasted time."),
                LazyText.url("Serbur Stats\n", "http://mlg.robomwm.com:28500/server/MLG_Fortress", "Serbur Stalkin"),
                LazyText.url("Ur Stats\n", "http://mlg.robomwm.com:28500/player/" + player.getName(), "ayyy")
        ));
        plugin.openBook(player, LazyText.getBook(aboutMeta));
    }

    private void openSettings(Player player)
    {
        BookMeta bookMeta = LazyText.getBookMeta();
        GrandPlayer grandPlayer = plugin.getGrandioseAPI().getGrandPlayerManager().getGrandPlayer(player);

        bookMeta.spigot().addPage(LazyText.buildPage(LazyText.command("⬅ ","/help","Back to /menu"),
                player.getDisplayName() + org.bukkit.ChatColor.BLACK + "'s settings\n",
                LazyText.command("View distance: " + player.getViewDistance(), "/view"), "\n",
                LazyText.command("Name color: " + grandPlayer.getNameColor() + grandPlayer.getNameColor().name().toLowerCase(), "/name"), "\n",
                LazyText.command("Music: on", "", "Not implemented yet"), "\n",
                LazyText.command("SneakPickup: " + getOnOff(player.hasMetadata("SNEAKPICKUP")), "/sneakpickup", "/sneakpickup\nPick up items only when sneaking."), "\n"
        ));

        plugin.openBook(player, LazyText.getBook(bookMeta));
    }

    private String getOnOff(boolean bool)
    {
        if (bool)
            return "on";
        return "off";
    }
}
