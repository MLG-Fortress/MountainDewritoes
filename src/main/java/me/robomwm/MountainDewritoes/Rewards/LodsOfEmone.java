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
        StringBuilder message = new StringBuilder();
        Title.Builder title = new Title.Builder();
        title.fadeIn(5);
        title.fadeOut(60);
        title.stay(60);
        switch (rewardType)
        {
            case XP_LEVELUP:
                title.title(ChatColor.YELLOW + "                LEVEL UP!");
                title.subtitle(ChatColor.WHITE + "                  lv" + level);
                message.append(ChatColor.AQUA + ChatColor.BOLD.toString() + "Reached level " + level + "!" + ChatColor.DARK_AQUA + " U g0t:\n");
                //Heal player
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.removePotionEffect(PotionEffectType.POISON);
                player.removePotionEffect(PotionEffectType.WITHER);
                //Give moniez
                double money = Math.log(level) * 500;
                instance.getEconomy().depositPlayer(player, money);
                message.append(instance.getEconomy().format(money) + "\n");
                player.sendMessage(message.toString());
                //Give random crate (needs to be updated as new crate series are added)
                executeCommand("newcrate " + instance.r4nd0m(1, 5) + "1 " + player.getName());
                //Also give key if level is divisible by 5
                if (level % 5 == 0)
                    executeCommand("newkey 1 " + player.getName());
                //TODO: sound

        }
        instance.getTitleManager().addUsingTitle(player, 10, title.build());
    }

    public boolean executeCommand(String command)
    {
        return instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), command);
    }
}

