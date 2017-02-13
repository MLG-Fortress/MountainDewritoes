package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by RoboMWM on 10/8/2016.
 * It's not just a manager, it contains it all!
 * No way am I gonna split this into classes (at least not now)
 */
public class AtmosphericManager implements Listener
{
    MountainDewritoes instance;
    World MALL;
    AtomicBoolean over10Minutes = new AtomicBoolean(true);
    MusicManager musicManager = new MusicManager();
    MemeBox memeBox;
    //Pattern hello = Pattern.compile("\\bhello\\b|\\bhi\\b|\\bhey\\b|\\bhai\\b");
    //Pattern bye = Pattern.compile("\\bsee you\\b|\\bc u\\b|\\bbye\\b");

    public AtmosphericManager(MountainDewritoes mountainDewritoes, MemeBox memeBox)
    {
        instance = mountainDewritoes;
        MALL = instance.getServer().getWorld("mall");
        this.memeBox = memeBox;
    }

    public void morningListener()
    {
        playSoundGlobal(musicManager.getMorningSong());
    }

    public void nightListener()
    {
        playSoundGlobal(musicManager.getNightSong());
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
        event.getPlayer().removeMetadata("LISTENING", instance);
    }

    /**In case metadata doesn't get removed for w/e reason*/
    @EventHandler
    void onQuitResetMetadata(PlayerChangedWorldEvent event)
    {
        event.getPlayer().removeMetadata("LISTENING", instance);
    }

    /**
     * Plays sound to players, unless they're already listening to something else
     * "Thread-safe"
     * @param sound Sound to play
     * @param world World to play sound in. Null if global
     * @param delay How long to wait in seconds before playing the sound
     */
    void playSound(MusicThing sound, World world, int delay)
    {
        Long time = System.currentTimeMillis(); //Used to determine if metadata should be removed
        new BukkitRunnable()
        {
            public void run()
            {
                for (Player player : instance.getServer().getOnlinePlayers())
                {
                    if (player.hasMetadata("LISTENING") || player.hasMetadata("DEAD") || player.isDead())
                        continue;
                    if (world != null && player.getWorld() != world)
                        continue;
                    player.setMetadata("LISTENING", new FixedMetadataValue(instance, time));
                    new BukkitRunnable()
                    {
                        public void run()
                        {
                            if (!player.hasMetadata("LISTENING"))
                                return;
                            //Can happen if another event removed metadata earlier (worldchange) and player received new music
                            if (player.getMetadata("LISTENING").equals(time))
                                player.removeMetadata("LISTENING", instance);
                        }
                    }.runTaskLater(instance, sound.getLength());
                    player.playSound(player.getLocation(), sound.getSoundName(), SoundCategory.AMBIENT, 3000000f, 1.0f);
                }
            }
        }.runTaskLater(instance, delay * 20L);
    }

    void playSoundGlobal(MusicThing sound)
    {
        playSound(sound, null, 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void playAmbientMusic(PlayerChangedWorldEvent event)
    {
        memeBox.switchPlayerShow(event.getPlayer(), event.getFrom());
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (world == MALL)
            memeBox.playSound("mall", musicManager.getMallSong());
    }

    /** Play sounds globally based on certain keywords
     * Totally not even close to ready yet, I might even scrap this idea*/
    //@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
//    void onPlayerChatPlaySounds(AsyncPlayerChatEvent event)
//    {
//        //Don't care about muted/semi-muted chatters
//        if (event.getRecipients().size() < instance.getServer().getOnlinePlayers().size())
//            return;
//
//        if (!hasItBeen10minutes(false))
//            return;
//
//        String message = ChatColor.stripColor(event.getMessage().toLowerCase());
//
//        //No need to block the event to check this
//        new BukkitRunnable()
//        {
//            public void run()
//            {
//                if (hello.matcher(message).matches())
//                    playSoundGlobal("fortress.hello", 41);
//                else if (bye.matcher(message).matches())
//                    playSoundGlobal("fortress.bye", 35);
//                //TODO: etc.
//            }
//        }.runTaskAsynchronously(instance);
//    }


}

