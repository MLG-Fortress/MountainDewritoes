package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.MountainDewritoes;
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
        config.options().pathSeparator('|');
        ConfigurationSection musicSection = config.getConfigurationSection("Music");
        if (musicSection == null)
        {
            musicSection = config.createSection("Music");
            musicSection.createSection("battle").set("http://localhost/test/adfsfd_lkji.mp3", 20);
            musicSection.createSection("mall").set("http://localhost/test/adfsfd_lkji.mp3", 20);
            instance.saveConfig();
        }

        for (String sectionName : musicSection.getKeys(false))
        {
            ConfigurationSection section = musicSection.getConfigurationSection(sectionName);
            master.put(sectionName, get(section.getValues(false)));
        }
//        battle.add(put("http://k003.kiwi6.com/hotlink/4p667bfkgx/robbierottenvisitsspiralmountain.mp3", 61));
//        battle.add(put("http://k003.kiwi6.com/hotlink/hgomo9zqa1/The_True_Battle_Is_Yet_To_Come_Original_Orchestral_Composition_.mp3", 60+49));
//        battle.add(put("http://k003.kiwi6.com/hotlink/ao38zrqlzw/battle_beep_beep_beep.mp3", 120+41));
//        battle.add(put("http://k003.kiwi6.com/hotlink/3msvwn9mix/battle_we_are_the_strongest.mp3", 120+41));
//        battle.add(put("http://k003.kiwi6.com/hotlink/5bm6q2001i/battle_The_Grand_Show.mp3", 120+34));
//
//        mall.add(put("http://k003.kiwi6.com/hotlink/itnxb4yw2p/splatoon_booyah_base_shopping.mp3", 90));
//        mall.add(put("http://k003.kiwi6.com/hotlink/7dffrye0i1/mall_sanicwave.mp3", 60+60+60+35));
//        mall.add(put("http://k003.kiwi6.com/hotlink/hr3e89idcx/mall_animal_lobby.mp3", 46));
//        mall.add(put("http://k003.kiwi6.com/hotlink/va0jj0xhuo/mall_flea_market_circulation.mp3", 120+7));
//        mall.add(put("http://k003.kiwi6.com/hotlink/b5fc4twixl/mall_lets_shop.mp3", 60));
//        mall.add(put("http://k003.kiwi6.com/hotlink/d8ej3ak8cd/mall_You_reposted_in_the_wrong_flea_market.mp3", 60+46));
    }

    private List<MusicThing> get(Map<String, Object> sectionMap)
    {
        List<MusicThing> wowAList = new ArrayList<>();
        for (String songName : sectionMap.keySet())
            wowAList.add(new MusicThing(songName, (int)(sectionMap.get(songName))));
        return wowAList;
    }

    private MusicThing randomizer(List<MusicThing> hello)
    {
        return hello.get(ThreadLocalRandom.current().nextInt(hello.size()));
    }

    public MusicThing getSong(String category)
    {
        return randomizer(master.get(category));
    }
}
