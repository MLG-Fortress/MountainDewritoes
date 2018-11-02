package me.robomwm.MountainDewritoes.Commands;

import com.robomwm.usefulutil.UsefulUtil;
import de.themoep.minedown.MineDown;
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
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 10/31/2018.
 *
 * @author RoboMWM
 */
public class ChangelogCommand implements Listener, CommandExecutor
{
    private MountainDewritoes plugin;
    private YamlConfiguration storage;

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
            storage.set(String.valueOf(System.currentTimeMillis()), thing);
            UsefulUtil.saveYamlFile(plugin, "changelog.data", storage);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("changelog") && sender instanceof Player)
        {
            if (args.length == 0)
            {
                ItemStack book = getEntryList();
                plugin.openBook((Player)sender, book);
                ((Player)sender).spigot().sendMessage(getChangelogEntries().toArray(new BaseComponent[0]));
                return true;
            }
            plugin.openBook((Player)sender, getChangelogEntryBook(args[0]));
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("deletelog"))
        {
            if (!sender.isOp())
                return false;
            storage.set(String.valueOf(System.currentTimeMillis()), null);
            UsefulUtil.saveYamlFile(plugin, "changelog.data", storage);
            return true;
        }
        return false;
    }

    public ItemStack getChangelogEntryBook(String time)
    {
        TextComponent component = LazyText.command("⬅Back                        \n","/changelog","Back to /changelog");
        BookMeta bookMeta = LazyText.getBookMeta();
        LazyText.Builder builder = new LazyText.Builder();
        List<BaseComponent> components = new LazyText.Builder()
                .add(component)
                .add(getChangelogEntry(time))
                .getBaseComponents();
        bookMeta.spigot().setPages(LazyText.buildPages(20, 12, components));
        return LazyText.getBook(bookMeta);
    }

    public ItemStack getEntryList()
    {
        BookMeta meta = LazyText.getBookMeta();
        meta.spigot().setPages(LazyText.buildPages(50, 20, getChangelogEntries()));
        return LazyText.getBook(meta);
    }

    public List<BaseComponent> getChangelogEntries()
    {
        List<BaseComponent> entries = new ArrayList<>();
        for (String key : storage.getKeys(false))
        {
            TextComponent component = new TextComponent(UsefulUtil.formatTime(Long.valueOf(key) / 1000, 0) + "\n");
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/changelog " + key));
            component.setColor(ChatColor.AQUA);
            //TODO: truncate preview, word wrap
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getChangelogEntry(key)));
            entries.add(component);
        }
        return entries;
    }

    public BaseComponent[] getChangelogEntry(String time)
    {
        return new MineDown(storage.getString(time, "")).replace("\\n", "\n").toComponent();
    }
}
