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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 3/3/2017.
 *
 * @author RoboMWM
 */
public class AtmosphericMusic implements Listener
{
    private MountainDewritoes instance;
    private AtmosphericManager atmosphericManager;
    private MusicManager musicManager;
    private MusicPackManager musicPackManager;

    public AtmosphericMusic(MountainDewritoes mountainDewritoes, AtmosphericManager atmosphericManager)
    {
        instance = mountainDewritoes;
        musicManager = new MusicManager(instance);
        musicPackManager = new MusicPackManager(instance);
        this.atmosphericManager = atmosphericManager;
        instance.registerListener(this);

        World mall = instance.getServer().getWorld("mall");

        startAmbiance(instance.getServer().getWorld("spawn"), 300L);
        startAmbiance(instance.getServer().getWorld("prison"), 12000L);
        //normalAmbiance(instance.getSurvivalWorlds());

        //Mall
        //playLocalizedSongs(musicPackManager.getSongs("mall"), new Location(mall, 2, 5, 36), 4f);
        playLocalizedSongs(musicPackManager.getSongs("mallfood"), new Location(mall, 50, 5, 102), 3.5f);
        playLocalizedSongs(musicPackManager.getSongs("mall"), new Location(mall, -45, 5, 102), 4.5f);
    }

    private void playLocalizedSongs(List<MusicThing> songs, Location location, float volume)
    {
        MusicThing song = songs.get(ThreadLocalRandom.current().nextInt(songs.size()));
        List<Player> players = location.getWorld().getPlayers();
        for (Player player : players)
                atmosphericManager.playSound(song, 0, player, location, SoundCategory.RECORDS, volume);

        new BukkitRunnable()
        {
            long timeToExpire = System.currentTimeMillis() + song.getLength() * 50;
            @Override
            public void run()
            {
                if (System.currentTimeMillis() > timeToExpire)
                {
                    playLocalizedSongs(songs, location, volume);
                    cancel();
                }
            }
        }.runTaskTimer(instance, song.getLength() + 10, 10L);
    }

    private void startAmbiance(World world, long interval)
    {
        if (world == null)
            return;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                atmosphericManager.playSound(musicPackManager.getSong(world.getName()), world);
            }
        }.runTaskTimer(instance, interval, interval);
    }

    @EventHandler
    private void onPlayerInteractJukebox(JukeboxInteractEvent event)
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

            atmosphericManager.stopMusic(player, 64);

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
                songToPlay = musicManager.getSong("battle");
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

        //Schedule removal of metadata
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (jukebox.hasMetadata("MD_JUKEBOX"))
                    if (song == jukebox.getMetadata("MD_JUKEBOX").get(0).value())
                        jukebox.removeMetadata("MD_JUKEBOX", instance);
            }
        }.runTaskLater(instance, song.getLength());
        atmosphericManager.playMusicNearPlayer(song.setPriority(999), player, 64);
    }

    //Player likely in a battle with mobs
    @EventHandler(priority = EventPriority.HIGH)
    void onMobTarget(MonsterTargetPlayerEvent event)
    {
        if (!instance.isSurvivalWorld(event.getPlayer().getWorld()))
            return;
        if (NSA.howManyTargetingPlayer(event.getPlayer()) > 3)
            atmosphericManager.playSound(musicManager.getSong("battle").setPriority(10), 0, event.getPlayer());
    }

    //Player on killing spree
    @EventHandler
    private void onEntityDeath(EntityDeathEvent event)
    {
        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        if (NSA.getSpreePoints(killer) >= 20)
            atmosphericManager.playSound(musicManager.getSong("spree").setPriority(50), 0, killer);
    }
}

//    Map<Location, Float> locations = new HashMap<>();
//        locations.put(new Location(mall, 2, 13, 88), 2.0f);
//                locations.put(new Location(mall, 2, 13, 116), 2.0f);
//                locations.put(new Location(mall, 2, 4, 298), 10f);
//                playLocalizedSongs(musicPackManager.getSongs("mall"), new HashMap<>(locations));
//
//        locations.clear();
//        locations.put(new Location(mall, 50, 5, 101), 1.5f);
//        locations.put(new Location(mall, 50, 5, 74), 1.5f);
//        locations.put(new Location(mall, 50, 5, 128), 1.5f);
//        playLocalizedSongs(musicPackManager.getSongs("mallfood"), new HashMap<>(locations));
//
//        locations.clear();
//        locations.put(new Location(mall, -45, 5, 101), 1.5f);
//        locations.put(new Location(mall, -45, 5, 74), 1.5f);
//        locations.put(new Location(mall, -45, 5, 128), 1.5f);
//        playLocalizedSongs(musicPackManager.getSongs("malljob"), new HashMap<>(locations));
//
//        locations.clear();

//Minecraft client cannot support more than 4 songs playing in a single channel! (2 channels afaik, stream true, stream false
//If a 5th song comes in, the following occurs:
//- If multiple playSounds are sent in the same (tick?), it will only accept the first (or last?) one.
//- A random sound (seems to be the one furthest down the list in sounds.json) stops playing.

//    private void playLocalizedSongs(List<MusicThing> songs, Map<Location, Float> locations)
//    {
//        MusicThing song = songs.get(ThreadLocalRandom.current().nextInt(songs.size()));
//        List<Player> players = locations.keySet().iterator().next().getWorld().getPlayers();
//        for (Player player : players)
//            for (Location location : locations.keySet())
//                atmosphericManager.playSound(song, 0, player, location, SoundCategory.RECORDS, locations.get(location));
//
//        new BukkitRunnable()
//        {
//            @Override
//            public void run()
//            {
//                playLocalizedSongs(songs, locations);
//            }
//        }.runTaskLater(instance, song.getLength());
//    }
