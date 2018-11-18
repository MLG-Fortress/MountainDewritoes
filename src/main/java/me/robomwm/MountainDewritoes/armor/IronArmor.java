package me.robomwm.MountainDewritoes.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 1/30/2018.
 *
 * @author RoboMWM
 */
public class IronArmor implements Listener
{
    private JavaPlugin instance;
    private ArmorAugmentation armorAugmentation;
    private final Map<Player, BukkitRunnable> floaters = new HashMap<>();
    private final Map<Player, BukkitRunnable> flyers = new HashMap<>();

    IronArmor(JavaPlugin plugin, ArmorAugmentation armorAugmentation)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        instance = plugin;
        this.armorAugmentation = armorAugmentation;
    }

    @EventHandler(ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (floaters.containsKey(player))
            floaters.remove(player).cancel();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event)
    {
        Player player = event.getPlayer();

        if (floaters.containsKey(player))
            floaters.remove(player).cancel();

        if (!event.isSneaking() || player.isOnGround())
            return;
        if (!armorAugmentation.isEquipped(player, Material.IRON_BOOTS))
            return;

        BukkitRunnable runnable = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!armorAugmentation.isEquipped(player, Material.IRON_BOOTS) || player.isOnGround())
                {
                    cancel();
                    return;
                }
                final int velocity = 255 - (4 - (player.getFoodLevel() / 5));
//                int velocity = 1;

//                if (player.getFoodLevel() <= 1)
//                {
//                    velocity = 254;
//                    player.setFoodLevel(1);
//                }
//                else
//                    player.setFoodLevel(player.getFoodLevel() - 1);

                player.removePotionEffect(PotionEffectType.LEVITATION);
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 30, velocity, true, false));
                player.sendActionBar(Integer.toString(velocity));
            }

            @Override
            public synchronized void cancel() throws IllegalStateException
            {
                super.cancel();
                player.removePotionEffect(PotionEffectType.LEVITATION);
                floaters.remove(player);
//                try
//                {
//                    if (ProtocolSupportAPI.getProtocolVersion(player).isBefore(ProtocolVersion.MINECRAFT_1_9))
//                        return;
//                }
//                catch (Throwable ignored){}
//                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 10, 250, true, false));
            }
        };

        floaters.put(player, runnable);
        runnable.runTaskTimer(instance, 0L, 10L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSprint(PlayerToggleSprintEvent event)
    {
        Player player = event.getPlayer();

        if (flyers.containsKey(player))
            flyers.remove(player).cancel();

        if (!armorAugmentation.usePowerAbility(event, Material.IRON_LEGGINGS, 1))
            return;

        new BukkitRunnable()
        {
            int time = 0;
            @Override
            public void run()
            {
                if (!armorAugmentation.isEquipped(player, Material.IRON_LEGGINGS)
                || !player.isSprinting() || player.getFoodLevel() == 0)
                {
                    cancel();
                    return;
                }

                player.setVelocity(player.getLocation().getDirection().multiply(0.3));
                if (++time % 5 == 0)
                    player.setFoodLevel(player.getFoodLevel() - 1);
            }
        }.runTaskTimer(armorAugmentation.getPlugin(), 1L, 1L);
    }
}

//Compensate for falling velocity //No longer needed since we reduced power cost.
//                    int velocity = (int)(-player.getVelocity().getY() * 15);
//                    if (velocity < 1)
//                        velocity = 1;
//                    else if (velocity > 100)
//                        velocity = 100;