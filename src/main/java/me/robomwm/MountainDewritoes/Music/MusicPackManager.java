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
        add("animallobbyextended", 60+52);
        add("jenka1", 120+49);
        add("pokemoncenterpianosolol", 180+3);
        add("shopthemecover", 120+51);
        add("skrillevapor", 120+20);
        add("unusedupgradestation", 120+5);
        add("wiishopthing", 120+17);
        add("yeoldetemmieshop", 120+10);

        category = "spawn";
        add("kahootlobbymusic", 120+6);
        add("dragonbornjeremysole", 120);
        add("mainmenu1", 60+16);
        add("overworld", 180+2);
        add("safety", 120+24);
        add("thetruebattleisyettocome", 60+58);
        add("wirelessplaymenumariokart8deluxe", 60+8);

        category = "minediamonds";
        add("minediamonds", 22);
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
        return new MusicThing(randomizer(master.get(category)));
    }
}
