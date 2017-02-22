package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Events.MonsterTargetPlayerEvent;
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

    Map<Player, Set<Monster>> targetedPlayers = new HashMap<>();

    @EventHandler
    void onPlayerTargeted(MonsterTargetPlayerEvent event)
    {
        Player player = event.getPlayer();
        Monster monster = event.getMonster();

        if (!targetedPlayers.containsKey(player))
        {
            Set<Monster> targeters = new HashSet<>();
            targeters.add(monster);
            targetedPlayers.put(player, targeters);
        }
        else
            targetedPlayers.get(player).add(monster);
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
            if (!monster.isValid() || monster.isDead() || monster.getTarget() != player)
                nonTargeters.add(monster);
        }
        targetedPlayers.get(player).removeAll(nonTargeters);
        return targetedPlayers.get(player).size();
    }
}
