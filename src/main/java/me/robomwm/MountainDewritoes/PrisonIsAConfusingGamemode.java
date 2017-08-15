package me.robomwm.MountainDewritoes;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 7/15/2017.
 *
 * Is this mode for bored people?
 *
 * @author RoboMWM
 */
public class PrisonIsAConfusingGamemode implements Listener
{
    JavaPlugin instance;
    World prisonWorld;
    Map<String, Location> mines = new HashMap<>();

    PrisonIsAConfusingGamemode(JavaPlugin plugin)
    {
        prisonWorld = plugin.getServer().getWorld("prison");
        if (prisonWorld == null)
            return;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        instance = plugin;
        mines.put("A", new Location(prisonWorld, -970D, 14D, 1295D));
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getWorld() != prisonWorld)
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event)
    {
        if (event.getBlock().getWorld() != prisonWorld)
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        int y = event.getBlock().getLocation().getBlockY();

        if (y > 18 || y < 4)
        {
            event.setCancelled(true);
            return;
        }


        //Too many, lol

//        switch (event.getBlock().getType())
//        {
//            case STONE:
//            case COBBLESTONE:
//            case MOSSY_COBBLESTONE:
//            case COAL_ORE:
//            case COAL_BLOCK:
//            case IRON_ORE:
//            case IRON_BLOCK:
//            case GOLD_ORE:
//            case GOLD_BLOCK:
//            case REDSTONE_ORE:
//            case GLOWING_REDSTONE_ORE:
//            case REDSTONE_BLOCK:
//            case DIAMOND_ORE:
//            case DIAMOND_BLOCK:
//            case EMERALD_ORE:
//            case EMERALD_BLOCK:
//            case CLAY:
//            case SANDSTONE:
//            case RED_SANDSTONE:
//            case SMOOTH_BRICK:
//            case SOUL_SAND:
//            case BRICK:
//            case ENDER_STONE:
//            case END_BRICKS:
//                return;
//        }

//        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onSignClick(PlayerInteractEvent event)
    {
        if (event.getPlayer().getWorld() != prisonWorld)
            return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.WALL_SIGN)
            return;
        Sign sine = (Sign)event.getClickedBlock().getState();
        if (!sine.getLine(0).contains("[Prison Mine]"))
            return;

        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();

        if (!isTicket(itemStack) || !itemStack.getItemMeta().getLore().get(0).substring(21).equals(sine.getLine(1)))
        {
            event.getPlayer().sendMessage(ChatColor.RED + "You must be holding a corresponding ticket for admittance to this mine.");
            return;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                event.getPlayer().teleport(mines.get(sine.getLine(1)));
            }
        }.runTask(instance);
    }

    private boolean isTicket(ItemStack itemStack)
    {
        if (itemStack == null || !itemStack.hasItemMeta())
            return false;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (!itemMeta.hasLore())
            return false;

        if (!itemMeta.getLore().get(0).startsWith(ChatColor.GREEN + "Admittance to mine "))
            return false;

        String mine = itemMeta.getLore().get(0).substring(21);

        if (mine.isEmpty() || !mines.containsKey(mine))
            return false;
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    private void createTicketLazyCommand(PlayerCommandPreprocessEvent event)
    {
        if (!event.getPlayer().hasPermission("mlgstaff"))
            return;
        String[] message = event.getMessage().toLowerCase().split(" ");
        if (message.length < 2 || !message[0].equals("/createprisonticket"))
            return;
        if (event.getPlayer().getInventory().getItemInMainHand() == null)
            return;
        event.getPlayer().getInventory().setItemInMainHand(createTicket(event.getPlayer().getInventory().getItemInMainHand(), message[1]));
        event.setCancelled(true);
    }

    private ItemStack createTicket(ItemStack itemStack, String mine)
    {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Admittance to mine " + mine);
        lore.add(ChatColor.GREEN + "in the prison WORLD");
        lore.add(ChatColor.GRAY + "Use it at " + ChatColor.GOLD + "/warp prison");
        itemStack.getItemMeta().setLore(lore);
        itemStack.getItemMeta().setDisplayName("Prison mine ticket (mine " + mine + ")");
        return itemStack;
    }
}
