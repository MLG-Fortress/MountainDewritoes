package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.LazyUtil;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
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
public class Emoticons implements CommandExecutor, Listener
{
    private MountainDewritoes plugin;
    private Map<Pattern, List<String>> emojiMovie = new HashMap<>();
    public Emoticons(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        put(":shrug:", "\u00AF\\_(\u30C4)_/\u00AF");
        put(":shrug:", " ┐('～`)┌");
        put(":shrug:", " ┐('～`；)┌");
        put(":shrug:", "~\\_(''/)_/~ ");
        put(":flip:", "( ﾉ⊙︵⊙）ﾉ ︵ ┻━┻");
        put(":flip:", "┻━┻ ︵ ¯\\ (ツ)/¯ ︵ ┻━┻");
        put(":flip:", "(ノᚖ⍘ᚖ)ノ彡┻━┻");
        put(":lenny:", "( ͡°( ͡° ͜ʖ( ͡° ͜ʖ ͡°)ʖ ͡°) ͡°)");
        put(":lenny:", "( ͜。 ͡ʖ ͜。)");
        put(":lenny:", "乁(´益`)ㄏ");
        put(":lenny:", "| ﾟ! ﾟ|");
        put(":lenny:", "°.ʖ ͡°");
        put(":heart:", "♥");
        put("<3", "♥");
        put(":relaxed:", "☺");
        put("$", "Ð");
        //put(">:\\(", "Ò╭╮Ó");
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
            emojiMovie.put(Pattern.compile(Matcher.quoteReplacement(patternString)), thing);
        }
        thing.add(emote);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        //Too lazy to check
        Player player = (Player)sender;

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta)book.getItemMeta();

        List<BaseComponent> baseComponents = new ArrayList<>(emojiMovie.keySet().size());

        int i = 0;
        for (Pattern pattern : emojiMovie.keySet())
        {
            String code = pattern.toString();
            String example = emojiMovie.get(pattern).get(ThreadLocalRandom.current().nextInt(emojiMovie.get(pattern).size()));
            baseComponents.add(LazyUtil.getClickableSuggestion(code + "\n", code, example));
            if (++i >= 14)
            {
                bookMeta.spigot().addPage(baseComponents.toArray(new BaseComponent[0]));
                baseComponents.clear();
                i = 0;
            }
        }

        bookMeta.spigot().addPage(baseComponents.toArray(new BaseComponent[0]));
        plugin.getBookUtil().openBook(player, book);
        return true;
    }
}
