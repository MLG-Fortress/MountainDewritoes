package me.robomwm.MountainDewritoes.Sounds;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Created by RoboMWM on 10/8/2016.
 * It's not just a manager, it contains it all!
 * No way am I gonna split this into classes (at least not now)
 */
public class AtmosphericManager implements Listener
{
    MountainDewritoes instance;
    World MALL;
    World SPAWN;
    AtomicBoolean over10Minutes = new AtomicBoolean(true);
    //Pattern hello = Pattern.compile("\\bhello\\b|\\bhi\\b|\\bhey\\b|\\bhai\\b");
    //Pattern bye = Pattern.compile("\\bsee you\\b|\\bc u\\b|\\bbye\\b");
    MusicManager musicManager = new MusicManager();
    public AtmosphericManager(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
        MALL = instance.getServer().getWorld("mall");
        SPAWN = instance.getServer().getWorld("minigames");
    }

    public void morningListener()
    {
        playSoundGlobal(musicManager.getMorningSong());
    }

    public void nightListener()
    {
        playSoundGlobal(musicManager.getNightSong());
    }

    /**
     * Has it been 10 minutes since we last (globally) played a song?
     * Used for global chat trigger primarily
     */
    boolean hasItBeen10minutes(boolean reset)
    {
        if (over10Minutes.get())
        {
            if (reset)
                new BukkitRunnable()
                {
                    public void run()
                    {
                        over10Minutes.set(true);
                    }
                }.runTaskLater(instance, 20L * 60L * 10L);
            return true;
        }
        else
            return false;
    }

    /**Music always stops when player changes worlds*/
    @EventHandler
    void changeWorldResetMetadata(PlayerChangedWorldEvent event)
    {
        event.getPlayer().removeMetadata("ListeningToMusic", instance);
    }

    /**
     * Plays sound to players, unless they're already listening to something else
     * "Thread-safe"
     * @param sound Sound to play
     * @param world World to play sound in. Null if global
     * @param delay How long to wait in seconds before playing the sound
     */
    void playSound(MusicThing sound, World world, int delay)
    {
        Long time = System.currentTimeMillis(); //Used to determine if metadata should be removed
        new BukkitRunnable()
        {
            public void run()
            {
                for (Player player : instance.getServer().getOnlinePlayers())
                {
                    if (player.hasMetadata("ListeningToMusic") || player.hasMetadata("DEAD") || player.isDead())
                        continue;
                    if (world != null && player.getWorld() != world)
                        continue;
                    player.setMetadata("ListeningToMusic", new FixedMetadataValue(instance, time));
                    new BukkitRunnable()
                    {
                        public void run()
                        {
							if (!player.hasMetadata("ListeningToMusic"))
								return;
							//Can happen if another event removed metadata earlier (worldchange) and player received new music
                            if (player.getMetadata("ListeningToMusic").equals(time))
                                player.removeMetadata("ListeningToMusic", instance);
                        }
                    }.runTaskLater(instance, sound.getLength());
                    player.playSound(player.getLocation(), sound.getSoundName(), SoundCategory.AMBIENT, 3000000f, 1.0f);
                }
            }
        }.runTaskLater(instance, delay * 20L);
    }

    void playSoundGlobal(MusicThing sound)
    {
        playSound(sound, null, 0);
    }

    /** Play world-specific "ambient" sounds, when player changes worlds, after a 10 second delay */
    @EventHandler(priority = EventPriority.HIGHEST)
    void playAmbientMusic(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (world == MALL)
            playSound(musicManager.getMallSong(), MALL, 10);
        else if (world == SPAWN)
            playSound(musicManager.getSpawnSong(), SPAWN, 30);
    }

    /** Play sounds globally based on certain keywords
     * Totally not even close to ready yet, I might even scrap this idea*/
    //@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
//    void onPlayerChatPlaySounds(AsyncPlayerChatEvent event)
//    {
//        //Don't care about muted/semi-muted chatters
//        if (event.getRecipients().size() < instance.getServer().getOnlinePlayers().size())
//            return;
//
//        if (!hasItBeen10minutes(false))
//            return;
//
//        String message = ChatColor.stripColor(event.getMessage().toLowerCase());
//
//        //No need to block the event to check this
//        new BukkitRunnable()
//        {
//            public void run()
//            {
//                if (hello.matcher(message).matches())
//                    playSoundGlobal("fortress.hello", 41);
//                else if (bye.matcher(message).matches())
//                    playSoundGlobal("fortress.bye", 35);
//                //TODO: etc.
//            }
//        }.runTaskAsynchronously(instance);
//    }


}

class MusicThing
{
    private String soundName;
    private long length; //Stored in ticks
    public MusicThing(String name, int seconds)
    {
        this.soundName = name;
        this.length = seconds * 20L; //autoconvert seconds to length
    }
    public String getSoundName()
    {
        return this.soundName;
    }
    public long getLength()
    {
        return this.length;
    }
}

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
    private List<MusicThing> spawn = new ArrayList<>();
    private List<MusicThing> pokemon = new ArrayList<>();
    private List<MusicThing> weather = new ArrayList<>();

    public MusicManager()
    {
        //TODO: create MusicThings and store them in demSongz
        //TODO: then obviously make copies for mall and whatnot so we can get a random song for a specific category
        battle.add(put("andyougotmesayin", 35));
        battle.add(put("SteelDeDeDrum", 60+36));
        boss.add(put("BulletForMyMeme", 119));
        boss.add(put("FreshSqueezedMemes100FollowerSpecial", 60+18));
        mall.add(put("AlphysShopChannel", 110));
        mall.add(put("AnimalLobby", 46));
        mall.add(put("buysomething", 31));
        mall.add(put("fleamarket", 127));
        mall.add(put("fleamarket2", 132));
        mall.add(put("GrandDadInAnElevator", 16));
        mall.add(put("LetsShop", 58));
        mall.add(put("Nintendogsong", 70));
        mall.add(put("SplatoonBooyahBaseShopping", 90));
        mall.add(put("torielnospeakamericano", 120+33));
        mall.add(put("WiiShopChannel", 90));
        mall.add(put("WiiShoppingInsideaHouse", 59));
        morning.add(put("badblood", 51));
        morning.add(put("crazyfrog", 43));
        morning.add(put("justdoit1", 128));
        morning.add(put("Meow-croRow", 94));
        morning.add(put("minecrafttrapremix", 120+34));
        morning.add(put("rickroll", 40));
        morning.add(put("soldierboy", 29));
        morning.add(put("strawberryshortcake", 35));
        morning.add(put("subway", 26));
        morning.add(put("sunrise", 45));
        morning.add(put("sunrise2", 67));
        night.add(put("sonsfavorite", 42));
        night.add(put("whymca", 57));
        spawn.add(put("hotel303", 42));
        spawn.add(put("HotelBarkley", 50));
        spawn.add(put("spawn", 49)); //Needs credit
        spawn.add(put("spawn2", 32));
        spawn.add(put("TheGrandShow", 60+18));
        spawn.add(put("SkrillexReplacesChip", 60));
        pokemon.add(put("HiddenMishaSwamp", 131));
        weather.add(put("SomeNewFleaswallowRap", 120+17));
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

    public MusicThing getSpawnSong()
    {
        return randomizer(spawn);
    }

    public MusicThing getMorningSong()
    {
        return randomizer(morning);
    }

    public MusicThing getNightSong()
    {
        return randomizer(night);
    }
}
