package me.robomwm.MountainDewritoes.serverlist;

import me.robomwm.MountainDewritoes.Commands.ChangelogCommand;
import me.robomwm.MountainDewritoes.Commands.TipCommand;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ServerListPingListener implements Listener
{
    private static final String SPECIAL_LIST_IP = "173.249.30.10";

    private final MountainDewritoes plugin;
    private final Map<String, KnownPlayer> knownPlayersByAddress = new ConcurrentHashMap<>();
    private final String[] quotes = {
            "&b        such seber       &6many meme\n    &c%player%      &amuch plogenz       &eWow",
            "&b        such plogin       &6many %player%\n    &cspooky      &astale       &ewow",
            "&b        such word       &6many tri\n    &c%player%      &aplz joyn       &eso dispare",
            "&f/op %player%\n&7&o[Server: Opped %player%]",
            "&7&o[Server: Opped %player%]",
            "&c&lU HAV BEN SPOOKED BY SPOOKY SKILENTON\n&aJOYN OR SKELINTONS WILL EAT &d%player%",
            "&eThe quick brown &d%player% &egot ran over by a double rainbow",
            "&eThe quick brown fox got ran over by a double &d%player%",
            "&aBorn 2 L8 2 explore da Earth, born 2 s00n 2 explore da Galaxy. Born just in time 2 post &dℳℰℳℰS └( ° ͜ʖ͡°)┐",
            "&b┻━┻ ︵ ¯\\ (ツ)/¯ ︵ ┻━┻",
            "&9Can i be a staff member on your server let me be an staff member Please %player% Please Please Please Please",
            "&6My favorite number is %player%",
            "&6hollo %player% u r authroaize 2 win vacay pls credit card info",
            "&6Hello valued customer. Would you like dog pictures to clog your computer",
            "&exDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD",
            "&eAccordion to a recent survey, replacing words with the names of musical instruments in a sentence often goes undetected.",
            "&brawblox",
            "%player%: i like memes",
            "&dℳℰℳℰS └",
            "&d└( ° ͜ʖ͡°)┐",
            "&d%player%",
            "&d\n¯\\ (ツ)/¯  ",
            "&d ō_o",
            "&d\n┐('～`；)┌",
            "&d      ( ﾟ∩ﾟ)",
            "&d      ~\\_(''/)_/~ ",
            "&d      (☞ﾟヮﾟ)☞",
            "&d       °.ʖ ° ",
            "&d     (╭☞ ° ʖ °)╭☞         ┐",
            "&d      ¯\\_(°_o)_/¯     ",
            "&c      i really should be more active      ",
            "&f     %player% r u admin????!?!?!1?1?",
            "&dLocal Rapper &e\"Big Shaq\" &dpresents mind-boggling question - What counts as &a\"Quick maffs?\"",
            "&dA wild &a%player% &dhas appeared!",
            "&6r u redy 4 %player%????",
            "&ehello sir this is windows tech support\n&6you have expired tech support key",
            "&6i actually play multiplayer minesweeper with CPUPLAYER#1",
            "&3that is not a valid gamemode\n&3it can only be fidget spinners",
            "&foops my system crashed\n&fI lost my data but I had an antivirus",
            "&7r u all pretending to be afk",
            "&bBorddd? Dont 4get about da &6/&am&ci&dn&ei&fg&ba&am&ce&ds",
            "&eidk man i'm dead rn, brain empty"
    };
    private boolean colorized = false;
    private int count = 0;

    public ServerListPingListener(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        InetAddress address = event.getPlayer().getAddress() == null ? null : event.getPlayer().getAddress().getAddress();
        if (address == null)
            return;

        knownPlayersByAddress.put(address.getHostAddress(), new KnownPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName()));
    }

    @EventHandler
    private void onServerListPing(ServerListPingEvent event)
    {
        count++;
        KnownPlayer knownPlayer = knownPlayersByAddress.get(event.getAddress().getHostAddress());
        String name = knownPlayer == null ? null : knownPlayer.name;
        UUID uuid = knownPlayer == null ? null : knownPlayer.uuid;
        if (name != null)
            plugin.getLogger().info(name + " is active.");

        if (SPECIAL_LIST_IP.equalsIgnoreCase(event.getAddress().getHostAddress()))
        {
            event.setMotd(specialResponse(name));
            return;
        }

        event.setMotd(computeResponse(uuid, name));
    }

    private String computeResponse(UUID uuid, String name)
    {
        if (name == null)
            name = "u";

        if (!plugin.isServerDoneLoading())
            return ChatColor.RED + "still brewing memes, pls w8" + count;

        ChatColor color = TipCommand.getRandomColor();
        colorizeQuotes();

        try
        {
            switch (ThreadLocalRandom.current().nextInt(6))
            {
                case 0:
                    return color + quotes[ThreadLocalRandom.current().nextInt(quotes.length)];
                case 1:
                    return color + quotes[ThreadLocalRandom.current().nextInt(quotes.length)];
                case 2:
                    return color + formatBalance(uuid);
                case 3:
                    return color + findRandomOnlinePlayer() + color + " wants to play with " +
                            TipCommand.getRandomColor() + name;
                case 4:
                    return color + quotes[ThreadLocalRandom.current().nextInt(quotes.length)];
                case 5:
                    return color + "ur lucky number is " + TipCommand.getRandomColor() + count;
                case 6:
                    int too = ThreadLocalRandom.current().nextInt(4);
                    int two = ThreadLocalRandom.current().nextInt(4);
                    int four = too + two;
                    int minus = ThreadLocalRandom.current().nextInt(4);
                    int three = four - minus;
                    return TipCommand.getRandomColor() + Integer.toString(too) + color + " + " +
                            TipCommand.getRandomColor() + two + color + " = " +
                            TipCommand.getRandomColor() + four + color + " - " +
                            TipCommand.getRandomColor() + minus + color + " = " +
                            TipCommand.getRandomColor() + three + color + " quik maffs";
                case 7:
                    return color + "There r " + ChangelogCommand.lastReadChangelog.get(plugin.getServer().getOfflinePlayer(uuid).getUniqueId()) +
                            " new updates in the /log!";
            }
        }
        catch (Throwable ignored) {}

        String quote = color + quotes[ThreadLocalRandom.current().nextInt(quotes.length)];
        return quote.replace("%player%", name);
    }

    private String specialResponse(String name)
    {
        ChatColor color = TipCommand.getRandomColor();
        colorizeQuotes();

        if (name == null)
            name = "u";
        else
            name = new StringBuilder(name).insert(1, "\u200B").toString();

        String quote = color + "Hi MCList person! Join us, we have maymayz and portal guns and ships n stuff! :DD %player% " +
                quotes[ThreadLocalRandom.current().nextInt(quotes.length)];
        return quote.replace("%player%", name);
    }

    private void colorizeQuotes()
    {
        if (colorized)
            return;

        for (int i = 0; i < quotes.length; i++)
            quotes[i] = ChatColor.translateAlternateColorCodes('&', quotes[i]);
        colorized = true;
    }

    private String findRandomOnlinePlayer()
    {
        List<Player> victims = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (victims.isEmpty())
            return "U_W0T_B0T";
        return victims.get(ThreadLocalRandom.current().nextInt(victims.size())).getDisplayName();
    }

    private String formatBalance(UUID uuid)
    {
        Economy economy = plugin.getEconomy();
        if (economy == null)
            return "economy machine broke";

        OfflinePlayer player = uuid == null ? null : plugin.getServer().getOfflinePlayer(uuid);
        return player == null ? economy.format(0D) : economy.format(economy.getBalance(player));
    }

    private record KnownPlayer(UUID uuid, String name) {}
}
