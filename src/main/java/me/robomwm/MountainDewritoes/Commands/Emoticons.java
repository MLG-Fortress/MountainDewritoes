package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2/25/2018.
 *
 * Superceded by CustomEmotes plugin
 *
 * @author RoboMWM
 */
public class Emoticons implements Listener
{
    public Emoticons(MountainDewritoes plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void chatter(AsyncPlayerChatEvent event)
    {
        event.setMessage(playEmojiMovie(event.getPlayer(), event.getMessage()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void commands(PlayerCommandPreprocessEvent event)
    {
        event.setMessage(playEmojiMovie(event.getPlayer(), event.getMessage()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void signer(SignChangeEvent event)
    {
        String[] lines = event.getLines();
        int i = 0;
        for (String line : lines)
            event.setLine(i++, playEmojiMovie(event.getPlayer(), line));
    }

    private Pattern money = Pattern.compile("\\$");
    private Pattern serverAdvertisin = Pattern.compile("(?i)\\b[a-zA-z0-9]+\\.us\\.to\\b");
    private Pattern pickle = Pattern.compile("(?i)\\b(h+y+p+i+x+e+l+|re*a*lms?)\\b");
    private Pattern vain = Pattern.compile("(?i)\\bgod\\b");

    private String playEmojiMovie(Player player, String message)
    {
        Matcher matcher = money.matcher(message);
        message = matcher.replaceAll("Ð");
        matcher = serverAdvertisin.matcher(message);
        message = matcher.replaceAll("Tech Fortress tf.robomwm.com");
        matcher = pickle.matcher(message);
        message = matcher.replaceAll("/minigames");
        matcher = vain.matcher(message);
        message = matcher.replaceAll(player.getName());

        return message;
    }
}
