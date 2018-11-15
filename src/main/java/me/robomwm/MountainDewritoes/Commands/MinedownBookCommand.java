package me.robomwm.MountainDewritoes.Commands;

import de.themoep.minedown.MineDown;
import me.robomwm.MountainDewritoes.LazyText;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.similarity.FuzzyScore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
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
        return true;
    }

    /**
     * Perform async
     * @return
     */
    public LazyText.Builder getTableOfContents(String label)
    {
        LazyText.Builder builder = new LazyText.Builder();
        //TODO: first append toc file
        int i = 0;
        File[] files = folder.listFiles();
        Arrays.sort(files);
        for (File file : files)
        {
            if (++i % 12 == 0)
                builder.add("\\p");
            String name = StringUtils.capitalize(file.getName().substring(0, file.getName().lastIndexOf(".")).replaceAll("_", " "));
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
}
