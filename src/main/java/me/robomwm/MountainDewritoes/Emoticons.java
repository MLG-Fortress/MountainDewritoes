package me.robomwm.MountainDewritoes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
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
        put(":shrug:", "\u00AF\\_(\u30C4)_/\u00AF");
        put(":shrug:", " ┐('～`)┌");
        put(":shrug:", " ┐('～`；)┌");
        put(":shrug:", "~\\_(''/)_/~ ");
        put(":shrug:", "┻━┻ ︵ ¯\\ (ツ)/¯ ︵ ┻━┻");
        put(":flip:", "( ﾉ⊙︵⊙）ﾉ ︵ ┻━┻");
        put(":flip:", "┻━┻ ︵ ¯\\ (ツ)/¯ ︵ ┻━┻");
        put(":lenny:", "( ͡°( ͡° ͜ʖ( ͡° ͜ʖ ͡°)ʖ ͡°) ͡°)");
        put(":lenny:", "( ͜。 ͡ʖ ͜。)");
        put(":lenny:", "°.ʖ ͡°");
        put(":heart:", "♥");
        put(":\\)", "☺");
        put("\\$", "Ð");
    }
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void chatter(AsyncPlayerChatEvent event)
    {
        event.setMessage(playEmojiMovie(event.getMessage()));
    }
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void signer(SignChangeEvent event)
    {
        String[] lines = event.getLines();
        int i = 0;
        for (String line : lines)
            event.setLine(i++, playEmojiMovie(line));
    }

    private String playEmojiMovie(String message)
    {
        for (Pattern pattern : emojiMovie.keySet())
        {
            Matcher matcher = pattern.matcher(message);
            message = matcher.replaceAll(Matcher.quoteReplacement(emojiMovie.get(pattern).get(ThreadLocalRandom.current().nextInt(emojiMovie.get(pattern).size()))));
        }
        return message;
    }

    private void put(String patternString, String emote)
    {
        List<String> thing = null;
        for (Pattern pattern : emojiMovie.keySet())
        {
            if (pattern.pattern().equalsIgnoreCase(patternString))
            {
                thing = emojiMovie.get(pattern);
                break;
            }
        }
        if (thing == null)
        {
            thing = new ArrayList<>();
            emojiMovie.put(Pattern.compile(patternString), thing);
        }
        thing.add(emote);
    }
}
