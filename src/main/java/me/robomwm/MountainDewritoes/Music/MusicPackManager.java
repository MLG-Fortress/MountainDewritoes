package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 11/26/2016.
 */
class MusicPackManager
{
    private Map<String, List<MusicThing>> master = new HashMap<>();
    String category = "";

    public MusicPackManager(MountainDewritoes instance)
    {
        category = "mall";
        add("banjokazooiepausethemechiptunecover", 60+16);
        add("friendlyshoptheme", 128);
        add("jenka1", 120+49);
        add("memedrops", 120+120+4);
        add("pokemoncenterpianosolol", 180+3);
        add("pokemoncenterthemeorchestrated", 119);
        add("roboplanetpause", 180+41);
        add("shopthemecover", 120+51);
        add("skrillevapor", 120+20);
        add("startrekthemepausecafe", 120+39);
        add("unusedupgradestation", 180);
        add("wiishopchiptune", 120+7);
        add("wiishopthemejazzcover", 120+120+18);
        add("wiishopthing", 120+17);
    }

    private void add(String soundName, long durationInSeconds)
    {
        if (!master.containsKey(category))
            master.put(category, new ArrayList<>());
        master.get(category).add(new MusicThing("music." + category + "." + soundName, durationInSeconds, true));
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
        return new MusicThing(song.getSoundName(), song.getLength(), true);
    }
}
