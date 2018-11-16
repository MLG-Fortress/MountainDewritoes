package me.robomwm.MountainDewritoes.hotmenu;

import me.robomwm.MountainDewritoes.Commands.TipCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
        Menu menu = getMenu(player, false);
        if (menu != null)
            menu.unregister(true);
    }

    /**
     *
     * @param player
     * @param display If we should display one if none are being displayed right now
     * @return The active menu only if it was already active. Null otherwise
     */
    public Menu getMenu(Player player, boolean display)
    {
        Menu menu = viewers.get(player);
        if (menu == null && !display) //if no menu exists and don't create, return now.
            return null;

        if (display && menu == null) //we should create if menu doesn't exist
        {
            menu = new Menu(plugin, player);
            viewers.put(player, menu);
            return null;
        }
        else if (menu.isRegistered()) //if menu exists and is active, return it.
            return menu;
        else if (display) //reactivate existing menu if we should create.
            menu.register();

        return null;
    }

    @EventHandler
    private void onPlayerScrollWheel(PlayerItemHeldEvent event)
    {
        Menu menu = getMenu(event.getPlayer(), false);
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
        Menu menu = getMenu(player, true);
        if (menu != null)
            menu.unregister(false);
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

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event)
    {
        cancel((Player)event.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        viewers.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onAttack(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.PHYSICAL)
            return;
        Menu menu = getMenu(event.getPlayer(), false);
        if (menu != null)
        {
            menu.unregister(false);
            event.setCancelled(true);
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
    private boolean registered;

    Menu(Plugin plugin, Player player)
    {
        this.plugin = plugin;
        this.player = player;
        this.initialHotbarSlot = player.getInventory().getHeldItemSlot();
        this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("hotmenu", "dummy",
                ChatColor.WHITE + "Choose with " + ChatColor.YELLOW +
                        "scrollwheel." + ChatColor.WHITE + " Press " + ChatColor.YELLOW +
                        "F" + ChatColor.WHITE + " to select.");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        entries.add("");
        entries.add("Open /book");
        entries.add("Hello!");
        entries.add("Over here!");
        entries.add("");
        entries.add("");
        entries.add("");
        entries.add("");
        entries.add("Pay respects");
        entries.add("Cancel");
        register();

    }

    public boolean isRegistered()
    {
        return registered;
    }

    public void register()
    {
        registered = true;
        color = TipCommand.getRandomColor();
        player.getInventory().setHeldItemSlot(selectedItem - 1);
        refreshDisplay();
        player.setScoreboard(this.scoreboard);
    }

    public void setSelectedItem(int selectedItem) //TODO: sounds
    {
        this.selectedItem = selectedItem + 1;
        refreshDisplay();
    }

    private void refreshDisplay()
    {
        //set scoreboard lines
        String color = this.color.toString() + ChatColor.UNDERLINE;
        String inactiveColor = ChatColor.GRAY.toString();
        if (!registered)
        {
            color += ChatColor.ITALIC.toString();
            inactiveColor = ChatColor.DARK_GRAY.toString();
        }

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
                currentDisplay[i++].setPrefix(color + line + " ←"); //TODO: unicode arrows
            else
                currentDisplay[i++].setPrefix(inactiveColor + line);
        }
    }

    //TODO: sounds
    public int unregister(boolean cancel)
    {
        registered = false;
        if (cancel)
        {
            if (player.getScoreboard() == scoreboard)
                player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
            player.getInventory().setHeldItemSlot(initialHotbarSlot);
            return -1;
        }

//        for (int i = 0; i < entries.size(); i++)
//        {
//            if (selectedItem != i)
//                entries.set(i, "");
//        }
        refreshDisplay();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (player.getScoreboard() == scoreboard && !registered)
                    player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
            }
        }.runTaskLater(plugin, 12L);

        player.getInventory().setHeldItemSlot(initialHotbarSlot);

        switch (selectedItem)
        {
            case 1:
                player.performCommand("book");
                break;
            case 2:
                player.performCommand("v hello");
                break;
            case 3:
                player.performCommand("v overhere");
                break;
            case 9:
                player.sendActionBar("You can also sneak to cancel out of the HotMenu.");
                break;
        }

        return selectedItem;
    }
}