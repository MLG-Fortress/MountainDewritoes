package me.robomwm.MountainDewritoes.Sounds;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Created by RoboMWM on 10/8/2016.
 * It's not just a manager, it contains it all!
 * No way am I gonna split this into classes (at least not now)
 */
public class AtmosphericManager implements Listener
{
    MountainDewritoes instance;
    World MALL;
    World SPAWN;
    AtomicBoolean over10Minutes = new AtomicBoolean(true);
    Pattern hello = Pattern.compile("\\bhello\\b|\\bhi\\b|\\bhey\\b|\\bhai\\b");
    Pattern bye = Pattern.compile("\\bsee you\\b|\\bc u\\b|\\bbye\\b");
    public AtmosphericManager(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        MALL = instance.getServer().getWorld("mall");
        SPAWN = instance.getServer().getWorld("minigames");
    }

    /**
     * Has it been 10 minutes since we last (globally) played a song?
     * Used for global chat trigger primarily
     */
    boolean hasItBeen10minutes(boolean reset)
    {
        if (over10Minutes.get())
        {
            if (reset)
                new BukkitRunnable()
                {
                    public void run()
                    {
                        over10Minutes.set(true);
                    }
                }.runTaskLater(instance, 20L * 60L * 10L);
            return true;
        }
        else
            return false;
    }

    /**Music always stops when player changes worlds*/
    @EventHandler
    void changeWorldResetMetadata(PlayerChangedWorldEvent event)
    {
        event.getPlayer().removeMetadata("ListeningToMusic", instance);
    }

    /**
     * Plays sound to players, unless they're already listening to something else
     * "Thread-safe"
     * @param sound Sound to play
     * @param lengthInSeconds Estimated length of sound in seconds
     * @param world World to play sound in. Null if global
     * @param delay How long to wait before playing the sound
     */
    void playSound(String sound, int lengthInSeconds, World world, int delay)
    {
        Long length = lengthInSeconds * 20L;
        Long time = System.currentTimeMillis(); //Used to determine if metadata should be removed
        new BukkitRunnable()
        {
            public void run()
            {
                for (Player player : instance.getServer().getOnlinePlayers())
                {
                    if (player.hasMetadata("ListeningToMusic"))
                        continue;
                    if (world != null && player.getWorld() != world)
                        continue;
                    player.setMetadata("ListeningToMusic", new FixedMetadataValue(instance, time));
                    new BukkitRunnable()
                    {
                        public void run()
                        {
							if (!player.hasMetadata("ListeningToMusic")
								return;
							//Can happen if another event removed metadata earlier (worldchange) and player received new music
                            if (player.getMetadata("ListeningToMusic").equals(time))
                                player.removeMetadata("ListeningToMusic", instance);
                        }
                    }.runTaskLater(instance, length);
                    player.playSound(player.getLocation(), sound, 3000000f, 1.0f);
                }
            }
        }.runTaskLater(instance, delay * 20L);
    }

    void playSoundGlobal(String sound, int lengthInSeconds)
    {
        playSound(sound, lengthInSeconds, null, 0);
    }

    /** Play world-specific "ambient" sounds, when player changes worlds, after a 10 second delay */
    @EventHandler(priority = EventPriority.HIGHEST)
    void playAmbientMusic(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String sound = null;
        if (world == MALL)
            sound = "fortress.mall";
        else if (world == SPAWN)
            sound = "fortress.spawn";
        else
            return;
        playSound(sound, 120, world, 10);
    }

    /** Play sounds globally based on certain keywords */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerChatPlaySounds(AsyncPlayerChatEvent event)
    {
        //Don't care about muted/semi-muted chatters
        if (event.getRecipients().size() < instance.getServer().getOnlinePlayers().size())
            return;

        if (!hasItBeen10minutes(false))
            return;

        String message = ChatColor.stripColor(event.getMessage().toLowerCase());

        //No need to block the event to check this
        new BukkitRunnable()
        {
            public void run()
            {
                if (hello.matcher(message).matches())
                    playSoundGlobal("fortress.hello", 41);
                else if (bye.matcher(message).matches())
                    playSoundGlobal("fortress.bye", 35);
                //TODO: etc.
            }
        }.runTaskAsynchronously(instance);
    }
}
