package me.robomwm.MountainDewritoes.arena;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

// Idk when I even created this, seems it doesn't even have the intellij signature so must've been something I did while playing with another IDE like VS Code.

public class Arena implements Listener
{
    public Plugin plugin;

    public Arena(Plugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onDeath(PlayerDeathEvent event)
    {
        if (event.getEntity().getKiller() == null)
            return;

        Player loser = event.getEntity();
        Player attacker = loser.getKiller();

        int attackerXP = attacker.getLevel();
        int loserXP = loser.getLevel();

        double rewardMultiplier = sigmoid(loserXP - attackerXP);
        double rewardBase = getExpAtLevel(attackerXP) / (attacker.getLevel() * 2 + 1);
        int reward = (int)(rewardMultiplier * rewardBase);

        ExperienceOrb orb = (ExperienceOrb)attacker.getWorld().spawnEntity(attacker.getLocation(), EntityType.EXPERIENCE_ORB);
        orb.setExperience(reward);
    }

    private static double sigmoid(double x)
    {
        return 4 / (1 + (Math.exp(-x) / 5));
    }

    // From https://github.com/drtshock/Essentials/blob/2.x/Essentials/src/com/earth2me/essentials/craftbukkit/SetExpFix.java
    public static int getExpAtLevel(final int level) {
        if (level <= 15) {
            return (2 * level) + 7;
        }
        if (level <= 30) {
            return (5 * level) - 38;
        }
        return (9 * level) - 158;

    }
}
