package me.robomwm.MountainDewritoes.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/7/2017.
 *
 * Was going to be "smart" and only deliver unseen tips, but uh... after it not working once+nobody asking for /tips I decided not to waste time on that.
 * @author RoboMWM
 */
public class TipCommand implements CommandExecutor
{
    private JavaPlugin instance;
    //YamlConfiguration storage;
    private List<String> randomTips = new ArrayList<>();
    private static List<ChatColor> color = new ArrayList<>(); //only written once, so _should_ be thread safe
    //File storageFile;

    public TipCommand(JavaPlugin plugin)
    {
        instance = plugin;

        color.add(ChatColor.WHITE);
        color.add(ChatColor.GOLD);
        color.add(ChatColor.GREEN);
        color.add(ChatColor.BLUE);
        color.add(ChatColor.AQUA);
        color.add(ChatColor.YELLOW);
        color.add(ChatColor.LIGHT_PURPLE);
//        storageFile = new File(plugin.getDataFolder(), "storage.data");
//        if (!storageFile.exists())
//        {
//            try
//            {
//                storageFile.createNewFile();
//                storage = YamlConfiguration.loadConfiguration(storageFile);
//            }
//            catch (IOException e)
//            {
//                plugin.getLogger().severe("Could not create storage.data.");
//                storage = new YamlConfiguration();
//                e.printStackTrace();
//            }
//        }
//        else
//            storage = YamlConfiguration.loadConfiguration(storageFile);


        randomTips.add("Mobs may drop a health canister; use these 2 hold moar sweg.");
        randomTips.add("Think u got wut it taeks 2 maek gud meemees? Den /apply");
        randomTips.add("Bored? Talk 2 U_W0T_B0T by mentioning it in chat!");
        randomTips.add("Got any suggestions for the MLG pack? Just state your opinions in chat!");
        randomTips.add("Play Watch2Win by typing /ad 2 win some neuuuu stooof");
        randomTips.add("Lern 2 bend da meincraft world wit /help bending");
        randomTips.add("Don't keel dee endermins");
        randomTips.add("Find loot quick, mobs r hard");
        randomTips.add("Git sum cool stoof wit slamphun!! Lern how in da " + ChatColor.GOLD + "/sf guide");
        add("Build and fly a Minecraft-sized airship! Look for AutoCraft in /help to find out how to build one!");
        add("Try some /voice commands!");
        add("Hmm, I've been finding these random portal-like things... maybe you should find one and check it out?");
        add("Nether portals aren't just portals to the nether...");
        add("If ur in a clan and u claimed some capture points, u can /tppoint to 'em!");
        add("Don't 4get u need to tp 2 /minigames if u want 2 play sum!");
        add("Press F (swap items button) to open the /menu. (Hold sneak if you wish to swap items.)");
        add("Can't play but want to ensure nobody's raiding your claims? Check out da LIVE /map");
        add("Leveling up increases ur swag, lungs, increases chance of saving items on death, and even grants u some kewl rewards! Go get those experience orbs!");
        add("/emoticons also work in signs too!");
    }

    public boolean add(String tip)
    {
        return randomTips.add(ChatColor.translateAlternateColorCodes('&', tip));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player)sender; //nah I don't need to check

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //boolean store = true;
                String tip = getTip(player, randomTips);

                player.sendMessage(formatTip(tip));

//                if (store)
//                {
//                    new BukkitRunnable()
//                    {
//                        @Override
//                        public void run()
//                        {
//                            List<String> seenTips = new ArrayList<>();
//                            if (storage.getStringList(player.getUniqueId().toString()) != null)
//                                seenTips = storage.getStringList(player.getUniqueId().toString());
//                            seenTips.add(tip);
//                            storage.set(player.getUniqueId().toString(), seenTips);
//                            saveStorage();
//                        }
//                    }.runTaskLater(plugin, 0L);
//                }
            }
        }.runTaskAsynchronously(instance);

        return true;
    }

    private String formatTip(String tip)
    {
        return getRandomColor() + "" + ChatColor.BOLD + "/TIP: " + getRandomColor() + tip; //┃\u2503
    }

    private String getTip(@Nullable Player player, List<String> tips)
    {
//        List<String> shuffledTips = new ArrayList<>(tips);
//        Collections.shuffle(shuffledTips);
//        List<String> seenTips = new ArrayList<>();
//        //if (storage.getStringList(player.getUniqueId().toString()) != null)
//            //seenTips = storage.getStringList(player.getUniqueId().toString());
//
//        for (String tip : shuffledTips)
//        {
//            //if (!seenTips.contains(tip))
//                return tip;
//        }

        return tips.get(ThreadLocalRandom.current().nextInt(tips.size()));
    }

    /**
     * Should be thread safe...
     * @return
     */
    public static ChatColor getRandomColor()
    {
        return color.get(ThreadLocalRandom.current().nextInt(color.size()));
    }

    public String getTip()
    {
        return randomTips.get(ThreadLocalRandom.current().nextInt(randomTips.size()));
    }

//    private void saveStorage()
//    {
//        if (storage != null)
//        {
//            try
//            {
//                storage.save(storageFile);
//            }
//            catch (IOException e) //really
//            {
//                e.printStackTrace();
//            }
//        }
//    }
}
