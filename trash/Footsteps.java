package me.robomwm.MountainDewritoes.Sounds;

import me.robomwm.MountainDewritoes.SimpleClansListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by RoboMWM on 10/10/2016.
 * Currently testing to see how badly this impacts performance
 */
public class Footsteps implements Listener
{
    World MALL = Bukkit.getWorld("mall");
    World SPAWN = Bukkit.getWorld("minigames");

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerFootstep(PlayerMoveEvent event)
    {
        //Don't care if player is just looking around
        if (event.getFrom().distanceSquared(event.getTo()) <= 0)
            return;

        Player player = event.getPlayer();
        if (player.isSneaking())
            return;

        //ignoredTODO: world checks

        final Material floorMaterial = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        //ignoredTODO: play according sound
        String soundToPlay = "fortress.stone.step";
        switch (floorMaterial)
        {
            case CARPET:
            case WOOL:
                soundToPlay = "fortress.cloth.step";
                break;
            case SNOW:
            case SNOW_BLOCK:
                soundToPlay = "fortress.snow.step";
                break;
            case WOOD:
            case LOG:
            case LOG_2:
            case FENCE:
            case FENCE_GATE:
            case ACACIA_FENCE:
            case DARK_OAK_FENCE:
                soundToPlay = "fortress.wood.step";
                break;
        }
        //etc. If not performance, this is going to be the reason why I'm not doing this

        final Location playerLocation = player.getLocation();
        final World playerWorld = player.getWorld();

        for (Player target : Bukkit.getOnlinePlayers())
        {
            if (target == player)
                return;
            if (target.getWorld() != playerWorld)
                return;
            if (target.getLocation().distanceSquared(playerLocation) > 256)
                return;
            //if (!SimpleClansListener.isInSameClan(player, target))
            //    return;
            target.playSound(playerLocation, soundToPlay, 1.0f, 1.0f);
        }


    }
}
