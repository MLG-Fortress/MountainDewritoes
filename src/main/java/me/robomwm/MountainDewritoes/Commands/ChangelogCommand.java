package me.robomwm.MountainDewritoes.Commands;

import com.robomwm.usefulutil.UsefulUtil;
import de.themoep.minedown.MineDown;
import me.robomwm.MountainDewritoes.Events.PlayerLoadedWorldEvent;
import me.robomwm.MountainDewritoes.LazyText;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 10/31/2018.
 *
 * @author RoboMWM
 */
public class ChangelogCommand implements Listener, CommandExecutor
{
    private MountainDewritoes plugin;
    private YamlConfiguration storage;
    private Map<UUID, Long> lastReadChangelog = new ConcurrentHashMap<>();

    public ChangelogCommand(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        storage = UsefulUtil.loadOrCreateYamlFile(plugin, "changelog.data");
        plugin.getCommand("changelog").setExecutor(this);
        plugin.getCommand("newlog").setExecutor(this);
        plugin.getCommand("deletelog").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("newlog"))
        {
            if (!sender.isOp())
                return false;
            String thing = String.join(" ", args);
            String time = String.valueOf(System.currentTimeMillis());
            storage.set(time, thing);
            UsefulUtil.saveYamlFile(plugin, "changelog.data", storage);
            sender.sendMessage(time + ": " + thing);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("changelog") && sender instanceof Player)
        {
            if (args.length == 0)
            {
                plugin.openBook((Player)sender, new LazyText.Builder().add(getChangelogEntries(lastReadChangelog.get(((Player)sender).getUniqueId()))).toBook());
                lastReadChangelog.put(((Player)sender).getUniqueId(), System.currentTimeMillis());
                return true;
            }
            plugin.openBook((Player)sender, getChangelogEntryBook(args[0]));
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("deletelog"))
        {
            if (!sender.isOp() || args.length == 0)
                return false;
            storage.set(String.valueOf(args[0]), null);
            UsefulUtil.saveYamlFile(plugin, "changelog.data", storage);
            return true;
        }
        return false;
    }

    public ItemStack getChangelogEntryBook(String time)
    {
        TextComponent component = LazyText.command("⬅Back                        \n","/changelog","Back to /changelog");
        return new LazyText.Builder()
                .add(component)
                .add(getChangelogEntry(time))
                .toBook();
    }

    public List<BaseComponent> getChangelogEntries(long time)
    {
        List<BaseComponent> entries = new ArrayList<>();
        int i = 0;
        for (String key : storage.getKeys(false))
        {
            TextComponent component = new TextComponent(UsefulUtil.formatTime((System.currentTimeMillis() - Long.valueOf(key)) / 1000, 0) + " ago \n");
            if (++i % 12 == 0)
                component.setText(component.getText() + "\\p");
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/changelog " + key));
            if (Long.valueOf(key) > time)
                component.setColor(ChatColor.AQUA);
            else
                component.setColor(ChatColor.DARK_AQUA);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getChangelogEntry(key)));
            entries.add(component);
        }
        entries.add(new TextComponent("    Log o' changes\n"));
        Collections.reverse(entries);
        return entries;
    }

    public BaseComponent[] getChangelogEntry(String time)
    {
        return new MineDown(storage.getString(time, "")).replace("\\n", "\n").toComponent();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        if (!lastReadChangelog.containsKey(event.getPlayer().getUniqueId()))
            lastReadChangelog.put(event.getPlayer().getUniqueId(), event.getPlayer().getLastPlayed());
    }

    @EventHandler
    private void onPlayerLoad(PlayerLoadedWorldEvent event)
    {
        Set<String> keys = new HashSet<>(storage.getKeys(false));
        long lastRead = lastReadChangelog.get(event.getPlayer().getUniqueId());
        plugin.getLogger().info(String.valueOf(lastRead));
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                int newChanges = 0;
                for (String key : keys)
                {
                    if (Long.valueOf(key) > lastRead)
                        newChanges++;
                }
                if (newChanges > 0)
                {
                    event.getPlayer().sendMessage(new LazyText.Builder().add(TipCommand.getRandomColor()
                            + Integer.toString(newChanges)
                            + " new server updates in the /changelog!")
                            .cmd("/changelog", true)
                            .toComponentArray());
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
