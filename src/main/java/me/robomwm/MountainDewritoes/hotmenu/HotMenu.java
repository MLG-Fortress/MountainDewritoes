package me.robomwm.MountainDewritoes.hotmenu;

import me.robomwm.MountainDewritoes.Commands.TipCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
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
    final private int SLOTS = 9;
    private Plugin plugin;
    private Map<Player, Menu> viewers = new HashMap<>();

    public HotMenu(Plugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onPlayerScrollWheel(PlayerItemHeldEvent event)
    {
        Menu menu = viewers.get(event.getPlayer());
        if (menu == null)
            return;
        int difference = event.getNewSlot() - event.getPreviousSlot();
        int absoluteDifference = Math.abs(difference);
        if (absoluteDifference == SLOTS - 1)
            difference = -difference % SLOTS - 2;
        else if (absoluteDifference > 1)
        {
            menu.setSelectedItem(event.getNewSlot());
            executeSelectionOrOpenMenu(event.getPlayer());
            return;
        }
        menu.changeSelection(difference);
        event.setCancelled(true);
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

    private void executeSelectionOrOpenMenu(Player player)
    {
        Menu menu = viewers.remove(player);
        if (menu == null)
            viewers.put(player, new Menu(plugin, player));
        else
        {
            switch (menu.unregister())
            {
                case 0:
                    player.performCommand("/book"); //TODO: direct method call
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
    private int selectedItem = 0;
    private Team[] currentDisplay = new Team[10];

    Menu(Plugin plugin, Player player)
    {
        this.plugin = plugin;
        this.player = player;
        this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("hotmenu", "dummy", "Use scrollwheel. Press F again to select.");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        entries.add("Open /book");
        entries.add("Test item");
        player.setScoreboard(this.scoreboard);
    }

    public void changeSelection(int item)
    {
        selectedItem = (selectedItem + item) % 9;
        refreshDisplay();
    }

    public void setSelectedItem(int selectedItem)
    {
        this.selectedItem = selectedItem;
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
                currentDisplay[i++].setPrefix(TipCommand.getRandomColor() + "> " + line + " <"); //TODO: unicode arrows
            else
                currentDisplay[i++].setPrefix(ChatColor.GRAY + " " + line + " ");
        }
    }

    public int unregister()
    {
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
        }.runTaskLater(plugin, 2L);
        return selectedItem;
    }
}