package me.robomwm.MountainDewritoes.Commands;

import com.robomwm.grandioseapi.GrandioseAPI;
import com.robomwm.grandioseapi.player.GrandPlayer;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import me.robomwm.usefulutil.UsefulUtil;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2/20/2017.
 * Yes I realized my package names are capitalized
 * Oh well ¯\_(ツ)_/¯
 * @author RoboMWM
 */
public class NickCommand implements CommandExecutor, Listener
{
    private GrandioseAPI grandioseAPI;
    private String acceptableColors;
    private MountainDewritoes plugin;
    private YamlConfiguration playerColorsYaml;
    private Chat chat;

    public NickCommand(MountainDewritoes plugin)
    {
        //Set<String> colorThingy = new HashSet<>(Arrays.asList("Aqua", "Blue", "Dark_Blue", "Green", "Dark_Green", "Light_Purple", "Dark_Purple", "Red", "Dark_Red", "Gold", "Yellow"));
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        grandioseAPI = (GrandioseAPI)plugin.getServer().getPluginManager().getPlugin("GrandioseAPI");
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();

        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (ChatColor ok : ChatColor.values())
        {
            if (isBannedColor(ok))
                continue;
            builder.append(ok);
            builder.append(ok.name());
            builder.append(", ");
            if (++i % 3 == 0)
                builder.append("\n");
        }
        acceptableColors = builder.toString().substring(0, builder.length() - 2);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        if (args.length < 1)
        {
            player.sendMessage("/nick <color>");
            player.sendMessage("colors: " + acceptableColors);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("nick"))
        {
            ChatColor color;
            try
            {
                color = ChatColor.valueOf(args[0].toUpperCase());
            }
            catch (Exception e)
            {
                color = ChatColor.getByChar(args[0]);
                if (color == null && args[0].length() > 1)
                {
                    color = ChatColor.getByChar(args[0].substring(1));
                }
            }
            if (color == null || isBannedColor(color))
            {
                player.sendMessage("Valid colors: " + acceptableColors);
                return true;
            }
            //player.performCommand("enick " + convertColor(color) + player.getName());
            grandioseAPI.getGrandPlayerManager().getGrandPlayer(player).setNameColor(color);
            player.setDisplayName(color + chat.getPlayerPrefix(player) + player.getName() + ChatColor.RESET);
            return true;
        }
        return false;
    }

    private boolean isBannedColor(ChatColor color)
    {
        if (color.isFormat())
            return true;

        switch (color)
        {
            case BLACK:
            case DARK_GRAY:
            case GRAY:
            case WHITE:
                return true;
        }
        return false;
    }

    //Automatically give a color to new players
    @EventHandler(priority = EventPriority.LOWEST)
    private void colorizeNewPlayers(PlayerJoinEvent event)
    {
        GrandPlayer grandPlayer = grandioseAPI.getGrandPlayerManager().getGrandPlayer(event.getPlayer());
        event.getPlayer().setDisplayName(grandPlayer.getNameColor() + chat.getPlayerPrefix(event.getPlayer()) + event.getPlayer().getName() + ChatColor.RESET);
    }

    //Now resides in GrandioseAPI
    public ChatColor getChatColor(Player player)
    {
        return ChatColor.getByChar(getColorCode(player));
    }

    public String getColorCode(Player player)
    {
        //Get hash code of player's UUID
        int colorCode = player.getUniqueId().hashCode();
        //Ensure number is positive
        colorCode = Math.abs(colorCode);

        //Will make configurable, hence this
        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e".split(",");
        //Divide hash code by length of acceptableColors, and use remainder
        //to determine which index to use (like a hashtable/map/whatever)
        colorCode = (colorCode % acceptableColors.length);
        String stringColorCode = acceptableColors[colorCode];

        return stringColorCode;
    }

    //Used to be back when we used Essentials /nick
    private String convertColor(ChatColor color)
    {
        switch (color)
        {
            case AQUA:
                return "&b";
            case BLUE:
                return "&9";
            case DARK_AQUA:
                return "&3";
            case DARK_BLUE:
                return "&1";
            case DARK_GREEN:
                return "&2";
            case DARK_PURPLE:
                return "&5";
            case DARK_RED:
                return "&4";
            case GOLD:
                return "&6";
            case GREEN:
                return "&a";
            case LIGHT_PURPLE:
                return "&d";
            case RED:
                return "&c";
            case YELLOW:
                return "&e";
        }
        return null;
    }
}
