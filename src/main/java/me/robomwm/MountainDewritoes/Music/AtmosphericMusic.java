package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.Events.MonsterTargetPlayerEvent;
import me.robomwm.MountainDewritoes.Events.PlayerLoadedWorldEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 3/3/2017.
 *
 * @author RoboMWM
 */
public class AtmosphericMusic implements Listener
{
    private MountainDewritoes instance;
    private AtmosphericManager atmosphericManager;
    private MusicPackManager musicPackManager;
    private Map<Player, BukkitTask> introTasks = new HashMap<>();
    private World MALL;
    private Location MALL_INTRO_LOCATION;

    @EventHandler(ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (introTasks.containsKey(player))
            introTasks.remove(player).cancel();
    }

    public AtmosphericMusic(MountainDewritoes mountainDewritoes, AtmosphericManager atmosphericManager)
    {
        instance = mountainDewritoes;
        musicPackManager = new MusicPackManager(instance);
        this.atmosphericManager = atmosphericManager;
        instance.registerListener(this);

        World mall = instance.getServer().getWorld("mall");
        this.MALL = mall;
        this.MALL_INTRO_LOCATION = new Location(mall, 2, 5, 36);

        //startAmbiance(instance.getServer().getWorld("spawn"), 300L);
        //startAmbiance(instance.getServer().getWorld("prison"), 12000L);
        //normalAmbiance(instance.getSurvivalWorlds());

        //Mall
        //playLocalizedSongs(musicPackManager.getSongs("mall"), new Location(mall, 2, 5, 36), 4f);
        //playLocalizedSongs(musicPackManager.getSongs("mallfood"), new Location(mall, 50, 5, 102), 3.2f);
        //playLocalizedSongs(musicPackManager.getSongs("mall"), new Location(mall, -45, 5, 102), 5f);
        playLocalizedSongs(musicPackManager.getSongs("mall"), new Location(mall, 2, 12, 101), 8f, -1);
    }

    private void playLocalizedSongs(@Nonnull List<MusicThing> songs, Location location, float volume, int index)
    {
        if (index < 0 || index >= songs.size())
        {
            Collections.shuffle(songs);
            index = 0;
        }

        MusicThing song = songs.get(index++);
        if (location == null || location.getWorld() == null)
            return;
        List<Player> players = location.getWorld().getPlayers();
        for (Player player : players)
                atmosphericManager.playSound(song, 0, player, location, SoundCategory.RECORDS, volume);

        final int finalIndex = index;
        new BukkitRunnable()
        {
            long timeToExpire = System.currentTimeMillis() + song.getLength() * 50;
            @Override
            public void run()
            {
                if (System.currentTimeMillis() > timeToExpire)
                {
                    playLocalizedSongs(songs, location, volume, finalIndex);
                    cancel();
                }
            }
        }.runTaskTimer(instance, song.getLength() + 10, 10L);
    }

    private void playLocalizedSongs(@Nonnull List<MusicThing> songs, Location location, float volume, Player player, int index)
    {
        if (index < 0 || index >= songs.size())
        {
            Collections.shuffle(songs);
            index = 0;
        }
        MusicThing song = songs.get(index++);
        atmosphericManager.playSound(song, 0, player, location, SoundCategory.RECORDS, volume);

        final int finalIndex = index;
        introTasks.put(player, new BukkitRunnable()
        {
            final World world = player.getWorld();
            long timeToExpire = System.currentTimeMillis() + song.getLength() * 50;
            @Override
            public void run()
            {
                if (player.getWorld() != world)
                {
                    cancel();
                    return;
                }
                if (System.currentTimeMillis() > timeToExpire)
                {
                    playLocalizedSongs(songs, location, volume, player, finalIndex);
                    cancel();
                }
            }
        }.runTaskTimer(instance, song.getLength() + 10, 10L));
    }

    @EventHandler
    private void playLocalizedSongsPlayer(PlayerLoadedWorldEvent event)
    {
        Player player = event.getPlayer();
        World world = event.getPlayer().getWorld();

        if (introTasks.containsKey(player))
            introTasks.remove(player).cancel();

        switch(world.getName())
        {
            case "spawn":
                playLocalizedSongs(musicPackManager.getSongs("arcade"), new Location(world, -453, 9, -123), 4.5f, player, -1);
        }
    }

    @EventHandler
    private void onEnterWorldIntro(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        if (introTasks.containsKey(player))
            introTasks.remove(player).cancel();
        if (event.getPlayer().getWorld() == MALL)
            introTasks.put(player,
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            player.playSound(MALL_INTRO_LOCATION, "music.mall.intro", SoundCategory.RECORDS, 4f, 1f);
                            introTasks.remove(player);
                        }
                    }.runTaskLater(instance, 100L));

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

//    @EventHandler
//    private void onPlayerInteractJukebox(JukeboxInteractEvent event)
//    {
//        Player player = event.getPlayer();
//        Material disc = event.getDisc();
//        Jukebox jukebox = event.getJukebox();
//
//        //If there's already a disc in here, eject it and stop playing
//        if (jukebox.eject())
//        {
//            //Ignore if we didn't start the sound
//            if (!jukebox.hasMetadata("MD_JUKEBOX"))
//                return;
//
//            atmosphericManager.stopMusic(player, 64);
//
//            jukebox.removeMetadata("MD_JUKEBOX", instance);
//            return;
//        }
//
//        if (!disc.isRecord())
//            return;
//
//        //Otherwise, let's play a song, yay
//        MusicThing songToPlay = null;
//
//        switch(disc)
//        {
//            case GOLD_RECORD:
//                songToPlay = musicManager.getSong("battle");
//                break;
//            case GREEN_RECORD:
//            case RECORD_3:
//            case RECORD_4:
//            case RECORD_5:
//            case RECORD_6:
//            case RECORD_7:
//            case RECORD_8:
//            case RECORD_9:
//            case RECORD_10:
//            case RECORD_11:
//            case RECORD_12:
//                break;
//        }
//
//        if (songToPlay == null)
//            return;
//
//        final MusicThing song = songToPlay;
//
//        jukebox.setMetadata("MD_JUKEBOX", new FixedMetadataValue(instance, songToPlay));
//
//        //Schedule removal of metadata
//        new BukkitRunnable()
//        {
//            @Override
//            public void run()
//            {
//                if (jukebox.hasMetadata("MD_JUKEBOX"))
//                    if (song == jukebox.getMetadata("MD_JUKEBOX").get(0).value())
//                        jukebox.removeMetadata("MD_JUKEBOX", instance);
//            }
//        }.runTaskLater(instance, song.getLength());
//        atmosphericManager.playMusicNearPlayer(song.setPriority(999), player, 64);
//    }

    //Player likely in a battle with mobs
    @EventHandler(priority = EventPriority.HIGH)
    void onMobTarget(MonsterTargetPlayerEvent event)
    {
        if (!instance.isSurvivalWorld(event.getPlayer().getWorld()))
            return;
        //if (NSA.howManyTargetingPlayer(event.getPlayer()) > 3)

    }

    //Player on killing spree
    @EventHandler
    private void onEntityDeath(EntityDeathEvent event)
    {
        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        //if (NSA.getSpreePoints(killer) >= 20)
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
