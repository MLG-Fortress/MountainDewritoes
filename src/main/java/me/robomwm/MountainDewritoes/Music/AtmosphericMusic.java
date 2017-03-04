package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.Events.JukeboxInteractEvent;
import me.robomwm.MountainDewritoes.Events.MonsterTargetPlayerEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.NSA;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 3/3/2017.
 *
 * @author RoboMWM
 */
public class AtmosphericMusic implements Listener
{
    MountainDewritoes instance;
    AtmosphericManager atmosphericManager;
    MusicManager musicManager = new MusicManager(instance);

    public AtmosphericMusic(MountainDewritoes mountainDewritoes, AtmosphericManager atmosphericManager)
    {
        instance = mountainDewritoes;
        this.atmosphericManager = atmosphericManager;
        instance.registerListener(this);

        World MALL = instance.getServer().getWorld("mall");
        if (MALL == null)
            return;

        //Mall Ambience
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                atmosphericManager.playSound(musicManager.getSong("battle"), MALL, false); //TODO: testing, pls change
            }
        }.runTaskTimer(instance, 200L, 600L);
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
        atmosphericManager.playSoundNearPlayer(song, player, 64, true, true);
    }

    //Player likely in a battle with mobs
    @EventHandler(priority = EventPriority.HIGH)
    void onMobTarget(MonsterTargetPlayerEvent event)
    {
        if (!instance.isSurvivalWorld(event.getPlayer().getWorld()))
            return;
        NSA nsa = instance.getNSA();
        if (nsa.howManyTargetingPlayer(event.getPlayer()) > 3)
            atmosphericManager.playSound(musicManager.getSong("battle").setPriority(10), 0, event.getPlayer(), false);
    }

    //Player on killing spree
//    @EventHandler
//    void onEntityDeath(EntityDeathEvent event)
//    {
//        Player killer = event.getEntity().getKiller();
//        if (killer == null)
//            return;
//
//        if (NSA.getSpreePoints(killer) >= 10)
//            atmosphericManager.playSound(musicManager.get);
//
//    }
}
