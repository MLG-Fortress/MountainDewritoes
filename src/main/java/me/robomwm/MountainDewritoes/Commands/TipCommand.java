package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/7/2017.
 *
 * I could do some sort of hashmap-id-pairing-efficiency but y' no
 * @author RoboMWM
 */
public class TipCommand implements CommandExecutor
{
    JavaPlugin instance;
    YamlConfiguration storage;
    List<String> randomTips = new ArrayList<>();
    List<String> betaTips = new ArrayList<>();
    File storageFile;

    public TipCommand(JavaPlugin plugin)
    {
        instance = plugin;
        storageFile = new File(plugin.getDataFolder(), "storage.data");
        if (!storageFile.exists())
        {
            try
            {
                storageFile.createNewFile();
                storage = YamlConfiguration.loadConfiguration(storageFile);
            }
            catch (IOException e)
            {
                plugin.getLogger().severe("Could not create storage.data.");
                storage = new YamlConfiguration();
                e.printStackTrace();
            }
        }
        else
            storage = YamlConfiguration.loadConfiguration(storageFile);


        randomTips.add("Mobs may drop a health canister; use these to add an extra heart.");
        randomTips.add("Long fall boots (iron boots) prevent " + ChatColor.BOLD + ChatColor.AQUA + "ALL fall damage!");
        randomTips.add("We could always use more staff, feel free to /apply");
        randomTips.add("Bored? Talk 2 U_W0T_B0T by mentioning it in chat!");
        randomTips.add("Got any suggestions for the MLG pack? Just state your opinions in chat!");
        randomTips.add("ur message culd b here! Just bcome staff by /apply m8");
        randomTips.add("Play Watch2Win by typing /ad 2 win some neuuuu stooof");
        randomTips.add("Need a crate key? Win one via an /ad or see if there's any at the /mall");
        randomTips.add("Lern 2 bend da meincraft world wit /help bending");
        randomTips.add("Don't keel dee endermins");
        randomTips.add("Find loot quick mobs r hard");
        randomTips.add("Git sum cool stoof wit slamphun!! Lern how in da " + ChatColor.GOLD + "/sf guide");

        betaTips.add("Try an AbsorptionShield in the chest at spawn!");
        betaTips.add("Wallride like loo-see-oh frum ogrewatch. Hold a feather, jump and hold sneak near a wall");
        betaTips.add("Test the portal gun - just craft a wood shovel");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player)sender; //nah I don't need to check

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                boolean store = true;
                String tip;

                if (label.toLowerCase().contains("beta") || (args.length > 0 && args[0].toLowerCase().contains("beta")))
                {
                    tip = getTip(player, betaTips);
                    store = false;
                }
                else if (args.length > 0 && args[0].toLowerCase().equals("join"))
                {
                    tip = getTip(player, randomTips);
                    store = false;
                }
                else
                    tip = getTip(player, randomTips);

                player.sendMessage(tip);

                if (store)
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            List<String> seenTips = new ArrayList<>();
                            if (storage.getStringList(player.getUniqueId().toString()) != null)
                                seenTips = storage.getStringList(player.getUniqueId().toString());
                            seenTips.add(tip);
                            storage.set(player.getUniqueId().toString(), seenTips);
                            saveStorage();
                        }
                    }.runTaskLater(instance, 0L);
                }
            }
        }.runTaskAsynchronously(instance);

        return true;
    }

    //TODO: word wrapping
    private String getTip(@Nonnull Player player, List<String> tips)
    {
        List<String> shuffledTips = new ArrayList<>(tips);
        Collections.shuffle(shuffledTips);
        List<String> seenTips = new ArrayList<>();
        if (storage.getStringList(player.getUniqueId().toString()) != null)
            seenTips = storage.getStringList(player.getUniqueId().toString());

        for (String tip : shuffledTips)
        {
            if (!seenTips.contains(tip))
                return getRandomColor() + "" + ChatColor.BOLD + "┃/TIP┃ " + getRandomColor() + tip;
        }

        return getRandomColor() + "" + ChatColor.BOLD + "{TIP} " + getRandomColor() + shuffledTips.get(0);
    }

    private ChatColor getRandomColor()
    {
        //Not the most CPU efficient but idc
        List<ChatColor> color = new ArrayList<>();
        color.add(ChatColor.WHITE);
        color.add(ChatColor.GOLD);
        color.add(ChatColor.GREEN);
        color.add(ChatColor.BLUE);
        color.add(ChatColor.AQUA);
        color.add(ChatColor.YELLOW);
        color.add(ChatColor.DARK_PURPLE);
        color.add(ChatColor.LIGHT_PURPLE);
        color.add(ChatColor.DARK_AQUA);
        color.add(ChatColor.DARK_BLUE);
        color.add(ChatColor.DARK_GREEN);
        return color.get(ThreadLocalRandom.current().nextInt(color.size()));
    }

    private void saveStorage()
    {
        if (storage != null)
        {
            try
            {
                storage.save(storageFile);
            }
            catch (IOException e) //really
            {
                e.printStackTrace();
            }
        }
    }
}
