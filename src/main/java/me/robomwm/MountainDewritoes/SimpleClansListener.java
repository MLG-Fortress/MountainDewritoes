package me.robomwm.MountainDewritoes;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import me.robomwm.BetterTPA.BetterTPA;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Created by RoboMWM on 2/13/2016.
 * Adds prefix based on clan tag
 * "Handles" clan home teleportation
 * Handles friendly-fire
 */
public class SimpleClansListener implements Listener
{
    private Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
    ClanManager clanManager;
    BukkitScheduler scheduler = Bukkit.getScheduler();
    private MountainDewritoes instance;
    BetterTPA betterTPA;

    public SimpleClansListener(MountainDewritoes mountainDewritoes, ClanManager clanManager)
    {
        this.clanManager = clanManager;
        instance = mountainDewritoes;
        betterTPA = (BetterTPA)instance.getServer().getPluginManager().getPlugin("BetterTPA");
        instance.getServer().dispatchCommand(instance.getServer().getConsoleSender(), "clan globalff allow");
        instance.registerListener(this);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : instance.getServer().getOnlinePlayers())
                    setClanPrefix(player);
            }
        }.runTaskTimer(instance, 6000L, 1200L);
    }

    //Set colors and prefix onJoin
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        final String colorCode = getColorCode(event.getPlayer());
        //Set colored display name
        setDisplayName(event.getPlayer(), colorCode);
        setClanPrefix(event.getPlayer());
    }

    //Set colors and prefix if player changes clans //Now covered by the periodic check task
//    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
//    {
//        String command = event.getMessage().toLowerCase();
//        final Player player = event.getPlayer();
//        //I'm 400,000% sure there's a better way to do this
//        if (command.startsWith("/clan ") || command.startsWith("/accept") || command.startsWith("/f ") || command.startsWith("/nick "))
//        {
//            scheduler.scheduleSyncDelayedTask(instance, new Runnable()
//            {
//                public void run()
//                {
//                    setClanPrefix(player);
//                }
//            }, 10L);
//        }
//    }

    //Get a randomized, consistent color code for player
    public String getColorCode(Player player)
    {
        //Get hash code of player's UUID
        int colorCode = player.getUniqueId().hashCode();
        //Ensure number is positive
        colorCode = Math.abs(colorCode);

        //Will make configurable, hence this
        String[] acceptableColors = "2,3,4,5,6,9,a,b,c,d,e".split(",");
        //Divide hash code by length of acceptableColors, and use remainder
        //to determine which index to use (like a hashtable/map/whatever)
        colorCode = (colorCode % acceptableColors.length);
        String stringColorCode = acceptableColors[colorCode];

        return stringColorCode;
    }

    //Delayed set playerListName (Primarily for onJoin, since Essentials sets displayName late)
    //Automatically adds appropriate spacing
    public void setListName(final Player player, final String prefix)
    {
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                if (!player.isOnline())
                    return;
                if (prefix.isEmpty())
                    player.setPlayerListName(player.getDisplayName());
                else
                    player.setPlayerListName(prefix + " " + player.getDisplayName());
            }
        }, 20L);
    }

    //Delayed setDisplayName
    //Now kinda useless, and not delayed.
    public void setDisplayName(final Player player1, final String colorCode)
    {
        if (!player1.hasPlayedBefore() || !ChatColor.stripColor(player1.getDisplayName()).contains(player1.getName()))
        {
            scheduler.scheduleSyncDelayedTask(instance, new Runnable()
            {
                public void run()
                {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "enick " + player1.getName() + " &" + colorCode + player1.getName());
                }
            });
        }
        //[16:34:22] RoboMWM: Does EssX constantly set or change the displayName? I'm able to change the displayName (I use it to setPlayerListName) but chat messages revert the color to white
        //[16:50:46] RoboMWM: Alright, so either I hook into Essentials and grab the nickname from there or implement my own sort of /nick

        //Don't alter if player name is already colored
//        scheduler.scheduleSyncDelayedTask(instance, new Runnable() {
//            public void run() {
//                if (!player1.isOnline())
//                    return;
//                if (player1.getDisplayName().startsWith(player1.getName()))
//                {
//                    player1.setDisplayName("\u00A7" + colorCode + player1.getName());
//                    System.out.println("display name of " + player1.getName() + " is: " + player1.getDisplayName());
//                }
//
//            }
//        }, 20L); //Ensure Essentials sets displayName before we set displayName (Essentials sets it later)
    }

    //Sets a player's appropriate chat color and prefix.
    public void setClanPrefix(final Player player)
    {
        final String colorCode = getColorCode(player);

        ClanPlayer clanPlayer = clanManager.getClanPlayer(player);
        if (clanPlayer == null)
        {
            //If not part of a clan, set colored name and do no more
            setListName(player, "");
            return;
        }

        final String tag = ("\u00A77" + clanPlayer.getClan().getColorTag());

        //Feature: set prefix in tablist
        //compatible with other prefix/suffix plugins since we just set PlayerListName
        setListName(player, tag);

        //Feature: set prefix in nameplate
        scheduler.scheduleSyncDelayedTask(instance, new Runnable()
        {
            public void run()
            {
                Team team = sb.getTeam(player.getName());
                if (team == null || !player.isOnline())
                    return;
                //Feature: color nameplate name
                //Get displayName color (player can change color via /nick)
                //Temporarily removed since some colors are hard to see
//                String color = "";
//                char[] charName = player.getDisplayName().toCharArray();
//                if (charName[2] == '\u00A7') //If they have a staff prefix
//                    color = String.valueOf(charName[3]);
//                else
//                    color = String.valueOf(charName[1]);

                team.setPrefix(tag + " \u00A7f");
            }
        }, 40L); //Ensure healthbar made the team
    }

    /**
     * Teleports player to clan home via our warmup methods and whatnot
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onWantToGoToClanHome(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        if (message.equals("/clan home") || message.equals("/home"))
        {
            event.setCancelled(true);
            Clan clan = clanManager.getClanByPlayerUniqueId(event.getPlayer().getUniqueId());
            if (clan == null)
            {
                player.sendMessage(ChatColor.RED + "You are not in a /clan");
                return;
            }
            if (clan.getHomeLocation() == null)
            {
                player.sendMessage(ChatColor.RED + "Your clan did not /sethome.");
                return;
            }
            betterTPA.teleportPlayer(player, "da " + clan.getName() + " homebase", clan.getHomeLocation(), true, null);
        }
    }

    /**
     * Extra clan commands
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void clanHelpOrWe(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        if (message.equals("/clan") || message.equals("/f"))
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    player.performCommand("/help clan");
                }
            }.runTaskLater(instance, 1L);
        }
    }

    /**
     * Player can join a clan if they're clan-less
     * TODO: Don't allow a player to rejoin a clan they were kicked from for w/e reason
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onWantToJoinAClan(PlayerCommandPreprocessEvent event)
    {
        String invalid = "/clan join <clan tag> - joins a clan.";
        String message = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        if (message.startsWith("/clan join"))
        {
            Clan playerclan = clanManager.getClanByPlayerUniqueId(player.getUniqueId());
            if (playerclan != null)
                return;

            event.setCancelled(true);
            String[] args = message.split(" ");

            if (args.length < 3)
            {
                player.sendMessage(invalid);
                return;
            }

            Clan clan = clanManager.getClan(args[2]);
            if (clan != null)
            {
                clan.addPlayerToClan(clanManager.getClanPlayer(player));
            }
            else
            {
                player.sendMessage("Invalid clan tag. See " + ChatColor.GOLD + "/clan list");
                player.performCommand("clan list");
            }

        }
    }

    //Projectiles can pass through allies
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerAboutToGetHit(ProjectileCollideEvent event)
    {
        if (!instance.isSurvivalWorld(event.getEntity().getWorld()))
            return;
        if (event.getCollidedWith().getType() != EntityType.PLAYER)
            return;
        if (!(event.getEntity().getShooter() instanceof Player))
            return;

        Player damagee = (Player)event.getCollidedWith();
        Player damager = (Player)event.getEntity().getShooter();

        if (isInSameClan(damagee, damager))
            event.setCancelled(true);
    }

    //No friendly fire
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerDamage(EntityDamageByEntityEvent event)
    {
        if (!instance.isSurvivalWorld(event.getEntity().getWorld()))
            return;
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;
        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        Player damagee = (Player)event.getEntity();
        Player damager = (Player)event.getDamager();

        if (isInSameClan(damagee, damager))
            event.setCancelled(true);
    }

    private boolean isInSameClan(Player player, Player target)
    {
        ClanPlayer clanPlayer = clanManager.getClanPlayer(player);
        ClanPlayer clanTarget = clanManager.getClanPlayer(target);
        if (clanPlayer == null || clanTarget == null)
            return false;
        return clanPlayer.getClan() == clanTarget.getClan();
    }

}
