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
        add("jenka1", 120+49, "Jenka 1 by DM Dokuro on Bandcamp.\nhttps://dmdokuro.bandcamp.com/track/jenka-1");
        add("unusedupgradestation", 120+5, "(unused) Upgrade Station Music from Team Fortress 2 (video game)");
        add("wiishopthing", 120+17, "Mii Favorite Things by Skiff on Soundcloud.\nhttps://soundcloud.com/skiff-music/mii-favorite-things");
        add("mall", 60+43, "Dead Bird Studio Reception from A Hat In Time (video game) by Pascal Michael Stiefel.\nhttp://store.steampowered.com/app/253230/");

        category = "spawn";
        add("kahoot", (60*4)+42, "The Kahoot Lobby remixed by xDEFCONx on Soundcloud.\nhttps://soundcloud.com/xdefconx/the-kahoot-lobby-xdefconx-synthesia-remakeremix-link-in-desc");

        category = "minediamonds";
        add("minediamonds", 22, null);
    }

    private void add(String soundName, long durationInSeconds, String description)
    {
        if (!master.containsKey(category))
            master.put(category, new ArrayList<>());
        master.get(category).add(new MusicThing("music." + category + "." + soundName, durationInSeconds, true, description));
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
