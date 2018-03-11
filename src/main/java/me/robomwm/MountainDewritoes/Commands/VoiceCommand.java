package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 7/26/2017.
 *
 * @author RoboMWM
 */
public class VoiceCommand implements CommandExecutor
{
    private MountainDewritoes plugin;
    private Map<UUID, String> UUIDtoName = new HashMap<>();

    public VoiceCommand(MountainDewritoes mountainDewritoes)
    {
        this.plugin = mountainDewritoes;
        UUIDtoName.put(UUID.fromString("35b16e81-5df0-494b-b057-7fb77b0b6a85"), "xkitty");
    }

    private ItemStack getBook()
    {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta)book.getItemMeta();

        bookMeta.spigot().addPage(buildPage("Voicelines:\n",
                "Greetings: ",
                getClickableChat("Hello,", "v hello", "say Hi"),
                getClickableChat(" Thanks", "v thanks", "thx"),
                "\nCallouts: ",
                getClickableChat("Over here!", "v overhere", null)));
        book.setItemMeta(bookMeta);
        return book;
    }

    private BaseComponent[] buildPage(Object... strings)
    {
        List<BaseComponent> baseComponents = new ArrayList<>(strings.length);
        for (Object object : baseComponents)
        {
            if (object instanceof TextComponent)
                baseComponents.add((TextComponent)object);
            else if (object instanceof String)
                baseComponents.addAll(Arrays.asList(TextComponent.fromLegacyText((String)object)));
        }
        return baseComponents.toArray(new BaseComponent[baseComponents.size()]);
    }

    private TextComponent getClickableChat(String message, String command, String hover)
    {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        if (hover != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return textComponent;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        if (args.length <= 0)
        {
            plugin.getBookUtil().openBook(player, getBook());
            sender.sendMessage(ChatColor.GOLD + "/v <voiceline>");
            sender.sendMessage("Voicelines: hello, over here");
            return false;
        }

        float volume = 1;
        String voiceCommand = String.join("", args).toLowerCase();

        //If it's a known/common voice line, also send an actionbar message to nearby players
        switch (voiceCommand)
        {
            case "hello":
                broadcastMessageNearby(player, 16, "hello");
                break;
            case "overhere":
                broadcastMessageNearby(player, 32, "over here");
                flashPlayer(player);
                volume = 2f;
                break;
        }

        String sound;

        if (UUIDtoName.containsKey(player.getUniqueId()))
        {
            sound = UUIDtoName.get(player.getUniqueId()) + "." + voiceCommand;
        }
        else
        {
            sound = "tts." + voiceCommand;
        }

        player.getWorld().playSound(player.getLocation(), sound, SoundCategory.VOICE, volume, 1.0f);

        return true;
    }

    private void flashPlayer(Player player)
    {

    }

    private void broadcastMessageNearby(Player player, int distance, String message)
    {
        message = player.getDisplayName() + ChatColor.AQUA + " says " + message + "!";
        Location location = player.getLocation();
        for (Player target : player.getWorld().getPlayers())
        {
            if (location.distanceSquared(target.getLocation()) < distance * distance)
                plugin.timedActionBar(target, 0, message);
        }
    }
}
