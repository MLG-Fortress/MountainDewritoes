package me.robomwm.MountainDewritoes.Commands;

import me.robomwm.BetterTPA.BetterTPA;
import me.robomwm.MountainDewritoes.LazyUtil;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 3/8/2017.
 *
 * @author RoboMWM
 */
public class WarpCommand implements CommandExecutor
{
    private MountainDewritoes instance;
    private BetterTPA betterTPA;
    private Map<String, Location> warps = new HashMap<>();
    private YamlConfiguration storedWarps;

    public WarpCommand(MountainDewritoes plugin)
    {
        instance = plugin;
        betterTPA = (BetterTPA)instance.getServer().getPluginManager().getPlugin("BetterTPA");
        boolean isNew = false;

        File storageFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!storageFile.exists())
        {
            try
            {
                storageFile.createNewFile();
                isNew = true;
            }
            catch (IOException e)
            {
                plugin.getLogger().severe("Could not create " + storageFile.getName());
                e.printStackTrace();
                return;
            }
        }
        storedWarps = YamlConfiguration.loadConfiguration(storageFile);

        for (String warpName : storedWarps.getKeys(false))
        {
            warps.put(warpName, (Location)storedWarps.get(warpName));
        }

        if (isNew)
        {
            addWarp("minigames", "spawn", -389.5D, 5D, -124.5D, 180.344f, -18.881f);
            addWarp("mall", "mall", 2.488, 5, -7.305, 0f, 0f);
            addWarp("prison", "prison", -970.5, 62, 1591.5, 270f, 6f);
            addWarp("jail", "minigames", -523.5D, 58.5D, -36.5D, 88.951f, 26.7f);
        }
    }

    private void saveWarps()
    {
        File storageFile = new File(instance.getDataFolder(), "warps.yml");
        try
        {
            storedWarps.save(storageFile);
        }
        catch (IOException e)
        {
            instance.getLogger().severe("Could not save " + storageFile.getName());
            e.printStackTrace();
            return;
        }
    }

    private void addWarp(String warp, String worldName, double x, double y, double z, float yaw, float pitch)
    {
        if (instance.getServer().getWorld(worldName) == null)
            return;
        Location location = new Location(instance.getServer().getWorld(worldName), x, y, z, yaw, pitch);
        warps.put(warp, location);
        storedWarps.set(warp, location);
        saveWarps();
    }

    private void addWarp(String warp, Location location)
    {
        warps.put(warp, location);
        storedWarps.set(warp, location);
        saveWarps();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        if (args.length < 1)
        {
            sendWarps(player);
            return true;
        }

        if (player.isOp() && args[0].equalsIgnoreCase("create"))
        {
            addWarp(args[1].toLowerCase(), player.getLocation());
            player.sendMessage("Warp created");
            return true;
        }

        String desiredWarp = args[0].toLowerCase();

        //Aliases
        switch (desiredWarp)
        {
            case "spawn":
                desiredWarp = "mall";
                break;
            case "lobby":
            case "hub":
            case "games":
            case "game":
            case "mini":
            case "minigame":
                desiredWarp = "minigames";
                break;
        }

        Location location = warps.get(desiredWarp);

        if (location == null)
            sendWarps(player);
        else
            betterTPA.teleportPlayer(player, desiredWarp, location, !(instance.isSafeWorld(player.getWorld()) && instance.isSafeWorld(location.getWorld())), null);
        return true;
    }

    private void sendWarps(Player player)
    {
        BookMeta bookMeta = LazyUtil.getBookMeta();
        List<BaseComponent> baseComponents = new ArrayList<>();
        LazyUtil.addLegacyText(ChatColor.DARK_BLUE + "Warps:\n\n", baseComponents);
        for (String warp : warps.keySet())
            baseComponents.add(LazyUtil.getClickableCommand("  " + warp + "     \n", "/warp " + warp));
        bookMeta.spigot().addPage(baseComponents.toArray(new BaseComponent[baseComponents.size()]));
        instance.getBookUtil().openBook(player, LazyUtil.getBook(bookMeta));
    }

    //old String method
//    private void sendWarps(Player player)
//    {
//        player.sendMessage("Warps:");
//        StringBuilder lazy = new StringBuilder(ChatColor.GOLD.toString());
//        for (String warp : warps.keySet())
//        {
//            lazy.append(warp);
//            lazy.append(", ");
//        }
//        lazy.setLength(lazy.length() - 2);
//        player.sendMessage(lazy.toString());
//    }
}