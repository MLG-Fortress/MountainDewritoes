package me.robomwm.MountainDewritoes.Rewards;

import com.destroystokyo.paper.Title;
import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
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
        title.fadeOut(50);
        title.stay(60);
        switch (rewardType)
        {
            case XP_LEVELUP:
                title.title(ChatColor.YELLOW + "        LEVEL UP!");
                title.subtitle(ChatColor.WHITE + "                             Lv " + level);
                message.append(ChatColor.AQUA + ChatColor.BOLD.toString() + "REACHED LEVEL " + level + "!" + ChatColor.DARK_AQUA + " U g0t: ");

                //Increase health (if under max of 90), heal, feed
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                if (maxHealth < 180D)
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth + 2D);
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.removePotionEffect(PotionEffectType.POISON);
                player.removePotionEffect(PotionEffectType.WITHER);
                player.setFoodLevel(20);

                //Give moniez
                double money = Math.log(level) * 500;
                instance.getEconomy().depositPlayer(player, money);
                message.append(instance.getEconomy().format(money));

                player.sendMessage(message.toString());
                //Give random crate (needs to be updated as new crate series are added)
                executeCommand("newcrate " + String.valueOf(instance.r4nd0m(1, 5)) + " 1 " + player.getName());
                //Also give key if level is divisible by 5
                if (level % 5 == 0)
                    executeCommand("newkey 1 " + player.getName());
                player.playSound(player.getLocation(), "fortress.levelup", SoundCategory.PLAYERS, 3000000f, 1.0f);
        }
        instance.getTitleManager().sendTitle(player, 10, title.build());
    }

    public boolean executeCommand(String command)
    {
        return instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), command);
    }
}

