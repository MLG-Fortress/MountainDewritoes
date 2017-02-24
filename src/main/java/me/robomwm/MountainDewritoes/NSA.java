package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Events.MonsterTargetPlayerEvent;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
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
 * Various trackers
 */
public class NSA implements Listener
{
    MountainDewritoes instance;

    NSA(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    Map<Player, Set<Creature>> targetedPlayers = new HashMap<>();

    @EventHandler
    void onPlayerTargeted(MonsterTargetPlayerEvent event)
    {
        Player player = event.getPlayer();
        Creature entity = event.getBadEntity();

        if (!targetedPlayers.containsKey(player))
        {
            Set<Creature> targeters = new HashSet<>();
            targeters.add(entity);
            targetedPlayers.put(player, targeters);
        }
        else
            targetedPlayers.get(player).add(entity);
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
        Set<Creature> nonTargeters = new HashSet<>();
        for (Creature entity : targetedPlayers.get(player))
        {
            if (!entity.isValid() || entity.isDead() || entity.getTarget() != player)
                nonTargeters.add(entity);
        }
        targetedPlayers.get(player).removeAll(nonTargeters);
        return targetedPlayers.get(player).size();
    }
}
