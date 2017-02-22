package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.Events.JukeboxInteractEvent;
import me.robomwm.MountainDewritoes.Events.MonsterTargetPlayerEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
    World SPAWN;
    MusicManager musicManager = new MusicManager();
    MemeBox memeBox;
    //AtomicBoolean over10Minutes = new AtomicBoolean(true);
    //Pattern hello = Pattern.compile("\\bhello\\b|\\bhi\\b|\\bhey\\b|\\bhai\\b");
    //Pattern bye = Pattern.compile("\\bsee you\\b|\\bc u\\b|\\bbye\\b");

    public AtmosphericManager(MountainDewritoes mountainDewritoes, MemeBox memeBox)
    {
        instance = mountainDewritoes;
        MALL = instance.getServer().getWorld("mall");
        SPAWN = instance.getServer().getWorld("minigames");
        this.memeBox = memeBox;
    }

//    public void morningListener()
//    {
//        playSoundGlobal(musicManager.getMorningSong());
//    }
//
//    public void nightListener()
//    {
//        playSoundGlobal(musicManager.getNightSong());
//    }


    public void stopMusic(Player player)
    {
        player.removeMetadata("MD_LISTENING", instance);
        memeBox.stopSound(player);
    }

    public void stopMusic(Player player, double radius)
    {
        Set<Player> players = new HashSet<>();
        for (Entity entity : player.getNearbyEntities(radius,radius,radius))
        {
            if (entity.getType() == EntityType.PLAYER)
                stopMusic(player);
        }
    }

    /**
     * Plays sound to players, unless they're already listening to something else
     * "Thread-safe"
     * @param song Sound to play
     * @param delay How long to wait in seconds before playing the sound
     */
    public void playSound(MusicThing song, int delay, Collection<? extends Player> players, boolean override)
    {
        Long time = System.currentTimeMillis(); //Used to determine if metadata should be removed
        new BukkitRunnable()
        {
            public void run()
            {
                for (Player player : players)
                {
                    //Skip player if they're dead
                    if (player.hasMetadata("DEAD") || player.isDead())
                        continue;
                    //Skip player if they're already listening to another song (unless we're overriding)
                    if (!override && player.hasMetadata("MD_LISTENING"))
                        continue;
                    player.setMetadata("MD_LISTENING", new FixedMetadataValue(instance, time));
                    memeBox.playSound(player, song);

                    //Schedule removal of metadata
                    new BukkitRunnable()
                    {
                        public void run()
                        {
                            //another event removed metadata earlier (worldchange)
                            if (!player.hasMetadata("MD_LISTENING"))
                                return;

                            //player received new music
                            if (player.getMetadata("MD_LISTENING").get(0).asLong() == time)
                                player.removeMetadata("MD_LISTENING", instance);
                        }
                    }.runTaskLater(instance, song.getLength());
                    //player.playSound(player.getLocation(), song.getSoundName(), SoundCategory.AMBIENT, 3000000f, 1.0f);
                }
            }
        }.runTaskLater(instance, delay * 20L);
    }

    public void playSoundNearPlayer(MusicThing song, Player player, double radius, boolean force)
    {
        Set<Player> players = new HashSet<>();
        for (Entity entity : player.getNearbyEntities(radius,radius,radius))
        {
            if (entity.getType() == EntityType.PLAYER)
                players.add((Player)entity);
        }
        playSound(song, 0, players, force);
    }
    public void playSound(MusicThing song, @Nullable World world, boolean force)
    {
        if (world == null)
            playSound(song, 0, instance.getServer().getOnlinePlayers(), force);
        else
            playSound(song, 0, world.getPlayers(), force);
    }
    public void playSound(MusicThing song, int delay, Player player, boolean force)
    {
        playSound(song, delay, Collections.singletonList(player), force);
    }

    /**Music always stops when player changes worlds*/
    @EventHandler
    void changeWorldResetMetadata(PlayerChangedWorldEvent event)
    {
        stopMusic(event.getPlayer());
    }

    /**Music always stops when player dies*/
    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event)
    {
        stopMusic(event.getEntity());
    }

    /**In case metadata doesn't get removed for w/e reason*/
    @EventHandler
    void onQuitResetMetadataAndStopMusic(PlayerChangedWorldEvent event)
    {
        stopMusic(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    void onMobTarget(MonsterTargetPlayerEvent event)
    {
        if (!instance.isSurvivalWorld(event.getPlayer().getWorld()))
            return;
        NSA nsa = instance.getNSA();
        if (nsa.howManyTargetingPlayer(event.getPlayer()) > 3)
            playSound(musicManager.getFightSong(), 0, event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void playAmbientMusic(PlayerChangedWorldEvent event)
    {
        //memeBox.switchPlayerShow(event.getPlayer(), event.getFrom());
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (world == MALL)
            playSound(musicManager.getMallSong(), MALL, false);
    }

    @EventHandler
    void onPlayerInteractJukebox(JukeboxInteractEvent event)
    {
        Player player = event.getPlayer();
        Material disc = event.getDisc();
        Jukebox jukebox = event.getJukebox();

        //If there's already a disc in here, eject it and stop playing
        if (jukebox.eject())
        {
            //Ignore if we didn't start the sound
            if (!jukebox.hasMetadata("MD_JUKEBOX"))
                return;

            stopMusic(player, 64);

            jukebox.removeMetadata("MD_JUKEBOX", instance);
            return;
        }

        if (!disc.isRecord())
            return;

        //Otherwise, let's play a song, yay
        MusicThing songToPlay = null;


        switch(disc)
        {
            case GOLD_RECORD:
                songToPlay = musicManager.getMallSong();
                break;
            case GREEN_RECORD:
            case RECORD_3:
            case RECORD_4:
            case RECORD_5:
            case RECORD_6:
            case RECORD_7:
            case RECORD_8:
            case RECORD_9:
            case RECORD_10:
            case RECORD_11:
            case RECORD_12:
                break;
        }

        if (songToPlay == null)
            return;

        final MusicThing song = songToPlay;

        jukebox.setMetadata("MD_JUKEBOX", new FixedMetadataValue(instance, songToPlay));
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (song == jukebox.getMetadata("MD_JUKEBOX").get(0).value())
                jukebox.removeMetadata("MD_JUKEBOX", instance);
            }
        }.runTaskLater(instance, song.getLength());
        playSoundNearPlayer(song, player, 64, true);
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

    /**
     * Has it been 10 minutes since we last (globally) played a song?
     * Used for global chat trigger primarily
     */
//    boolean hasItBeen10minutes(boolean reset)
//    {
//        if (over10Minutes.get())
//        {
//            if (reset)
//                new BukkitRunnable()
//                {
//                    public void run()
//                    {
//                        over10Minutes.set(true);
//                    }
//                }.runTaskLater(instance, 20L * 60L * 10L);
//            return true;
//        }
//        else
//            return false;
//    }


}

