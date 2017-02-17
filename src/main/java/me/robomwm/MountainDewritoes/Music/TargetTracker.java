package me.robomwm.MountainDewritoes.Music;

import me.robomwm.MountainDewritoes.Events.MonsterTargetEvent;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2/13/2017.
 * @author RoboMWM
 * Currently in Music package since that's where we're using it, but I'll probably move it out... s0000n
 */
public class TargetTracker implements Listener
{
    MountainDewritoes instance;

    TargetTracker(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    Map<Player, Set<Monster>> targetedPlayers = new HashMap<>();

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

        if (!targetedPlayers.containsKey(player))
        {
            Set<Monster> targeters = new HashSet<>();
            targeters.add(monster);
            targetedPlayers.put(player, targeters);
        }
        else
            targetedPlayers.get(player).add(monster);

        //Call MonsterTargetEvent
        instance.getServer().getPluginManager().callEvent(new MonsterTargetEvent(monster, player));
    }

    public boolean isPlayerTargeted(Player player)
    {
        return howManyTargetingPlayer(player) > 0;
    }

    /**
     * Also updates the target list
     * @param player
     * @return how many monsters are trying to attack this player
     */
    public int howManyTargetingPlayer(Player player)
    {
        if (!targetedPlayers.containsKey(player))
            return 0;
        Set<Monster> nonTargeters = new HashSet<>();
        for (Monster monster : targetedPlayers.get(player))
        {
            if (monster.isDead() || monster.getTarget() != player)
                nonTargeters.add(monster);
        }
        targetedPlayers.get(player).removeAll(nonTargeters);
        return targetedPlayers.get(player).size();
    }
}
