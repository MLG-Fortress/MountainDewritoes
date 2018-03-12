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
        put(":lenny:", "ಠ_ಠ");
        put(":heart:", "♥");
        put(":swag:", "ⓈⓌⒶⒼ");
        put(":derp:", "◴_◶");
        put(":fiteme:", "ლ(ಠ益ಠლ)");
        put(":checkmark:", "✔");
        put(":x:", "✖");
        put(":box:", "☐");
        put(":checkbox:", "☑");
        put(":xbox:", "☒");
        put(":triangle:", "▲");
        put(":square:", "■");
        put(":circle:", "○");
        put(":katakana:", "ツ");
        put(":copy:", "©");
        put(":trademark:", "™");
        put(":1/2:", "½");
        put(":clock:", "⌚");
        put(":registered:", "®");
        put(":registered:", "®");
        put(":hourglass:", "⌛");
        put(":star:", "★");
        put(":star:", "☆");
        put(":armystar:", "✪");
        put(":up:", "↑");
        put(":down:", "↓");
        put(":left:", "←");
        put(":right:", "→");
        put(":sun:", "☼");
        put(":sun:", "☀");
        put(":moon:", "☾");
        put(":moon:", "☽");
        put(":cloud:", "☁");
        put(":umbrella:", "☂");
        put(":snowman:", "☃");
        put(":zap:", "ϟ");
        put(":airplane:", "✈");
        put(":crossbones:", "☠");
        put(":music:", "♪");
        put(":music:", "♫");
        put(":music:", "♪♫");
        put(":anchor:", "⚓");
        put(":warning:", "⚠");
        put(":radioactive:", "☢");
        put(":biohazard:", "☣");
        put(":coffee:", "☕");
        put(":gear:", "⚙");
        put("<3", "♥");
        put(":relaxed:", "☺");
        put(":)", "☻");
        put("$", "Ð");
        put(">:(", "Ò╭╮Ó");
        put(":(", "☹");
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
            message = matcher.replaceAll(" " + Matcher.quoteReplacement(emojiMovie.get(pattern).get(ThreadLocalRandom.current().nextInt(emojiMovie.get(pattern).size()))) + " ");
        }
        return message;
    }

    private void put(String patternString, String emote)
    {
        patternString = "(?<!\\S)\\Q" + patternString + "\\E(?!\\S)";
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
            //Match the patternString exactly, and only if nothing except whitespace is pre/appended
            emojiMovie.put(Pattern.compile(patternString), thing);
        }
        thing.add(emote);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        //Too lazy to check
        Player player = (Player)sender;

        List<BaseComponent> baseComponents = new ArrayList<>(emojiMovie.keySet().size());
        int i = 0;
        for (Pattern pattern : emojiMovie.keySet())
        {
            String code = pattern.pattern().substring(9, pattern.pattern().length() - 8);
            String example = emojiMovie.get(pattern).get(ThreadLocalRandom.current().nextInt(emojiMovie.get(pattern).size()));
            baseComponents.add(LazyUtil.getClickableSuggestion(" " + code + " ", code, example));
            i += code.length() + 2;
            if (i >= 80)
            {
                player.sendMessage(baseComponents.toArray(new BaseComponent[baseComponents.size()]));
                baseComponents.clear();
            }
        }
        player.sendMessage(baseComponents.toArray(new BaseComponent[baseComponents.size()]));
        return true;
    }
}

//    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
//    {
//        //Too lazy to check
//        Player player = (Player)sender;
//
//        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
//        BookMeta bookMeta = (BookMeta)book.getItemMeta();
//
//        List<BaseComponent> baseComponents = new ArrayList<>(emojiMovie.keySet().size());
//
//        int i = 0;
//        for (Pattern pattern : emojiMovie.keySet())
//        {
//            String code = pattern.pattern().substring(4, pattern.pattern().length() - 4);
//            String example = emojiMovie.get(pattern).get(ThreadLocalRandom.current().nextInt(emojiMovie.get(pattern).size()));
//            baseComponents.add(LazyUtil.getClickableSuggestion(code + "\n", code, example));
//            if (++i >= 14)
//            {
//                bookMeta.spigot().addPage(baseComponents.toArray(new BaseComponent[0]));
//                baseComponents.clear();
//                i = 0;
//            }
//        }
//
//        bookMeta.spigot().addPage(baseComponents.toArray(new BaseComponent[0]));
//        book.setItemMeta(bookMeta);
//        plugin.getBookUtil().openBook(player, book);
//        return true;
//    }
