package me.robomwm.MountainDewritoes.Events;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2/13/2017.
 * @author RoboMWM
 * Fires MonsterTargetPlayerEvent, as well as storing a set of monsters targeting a player in "MD_TRACKING"
 */
public class ReverseOsmosis implements Listener
{
    private MountainDewritoes instance;

    private Map<Player, Double> oldBalances = new HashMap<>();
    private Map<Player, World> changedWorld = new HashMap<>();
    private Map<Player, Location> playersThatMoved = new HashMap<>();

    public ReverseOsmosis(MountainDewritoes plugin)
    {
        instance = plugin;
        instance.registerListener(this);

        //Tracks changes to player's balance
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : plugin.getServer().getOnlinePlayers())
                {
                    transactionEvent(player, plugin.getEconomy());
                }
                movedEvent();
            }
        }.runTaskTimer(plugin, 1L, 20L);
    }

    private void callEvent(Event event)
    {
        instance.getServer().getPluginManager().callEvent(event);
    }

    private void transactionEvent(Player player, Economy economy)
    {
        if (!oldBalances.containsKey(player))
        {
            oldBalances.put(player, economy.getBalance(player));
            return;
        }

        double oldBalance = oldBalances.get(player);
        double balance = economy.getBalance(player);
        double difference = balance - oldBalance;
        if (difference != 0)
        {
            callEvent(new TransactionEvent(player, difference, economy));
            oldBalances.put(player, balance);
        }
    }

    private void movedEvent()
    {
        Iterator<Map.Entry<Player, Location>> movedPlayersIterator = playersThatMoved.entrySet().iterator();
        while (movedPlayersIterator.hasNext())
        {
            Map.Entry<Player, Location> entry = movedPlayersIterator.next();
            callEvent(new ScheduledPlayerMovedEvent(entry.getKey(), entry.getValue()));
            movedPlayersIterator.remove();
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        oldBalances.remove(event.getPlayer());
        changedWorld.remove(event.getPlayer());
        playersThatMoved.remove(event.getPlayer());
    }

    /**
     * @see MonsterTargetPlayerEvent
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerTargeted(EntityTargetLivingEntityEvent event)
    {
        if (event.getTarget() == null) //forgot/lost the target
            return;
        if (event.getTarget().getType() != EntityType.PLAYER) //not targeting a player
            return;
        if (!isLivingMonster(event.getEntity())) //not a monster
            return;

        Player player = (Player)event.getTarget();
        Creature entity = (Creature)event.getEntity();

        //Call MonsterTargetPlayerEvent
        instance.getServer().getPluginManager().callEvent(new MonsterTargetPlayerEvent(entity, player));
    }

    private boolean isLivingMonster(Entity entity)
    {
        if (!(entity instanceof Creature))
            return false;

        if (entity instanceof Monster)
            return true;


        EntityType type = entity.getType();
        switch(type)
        {
            //case GHAST: //Not a creature, therefore does not have getTarget()
            //case MAGMA_CUBE:
            //case SLIME:
            case SHULKER:
            case POLAR_BEAR:
                return true;
            case RABBIT:
                Rabbit rabbit = (Rabbit) entity;
                if (rabbit.getRabbitType() == Rabbit.Type.THE_KILLER_BUNNY)
                    return true;
            default:
                return false;
        }
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

    @EventHandler(priority = EventPriority.MONITOR)
    private void onWorldChange(PlayerChangedWorldEvent event)
    {
        changedWorld.put(event.getPlayer(), event.getPlayer().getWorld());
    }
    @EventHandler(priority = EventPriority.MONITOR)
    private void onJoin(PlayerJoinEvent event)
    {
        changedWorld.put(event.getPlayer(), event.getPlayer().getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerMovedAfterChangingWorlds(PlayerMoveEvent event)
    {
        if (changedWorld.remove(event.getPlayer()) == event.getPlayer().getWorld())
            instance.getServer().getPluginManager().callEvent(new PlayerLoadedWorldEvent(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerMove(PlayerMoveEvent event)
    {
        playersThatMoved.putIfAbsent(event.getPlayer(), event.getFrom());
    }

}
