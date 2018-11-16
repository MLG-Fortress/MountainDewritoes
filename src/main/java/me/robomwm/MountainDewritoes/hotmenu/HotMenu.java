package me.robomwm.MountainDewritoes.hotmenu;

import me.robomwm.MountainDewritoes.Commands.TipCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 11/15/2018.
 *
 * @author RoboMWM
 */
public class HotMenu implements Listener
{

    private Plugin plugin;
    private Map<Player, Menu> viewers = new HashMap<>();

    public HotMenu(Plugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void cancel(Player player)
    {
        Menu menu = viewers.remove(player);
        if (menu != null)
            menu.unregister(true);
    }

    @EventHandler
    private void onPlayerScrollWheel(PlayerItemHeldEvent event)
    {
        Menu menu = viewers.get(event.getPlayer());
        if (menu == null)
            return;
        if (event.getNewSlot() - event.getPreviousSlot() == 0) //idk why this fires twice
            return;
        menu.setSelectedItem(event.getNewSlot());
        //This doesn't work because the event doesn't fire for _every change,_ sometimes it will skip on fast scroll.
//        final private int SLOTS = 9;
//        int difference = event.getNewSlot() - event.getPreviousSlot();
//        int absoluteDifference = Math.abs(difference);
//        if (absoluteDifference == SLOTS - 1)
//            difference = -difference % SLOTS - 2;
//        else if (absoluteDifference > 1)
//        {
//            menu.setSelectedItem(event.getNewSlot());
//            executeSelectionOrOpenMenu(event.getPlayer());
//            return;
//        }
//        menu.changeSelection(difference);
//        event.setCancelled(true);

    }

    @EventHandler
    private void onPressF(PlayerSwapHandItemsEvent event)
    {
        Player player = event.getPlayer();
        if (player.isSneaking())
            return;
        executeSelectionOrOpenMenu(player);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onSneak(PlayerToggleSneakEvent event)
    {
        if (event.isSneaking())
            cancel(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryOpen(InventoryOpenEvent event)
    {
        cancel((Player)event.getPlayer());
    }

    private void executeSelectionOrOpenMenu(Player player)
    {
        Menu menu = viewers.remove(player);
        if (menu == null)
            viewers.put(player, new Menu(plugin, player, player.getInventory().getHeldItemSlot()));
        else
        {
            switch (menu.unregister(false))
            {
                case 1:
                    player.performCommand("book"); //TODO: direct method call
                    break;
                case 9:
                    player.sendActionBar("You can also sneak to cancel out of the HotMenu.");
                    break;
            }
        }
    }
}

/**
 * Similar to ActionCenter but doesn't yield.
 */
class Menu
{
    private Plugin plugin;
    private Scoreboard scoreboard;
    private Objective objective;
    private Player player;
    private List<String> entries = new ArrayList<>(10);
    private int selectedItem = 1;
    private Team[] currentDisplay = new Team[10];
    private int initialHotbarSlot;
    private ChatColor color = TipCommand.getRandomColor();

    Menu(Plugin plugin, Player player, int hotbarSlot)
    {
        this.plugin = plugin;
        this.player = player;
        this.initialHotbarSlot = hotbarSlot;
        this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("hotmenu", "dummy",
                ChatColor.WHITE + "Choose with " + ChatColor.YELLOW +
                        "scrollwheel." + ChatColor.WHITE + " Press " + ChatColor.YELLOW +
                        "F" + ChatColor.WHITE + " to select.");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        entries.add("");
        entries.add("Open /book");
        entries.add("Pay respects");
        entries.add("");
        entries.add("");
        entries.add("");
        entries.add("");
        entries.add("");
        entries.add("");
        entries.add("Cancel");
        player.getInventory().setHeldItemSlot(0);
        player.setScoreboard(this.scoreboard);
        refreshDisplay();
    }

    public void setSelectedItem(int selectedItem) //TODO: sounds
    {
        this.selectedItem = selectedItem + 1;
        refreshDisplay();
    }

    private void refreshDisplay()
    {
        //set scoreboard lines
        int i = 0;
        for (String line : entries)
        {
            if (currentDisplay[i] == null)
            {
                String teamName = ChatColor.values()[i].toString();
                currentDisplay[i] = scoreboard.registerNewTeam(teamName);
                currentDisplay[i].addEntry(teamName);
                objective.getScore(teamName).setScore(-i);
            }
            if (i == selectedItem)
                currentDisplay[i++].setPrefix(color + "→ " + line + " ←"); //TODO: unicode arrows
            else
                currentDisplay[i++].setPrefix(ChatColor.GRAY + "   " + line);
        }
    }

    //TODO: sounds
    public int unregister(boolean cancel)
    {
        if (cancel)
        {
            if (player.getScoreboard() == scoreboard)
                player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
            player.getInventory().setHeldItemSlot(initialHotbarSlot);
            return -1;
        }

        for (int i = 0; i < entries.size(); i++)
        {
            if (selectedItem != i)
                entries.set(i, "");
        }
        refreshDisplay();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (player.getScoreboard() == scoreboard)
                    player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
            }
        }.runTaskLater(plugin, 7L);

        player.getInventory().setHeldItemSlot(initialHotbarSlot);
        return selectedItem;
    }
}