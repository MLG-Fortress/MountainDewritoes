package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
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

/**
 * Created by RoboMWM on 5/27/2016.
 *
 * Random tip of the day
 * Resource pack notifier
 * Warning to players using older clients (protocolsupport dependency)
 */
public class JoinMessages implements Listener
{
    MountainDewritoes instance;

    List<String> randomTips = new ArrayList<>();
    Random random = new Random();

    String pack;
    Set<UUID> ignoredUUIDs = new HashSet<>();

    JoinMessages(MountainDewritoes blah)
    {
        blah.getServer().getPluginManager().registerEvents(this, blah);
        instance = blah;

        //Tip of the day
        randomTips.add("Mobs may drop a health canister; use these to add an extra heart.");
        randomTips.add("Long fall boots (iron boots) prevent " + ChatColor.BOLD + ChatColor.AQUA + "ALL fall damage!");
        randomTips.add("We could always use more staff, feel free to /apply");
        randomTips.add("Bored? Talk 2 U_W0T_B0T by mentioning it in chat!");
        randomTips.add("Got any suggestions for the MLG pack? Just state your opinions in chat!");
        randomTips.add("ur message culd b here! Just bcome staff by /apply m8");
        randomTips.add("Play Watch2Win by typing /ad 2 win some neuuuu stooof");
        randomTips.add("Need a crate key? Win one via an /ad or see if there's any at the /mall");
        randomTips.add("Try an AbsorptionShield in the chest at spawn!");

        //Resource pack notifier
        ignoredUUIDs.add(UUID.fromString("a1a23a3f-ab44-45c9-b484-76c99ae8fba8"));
        pack = instance.getConfig().getString("pack");

        //client version
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
                if (player.isOnline())
                {
                    String tip = randomTips.get(random.nextInt(randomTips.size()));
                    instance.timedActionBar(player, 20, ChatColor.GOLD + tip);
                }
            }
        }.runTaskLater(instance, 2400L);
    }

    //resource pack
    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        if (pack == null || pack.isEmpty())
            return;
        new BukkitRunnable()
        {
            public void run()
            {
                if (!event.getPlayer().isOnline())
                    this.cancel();
                else if (!event.getPlayer().isOnGround())
                    return;
                else
                {
                    event.getPlayer().setResourcePack(pack);
                    this.cancel();
                }
            }
        }.runTaskTimer(instance, 40L, 100L);
    }
    @EventHandler
    void statusOfPack(PlayerResourcePackStatusEvent event)
    {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED)
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "irc say samplebot #MLG " + event.getPlayer().getName() + " denied da meme pack.");

            if (ignoredUUIDs.contains(event.getPlayer().getUniqueId()))
                return;

            new BukkitRunnable()
            {
                public void run()
                {
                    if (event.getPlayer().isOnline())
                        event.getPlayer().sendMessage(ChatColor.GOLD + "Ayyy, we noticed u denied our meme resource pack. Please enable it by editing the server in your servers list.");
                }
            }.runTaskLater(instance, 1200L);
        }
    }

    //Use the latest version!
    @EventHandler
    void onUsingOlderClient(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!player.isOnline())
                    return;
                try
                {
                    if (ProtocolSupportAPI.getProtocolVersion(player) != ProtocolVersion.getLatest(ProtocolType.PC))
                    {
                        player.sendMessage(ChatColor.RED + "Warning: " + ChatColor.GOLD + "You are using an outdated version of Minecraft - Some features on this server might not appear to work correctly for you." + ChatColor.YELLOW + "\nFor the best and intended MLG experience, please update to " + ProtocolVersion.getLatest(ProtocolType.PC).getName());
                    }
                }
                catch (Exception e)
                {
                    instance.getLogger().info("Probably need to update ProtocolSupport dependency (or it isn't loaded atm.)");
                    instance.getLogger().warning(e.getMessage());
                }
            }
        }.runTaskLater(instance, 900L); //15 seconds after joining

    }
}
