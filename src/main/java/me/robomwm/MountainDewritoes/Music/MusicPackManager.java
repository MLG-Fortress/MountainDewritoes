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
        add("mall", 60*4+6, "Dead Bird Studio Reception - A Hat In Time by Pascal Michael Stiefel.\nhttp://store.steampowered.com/app/253230/");

        category = "spawn";
        add("kahoot", 60*4+42, "The Kahoot Lobby - remixed by xDEFCONx on Soundcloud.\nhttps://soundcloud.com/xdefconx/the-kahoot-lobby-xdefconx-synthesia-remakeremix-link-in-desc");

        category = "prison";
        add("oddity", 60*4+5, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("silverbells", 120+28, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("oceanman", 120+9, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("september", 180+41, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("minediamonds", 180+47, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("tragic", 120+55, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("fireflies", 180+4, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("road", 120+29, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("house", 180+10, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
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
