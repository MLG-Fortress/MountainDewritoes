package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.LazyUtil;
import me.robomwm.MountainDewritoes.MountainDewritoes;
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

import java.util.HashMap;
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

        bookMeta.spigot().addPage(LazyUtil.buildPage("Voicelines:\n",
                "Greetings:\n",
                LazyUtil.getClickableCommand("Hello,", "/v hello", "say Hi"),
                LazyUtil.getClickableCommand(" Thanks", "/v thanks", "thx"),
                "\nCallouts:\n",
                LazyUtil.getClickableCommand("Over here!", "/v overhere", null)));
        book.setItemMeta(bookMeta);
        return book;
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
                broadcastMessageNearby(player, 16, "says hello!");
                break;
            case "overhere":
                broadcastMessageNearby(player, 32, "says over here!");
                flashPlayer(player);
                volume = 2f;
                break;
            case "thx":
            case "thank":
            case "thanks":
                broadcastMessageNearby(player, 16, "says Thanks!");
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
        message = player.getDisplayName() + ChatColor.AQUA + message;
        Location location = player.getLocation();
        for (Player target : player.getWorld().getPlayers())
        {
            if (location.distanceSquared(target.getLocation()) < distance * distance)
                plugin.timedActionBar(target, 0, message);
        }
    }
}
