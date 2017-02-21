package me.robomwm.MountainDewritoes.Music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by RoboMWM on 11/26/2016.
 */
class MusicManager
{
    private Map<String, MusicThing> demSongz = new HashMap<>();
    private List<MusicThing> battle = new ArrayList<>();
    private List<MusicThing> boss = new ArrayList<>();
    private List<MusicThing> chat = new ArrayList<>();
    private List<MusicThing> Christmas = new ArrayList<>();
    private List<MusicThing> mall = new ArrayList<>();
    private List<MusicThing> morning = new ArrayList<>();
    private List<MusicThing> night = new ArrayList<>();
    private List<MusicThing> records = new ArrayList<>();
    private List<MusicThing> pokemon = new ArrayList<>();
    private List<MusicThing> weather = new ArrayList<>();
    private List<MusicThing> running = new ArrayList<>();
    private List<MusicThing> menu = new ArrayList<>();

    public MusicManager()
    {
        //TODO: create MusicThings and store them in demSongz
        //TODO: then obviously make copies for mall and whatnot so we can get a random song for a specific category
//        battle.add(put("andyougotmesayin", 35));
//        battle.add(put("SteelDeDeDrum", 60+36));
//        battle.add(put("fight", 60));
        battle.add(put("http://k003.kiwi6.com/hotlink/4p667bfkgx/robbierottenvisitsspiralmountain.mp3", 61));
        battle.add(put("http://k003.kiwi6.com/hotlink/hgomo9zqa1/The_True_Battle_Is_Yet_To_Come_Original_Orchestral_Composition_.mp3", 60+49));
        battle.add(put("http://k003.kiwi6.com/hotlink/ao38zrqlzw/battle_beep_beep_beep.mp3", 120+41));
        battle.add(put("http://k003.kiwi6.com/hotlink/3msvwn9mix/battle_we_are_the_strongest.mp3", 120+41));
        battle.add(put("http://k003.kiwi6.com/hotlink/5bm6q2001i/battle_The_Grand_Show.mp3", 120+34));
        boss.add(put("bulletformymeme", 119));
        boss.add(put("freshsqueezedmemes100followerspecial", 60+18));
        boss.add(put("intune", 57));
//        mall.add(put("animallobby", 46));
//        mall.add(put("buysomething", 31));
//        mall.add(put("fleamarket", 127));
//        mall.add(put("fleamarket2", 132));
//        mall.add(put("letsshop", 58));
//        mall.add(put("nintendogsong", 70));
//        mall.add(put("splatoonbooyahbaseshopping", 90));
//        mall.add(put("torielnospeakamericano", 120+33));
//        mall.add(put("wiishopchannel", 90));
//        mall.add(put("buysomethingwillya", 60+16));
        mall.add(put("http://k003.kiwi6.com/hotlink/itnxb4yw2p/splatoon_booyah_base_shopping.mp3", 90));
        mall.add(put("http://k003.kiwi6.com/hotlink/7dffrye0i1/mall_sanicwave.mp3", 60+60+60+35));
        mall.add(put("http://k003.kiwi6.com/hotlink/hr3e89idcx/mall_animal_lobby.mp3", 46));
        mall.add(put("http://k003.kiwi6.com/hotlink/va0jj0xhuo/mall_flea_market_circulation.mp3", 120+7));
        mall.add(put("http://k003.kiwi6.com/hotlink/b5fc4twixl/mall_lets_shop.mp3", 60));
        mall.add(put("http://k003.kiwi6.com/hotlink/d8ej3ak8cd/mall_You_reposted_in_the_wrong_flea_market.mp3", 60+46));
        morning.add(put("badblood", 51));
        morning.add(put("crazyfrog", 43));
        morning.add(put("justdoit1", 128));
        morning.add(put("meow-crorow", 94));
        morning.add(put("rickroll", 40));
        morning.add(put("soldierboy", 29));
        morning.add(put("strawberryshortcake", 35));
        morning.add(put("subway", 26));
        morning.add(put("sunrise2", 67));
        morning.add(put("spawn2", 32));
        morning.add(put("skrillexreplaceschip", 60));
        morning.add(put("unknown3", 23));
        morning.add(put("souljatune", 60+12));
        morning.add(put("spawn", 49)); //needs credit
        morning.add(put("hotelbarkley", 50));
        morning.add(put("hakunamutata", 34));
        night.add(put("minecrafttrapremix", 120+34));
        night.add(put("sonsfavorite", 42));
        night.add(put("whymca", 57));
        night.add(put("coconut", 23));
        pokemon.add(put("hiddenmishaswamp", 131));
        weather.add(put("somenewfleaswallowrap", 120+17));
        running.add(put("fight2", 90));
        running.add(put("thegrandshow", 60+18));
        put("cake", 27);
        put("welcome", 15);
        put("thegame", 6);
    }

    private MusicThing put(String name, int length)
    {
        MusicThing ok = new MusicThing(name, length);
        demSongz.put(name, ok);
        return ok;
    }

    private MusicThing randomizer(List<MusicThing> hello)
    {
        return hello.get(ThreadLocalRandom.current().nextInt(hello.size()));
    }

    /**
     * Get a specific song
     * @param song
     * @return null if song doesn't exist
     */
    public MusicThing getSong(String song)
    {
        return demSongz.get(song);
    }

    public MusicThing getMallSong()
    {
        return randomizer(mall);
    }

    public MusicThing getMorningSong()
    {
        return randomizer(morning);
    }

    public MusicThing getNightSong()
    {
        return randomizer(night);
    }

    public MusicThing getFightSong()
    {
        return randomizer(battle);
    }
}
