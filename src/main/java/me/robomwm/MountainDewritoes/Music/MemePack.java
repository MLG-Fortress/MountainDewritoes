package me.robomwm.MountainDewritoes.Music;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * Created on 6/5/2017.
 *
 * @author RoboMWM
 */
public class MemePack
{
    public void playSound(Player player, MusicThing song)
    {
        player.playSound(player.getLocation(), song.getSoundName(), SoundCategory.RECORDS, 3000000f, 1.0f);
    }

    public void playSound(Player player, MusicThing song, float radius)
    {
        player.getWorld().playSound(player.getLocation(), song.getSoundName(), SoundCategory.RECORDS, radius, 1.0f);
    }

    public void stopSound(Player player, MusicThing song)
    {
        player.stopSound(song.getSoundName());
    }

    public void stopSound(Player player)
    {
        player.stopSound("", SoundCategory.RECORDS);
    }
}
