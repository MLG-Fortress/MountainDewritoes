package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.MountainDewritoes;

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
    String category;

    public MusicPackManager(MountainDewritoes instance)
    {
        category = "mall";
        add("elevatorjam", 120+43);
        add("hhstar", 180+20);
        add("miifavoritethings", 120+17);
        add("nyanjazz", 180+30);
        add("wiibopchannel", 60+60+60+60+32);
        add("teeosupdate", 66);
        add("beautifulmiis", 120+22);
        add("whosthatdeadhomeappliance", 66);
        add("miichannel", 124);
        add("splatoonbooyahbaseshopping", 90);
        add("greenhiillzhop", 120+11);
        category = "mallfood";
        add("smokeoddity", 57);
        add("krabbypower", 60+47);
        add("noodlescantbebeat", 50);
        add("noodlescantbebeat2", 60+43);
        add("portacorobinson", 63);
        add("steamedhamsbutallstar", 120+18);
        add("bigsmokegetshurt", 49);
        add("brokensmokes", 60+31);
        add("frostedflakes", 51);

        category = "spawn";
        add("kahoot", 60*4+41, "The Kahoot Lobby - remixed by xDEFCONx on Soundcloud.\nhttps://soundcloud.com/xdefconx/the-kahoot-lobby-xdefconx-synthesia-remakeremix-link-in-desc");
        add("wiisports", 125, "https://soundcloud.com/videogameremixes/wii-sports-remix");
        add("wirelessplaymenu", 68);

        category = "prison";
        add("oddity", 180+38, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("silverbells", 120+28, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("oceanman", 120+9, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("minediamonds", 60+60+60+60+8);
        add("tragic", 120+55, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("fireflies", 180+4, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
        add("road", 120+29, "https://www.youtube.com/channel/UCPT7fE_u4Q-ioN49Bi2G74Q");
    }

    private void add(String soundName, long seconds)
    {
        add(soundName, seconds, "");
    }
    private void add(String soundName, long durationInSeconds, String description)
    {
        durationInSeconds += 2; //account for lag and etc.
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

    public List<MusicThing> getSongs(String category)
    {
        return master.get(category);
    }
}
