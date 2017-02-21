package me.robomwm.MountainDewritoes.Events;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2/13/2017.
 * @author RoboMWM
 * Fires MonsterTargetPlayerEvent, as well as storing a set of monsters targeting a player in "MD_TRACKING"
 */
public class ReverseOsmosis implements Listener
{
    MountainDewritoes instance;

    public ReverseOsmosis(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    Map<Player, Set<Monster>> targetedPlayers = new HashMap<>();

    /**
     * @see MonsterTargetPlayerEvent
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerTargeted(EntityTargetLivingEntityEvent event)
    {
        if (event.getTarget() == null) //forgot/lost the target
            return;
        if (event.getTarget().getType() != EntityType.PLAYER)
            return;
        if (!(event.getEntity() instanceof Monster))
            return;

        Player player = (Player)event.getTarget();
        Monster monster = (Monster)event.getEntity();

        //Call MonsterTargetPlayerEvent
        instance.getServer().getPluginManager().callEvent(new MonsterTargetPlayerEvent(monster, player));
    }

    /**
     * @see JukeboxInteractEvent
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerInteractJukebox(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Material disc = event.getMaterial();
        Block block = event.getClickedBlock();

        if (player.isSneaking())
            return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX)
            return;

        Jukebox jukebox = (Jukebox)block.getState();

        instance.getServer().getPluginManager().callEvent(new JukeboxInteractEvent(player, jukebox, disc));
    }


}
