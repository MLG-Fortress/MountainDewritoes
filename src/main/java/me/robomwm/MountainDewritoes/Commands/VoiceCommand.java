package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.MountainDewritoes.Music.MusicThing;
import me.robomwm.MountainDewritoes.SimpleClansListener;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created on 7/26/2017.
 *
 * @author RoboMWM
 */
public class VoiceCommand implements CommandExecutor
{
    private MountainDewritoes instance;
    private Map<UUID, String> UUIDtoName = new HashMap<>();

    public VoiceCommand(MountainDewritoes mountainDewritoes)
    {
        this.instance = mountainDewritoes;
        UUIDtoName.put(UUID.fromString("35b16e81-5df0-494b-b057-7fb77b0b6a85"), "xkitty");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        if (args.length <= 0)
        {
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
                broadcastMessageNearby(player, 16, "hello", 0);
                break;
            case "overhere":
                broadcastMessageNearby(player, 32, "over here", 3);
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

    private void broadcastMessageNearby(Player player, int distance, String message, int length)
    {
        message = player.getDisplayName() + ChatColor.AQUA + " says " + message + "!";
        Location location = player.getLocation();
        for (Player target : player.getWorld().getPlayers())
        {
            if (location.distanceSquared(target.getLocation()) < distance * distance)
                instance.timedActionBar(player, 0, message);
        }
    }
}
