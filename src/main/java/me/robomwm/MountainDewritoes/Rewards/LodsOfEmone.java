package me.robomwm.MountainDewritoes.Rewards;

import com.destroystokyo.paper.Title;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Created on 8/19/2017.
 *
 * @author RoboMWM
 */
public class LodsOfEmone
{
    MountainDewritoes instance;

    public LodsOfEmone(MountainDewritoes plugin)
    {
        instance = plugin;
        new LevelingProgression(plugin, this);
    }

    public void rewardPlayer(Player player, int level, RewardType rewardType)
    {
        Title.Builder title = new Title.Builder();
        title.fadeIn(5);
        title.fadeOut(80);
        title.stay(60);
        switch (rewardType)
        {
            case XP_LEVELUP:
                title.title(ChatColor.YELLOW + "LEVEL UP!");
                title.subtitle(ChatColor.AQUA + "Reached level " + level + "!");
                //Heal player
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.removePotionEffect(PotionEffectType.POISON);
                player.removePotionEffect(PotionEffectType.WITHER);
                //Give moniez
                instance.getEconomy().depositPlayer(player, Math.log(level) * 500);
                //Give key
                instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "newkey 1 " + player.getName());
                //TODO: sound

        }
        instance.getTitleManager().addUsingTitle(player, 10, title.build());
    }
}

