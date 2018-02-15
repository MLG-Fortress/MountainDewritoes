package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolType;
import protocolsupport.api.ProtocolVersion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 5/27/2016.
 *
 * Random tip of the day
 * Resource pack notifier
 * Warning to players using older clients (protocolsupport dependency)
 */
public class JoinMessages implements Listener
{
    private MountainDewritoes instance;

    //List<String> randomTips = new ArrayList<>();
    //Random random = new Random();

    private List<String> randomTitles = new ArrayList<>();
    private List<String> randomSubTitles = new ArrayList<>();

    private String pack;
    private Title.Builder loadingPackTitleBuilder;

    JoinMessages(MountainDewritoes blah)
    {
        blah.getServer().getPluginManager().registerEvents(this, blah);
        instance = blah;

        //Tip of the day
//        randomTips.add("Mobs may drop a health canister; use these to add an extra heart.");
//        randomTips.add("Long fall boots (iron boots) prevent " + ChatColor.BOLD + ChatColor.AQUA + "ALL fall damage!");
//        randomTips.add("We could always use more staff, feel free to /apply");
//        randomTips.add("Bored? Talk 2 U_W0T_B0T by mentioning it in chat!");
//        randomTips.add("Got any suggestions for the MLG pack? Just state your opinions in chat!");
//        randomTips.add("ur message culd b here! Just bcome staff by /apply m8");
//        randomTips.add("Play Watch2Win by typing /ad 2 win some neuuuu stooof");
//        randomTips.add("Need a crate key? Win one via an /ad or see if there's any at the /mall");
//        randomTips.add("Try an AbsorptionShield in the chest at spawn!");

        //Resource pack notifier
        pack = instance.getConfig().getString("pack");
        loadingPackTitleBuilder = new Title.Builder();
        loadingPackTitleBuilder.fadeIn(0);
        loadingPackTitleBuilder.stay(30);
        loadingPackTitleBuilder.fadeOut(0);
        randomTitles.add("Loadin Memez");
        randomTitles.add("Laodin Maymays");
        randomTitles.add("Meme Delivery");
        randomTitles.add("Welcome to Minecraft");
        randomTitles.add("Pls Dont timeout");
        randomTitles.add("Don't 4get 2 breathe");
        randomSubTitles.add("Did u no u can /tip");
        randomSubTitles.add("Did u no u can /apply 4 staff");
        randomSubTitles.add("Did u no theres over 120 plogenz here");
        randomSubTitles.add("Did u no therez moar world typez no man has gone b4");
        randomSubTitles.add("Did u no there r wormholes");
        randomSubTitles.add("Did u no Did u no Did u no Did u no Did u no Did u no Did u no Did u no Did u no Did u no");
        randomSubTitles.add(ChatColor.LIGHT_PURPLE + "* U_WOT_BOT is lonely");
    }

    //Tips
    @EventHandler
    void onPlayerJoinToDeliverATip(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        new BukkitRunnable()
        {
            public void run()
            {
                if (!instance.getServer().getOnlinePlayers().contains(player))
                {
                    //String tip = randomTips.get(random.nextInt(randomTips.size()));
                    //instance.timedActionBar(player, 20, ChatColor.GOLD + tip);
                    player.performCommand("tip join");
                }
            }
        }.runTaskLater(instance, 2400L);
    }

    //resource pack
    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerJoin(PlayerJoinEvent event)
    {
        if (pack == null || pack.isEmpty())
            return;
        event.getPlayer().setMetadata("MD_JOINING", new FixedMetadataValue(instance, true));
        if (event.getPlayer().hasMetadata("MD_ACCEPTED"))
        {
            event.getPlayer().sendMessage("Seems u timed out while attempting 2 load de memepack. Try restarting Minecraft?\nWe'll resend it 2 u da next time u join.");
            event.getPlayer().removeMetadata("MD_ACCEPTED", instance);
        }
        else
        {
            //loadingPackTitleBuilder.title(randomTitles.get(ThreadLocalRandom.current().nextInt(randomTitles.size())));
            //loadingPackTitleBuilder.subtitle(randomSubTitles.get(ThreadLocalRandom.current().nextInt(randomSubTitles.size())));
            //instance.getTitleManager().sendTitle(event.getPlayer(), 0, loadingPackTitleBuilder.build());
            event.getPlayer().setResourcePack(pack);
        }
        //Prompt again if no response (sometimes prompt disappears on teleport and etc.)
        new BukkitRunnable()
        {
            public void run()
            {
                if (!instance.getServer().getOnlinePlayers().contains(event.getPlayer()))
                    this.cancel();
                else if (!event.getPlayer().isOnGround())
                    return;
                else if (event.getPlayer().hasMetadata("MD_ACCEPTED") || event.getPlayer().hasMetadata("MD_DECLINED") || event.getPlayer().hasMetadata("MD_LOADED"))
                    this.cancel();
                else
                {
                    //loadingPackTitleBuilder.title(randomTitles.get(ThreadLocalRandom.current().nextInt(randomTitles.size() - 1)));
                    //loadingPackTitleBuilder.subtitle(randomSubTitles.get(ThreadLocalRandom.current().nextInt(randomSubTitles.size() - 1)));
                    //instance.getTitleManager().sendTitle(event.getPlayer(), 0, loadingPackTitleBuilder.build());
                    instance.getLogger().info("Resending pack to " + event.getPlayer().getName());
                    event.getPlayer().setResourcePack(pack);
                }
            }
        }.runTaskTimer(instance, 100L, 100L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void statusOfPack(PlayerResourcePackStatusEvent event)
    {
        switch(event.getStatus())
        {
            case ACCEPTED:
                event.getPlayer().setMetadata("MD_ACCEPTED", new FixedMetadataValue(instance, true));
                break;
            case DECLINED:
                if (event.getPlayer().hasMetadata("MD_DECLINED"))
                    return;
                event.getPlayer().setMetadata("MD_DECLINED", new FixedMetadataValue(instance, true));
                //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "communicationconnector " + event.getPlayer().getName() + " denied da memepak.");

                new BukkitRunnable()
                {
                    public void run()
                    {
                        if (!instance.getServer().getOnlinePlayers().contains(event.getPlayer()))
                            event.getPlayer().sendMessage(ChatColor.GOLD + "Ayyy, we noticed u denied our memetastic resource pack." + ChatColor.YELLOW + "\nEnable the pack by editing da serbur in ur servers list.");
                    }
                }.runTaskLater(instance, 100L); //5 seconds
                event.getPlayer().removeMetadata("MD_ACCEPTED", instance);
                event.getPlayer().setViewDistance(8);
                break;
            case SUCCESSFULLY_LOADED:
                event.getPlayer().removeMetadata("MD_ACCEPTED", instance);
                event.getPlayer().removeMetadata("MD_JOINING", instance);
                event.getPlayer().setMetadata("MD_LOADED", new FixedMetadataValue(instance, true));
                instance.getTitleManager().removeTitle(event.getPlayer(), 0);
                event.getPlayer().setViewDistance(8);
                break;
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        event.getPlayer().removeMetadata("MD_LOADED", instance);
        event.getPlayer().removeMetadata("MD_DECLINED", instance);
    }

    //Use the latest version!
    @EventHandler
    private void onUsingOlderClient(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        try
        {
            if (ProtocolSupportAPI.getProtocolVersion(player) == ProtocolVersion.getLatest(ProtocolType.PC))
                return;
        }
        catch (Throwable e)
        {
            instance.getLogger().info("Probably need to update ProtocolSupport dependency (or it isn't loaded atm.)");
            instance.getLogger().warning(e.getMessage());
            return;
        }

        //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "communicationconnector " + event.getPlayer().getName() + " is using archaic " + ProtocolSupportAPI.getProtocolVersion(player).getName());

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!instance.getServer().getOnlinePlayers().contains(player))
                {
                    cancel();
                    return;
                }
                player.sendMessage(ChatColor.DARK_RED + "~~~~~~~~---------~~~~~~~~~\n");
                player.sendMessage(ChatColor.RED + "Warning: " + ChatColor.GOLD + "Some stuff might look broken because you're using an outdated version of Minecraft!" + ChatColor.YELLOW + "\nPlease update to " + ProtocolVersion.getLatest(ProtocolType.PC).getName());
                player.sendMessage(ChatColor.DARK_RED + "\n~~~~~~~~---------~~~~~~~~~");
            }
        }.runTaskTimer(instance, 200L, 24000); //10 seconds after joining, and every 20 minutes

    }

    @EventHandler(ignoreCancelled = true)
    private void onChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        if (!NSA.setTempdata(event.getPlayer(), "chatted"))
            return;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                StringBuilder stringBuilder = new StringBuilder();
                if (ProtocolSupportAPI.getProtocolVersion(player) != ProtocolVersion.getLatest(ProtocolType.PC))
                    stringBuilder.append(player.getName() + " is using archaic "
                            + ProtocolSupportAPI.getProtocolVersion(player).getName() + ". ");

                if (player.hasMetadata("MD_DECLINED"))
                    stringBuilder.append(player.getName() + " denied da memepak. ");

                if (stringBuilder.length() > 0)
                {
                    instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(),
                            "communicationconnector " + stringBuilder.toString());
                    instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(),
                            "broadcast " + stringBuilder.toString());
                }

            }
        }.runTask(instance);
    }
}
