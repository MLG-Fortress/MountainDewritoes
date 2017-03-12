package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 11/26/2016.
 */
class MusicManager
{
    private Map<String, List<MusicThing>> master = new HashMap<>();

    public MusicManager(MountainDewritoes instance)
    {
        FileConfiguration config = instance.getConfig();

        ConfigurationSection musicSection = config.getConfigurationSection("Music");
        if (musicSection == null)
        {
            musicSection = config.createSection("Music");
            musicSection.createSection("battle").set("http://localhost/test/adfsfd_lkji.mp3", 20);
            musicSection.createSection("mall").set("http://localhost/test/adfsfd_lkji.mp3", 20);
            musicSection.createSection("spree").set("http://localhost/test/adfsfd_lkji.mp3", 20);
            instance.getLogger().info("saved blank config");
        }

        for (String sectionName : musicSection.getKeys(false))
        {
            ConfigurationSection section = musicSection.getConfigurationSection(sectionName);
            master.put(sectionName, get(section.getValues(false)));
        }
    }

    private List<MusicThing> get(Map<String, Object> sectionMap)
    {
        List<MusicThing> wowAList = new ArrayList<>();
        for (String songName : sectionMap.keySet())
            wowAList.add(new MusicThing(songName, (int)sectionMap.get(songName)));

        return wowAList;
    }

    private MusicThing randomizer(List<MusicThing> hello)
    {
        return hello.get(ThreadLocalRandom.current().nextInt(hello.size()));
    }

    public MusicThing getSong(String category)
    {
        MusicThing song = randomizer(master.get(category));
        return new MusicThing(song.getURL(), song.getLength());
    }
}
