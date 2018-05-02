package me.robomwm.MountainDewritoes.armor;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolType;
import protocolsupport.api.ProtocolVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 1/30/2018.
 *
 * Leggings: "Long Leap." Leap in the direction you are looking.
 * Boots: "Magnetic Boots." Hover/fly for a short duration. Consumes around three doritos a second.
 *
 * @author RoboMWM
 */
public class IronArmor implements Listener
{
    private JavaPlugin instance;
    private ArmorAugmentation armorAugmentation;
    private final Map<Player, BukkitRunnable> floaters = new HashMap<>();

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

        if (player.getFoodLevel() > 0)
        {
            BukkitRunnable runnable = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (player.getFoodLevel() <= 0 || !armorAugmentation.isEquipped(player, Material.IRON_BOOTS))
                    {
                        cancel();
                        return;
                    }
                    int velocity = 1;
                    //Compensate for falling velocity //No longer needed since we reduced power cost.
//                    int velocity = (int)(-player.getVelocity().getY() * 15);
//                    if (velocity < 1)
//                        velocity = 1;
//                    else if (velocity > 100)
//                        velocity = 100;
                    player.removePotionEffect(PotionEffectType.LEVITATION);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, velocity, true, false));
                    player.setFoodLevel(player.getFoodLevel() - 1);
                }

                @Override
                public synchronized void cancel() throws IllegalStateException
                {
                    super.cancel();
                    player.removePotionEffect(PotionEffectType.LEVITATION);
                    floaters.remove(player);
                    try
                    {
                        if (ProtocolSupportAPI.getProtocolVersion(player) != ProtocolVersion.getLatest(ProtocolType.PC))
                            return;
                    }
                    catch (Throwable ignored){}
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 10, 250, true, false));

                }
            };
            floaters.put(player, runnable);
            runnable.runTaskTimer(instance, 0L, 7L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onAttack(EntityDamageByEntityEvent event)
    {
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;
        Player player = (Player)event.getDamager();

        if (!player.isSprinting() || !armorAugmentation.isEquipped(player, Material.IRON_LEGGINGS))
            return;

        event.getEntity().setVelocity(event.getEntity().getLocation().toVector().subtract(player.getLocation().toVector()).normalize().setY(0.3));
    }
}
