package me.robomwm.MountainDewritoes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2/25/2018.
 *
 * @author RoboMWM
 */
public class Emoticons implements Listener
{
    private Map<Pattern, List<String>> emojiMovie = new HashMap<>();
    public Emoticons(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        put(Pattern.compile(":shrug:"), "\u00AF\\_(\u30C4)_/\u00AF");
        put(Pattern.compile(":shrug:"), " ┐('～`)┌");
        put(Pattern.compile(":shrug:"), " ┐('～`；)┌");
        put(Pattern.compile(":shrug:"), "~\\_(''/)_/~ ");
        put(Pattern.compile(":shrug:"), "┻━┻ ︵ ¯\\ (ツ)/¯ ︵ ┻━┻");
        put(Pattern.compile(":flip:"), "( ﾉ⊙︵⊙）ﾉ ︵ ┻━┻");
        put(Pattern.compile(":flip:"), "┻━┻ ︵ ¯\\ (ツ)/¯ ︵ ┻━┻");
        put(Pattern.compile(":lenny:"), "( ͡°( ͡° ͜ʖ( ͡° ͜ʖ ͡°)ʖ ͡°) ͡°)");
        put(Pattern.compile(":lenny:"), "( ͜。 ͡ʖ ͜。)");
        put(Pattern.compile(":lenny:"), "°.ʖ ͡°");
        put(Pattern.compile(":heart:"), "♥");
        put(Pattern.compile(":\\)"), "☺");
    }
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void chatter(AsyncPlayerChatEvent event)
    {
        String message = event.getMessage();
        for (Pattern pattern : emojiMovie.keySet())
        {
            Matcher matcher = pattern.matcher(message);
            message = matcher.replaceAll(emojiMovie.get(pattern).get(ThreadLocalRandom.current().nextInt(emojiMovie.get(pattern).size())));
        }
        event.setMessage(message);
    }

    private void put(Pattern pattern, String emote)
    {
        if (!emojiMovie.containsKey(pattern))
            emojiMovie.put(pattern, new ArrayList<>());
        List<String> thing = emojiMovie.get(pattern);
        thing.add(emote);
        emojiMovie.put(pattern, thing);
    }
}
