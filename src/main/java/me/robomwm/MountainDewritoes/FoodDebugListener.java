package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Commands.DebugCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * Server-side debug for dorito/energy eating (burp) diagnosis.
 * Toggle via /mdebug (global debug flag). Logs to console via DebugCommand.debug.
 * Added per https://github.com/MLG-Fortress/MountainDewritoes/issues/113#issuecomment-5404294353
 */
public class FoodDebugListener implements Listener {

    public FoodDebugListener(MountainDewritoes plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPreEat(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = event.getPlayer();
        // Mirror OldFood.java:32 gate but log always
        DebugCommand.debug("FoodDebug PreEat: player=" + p.getName()
                + " food=" + p.getFoodLevel()
                + " health=" + p.getHealth() + "/" + p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()
                + " item=" + (event.getItem() != null ? event.getItem().getType() : "null")
                + " action=" + event.getAction()
                + " cancelled=" + event.isCancelled()
                + " handRaised=" + p.isHandRaised());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        // Log at MONITOR so we see final cancelled state
        DebugCommand.debug("FoodDebug FoodLevelChange: player=" + p.getName()
                + " " + p.getFoodLevel() + "->" + event.getFoodLevel()
                + " cancelled=" + event.isCancelled()
                + " handRaised=" + p.isHandRaised()
                + " stack=" + Thread.currentThread().getStackTrace()[2] + "#" + Thread.currentThread().getStackTrace()[3]);
        // Also log stack snippet for ATPgeneration vs OldFood vs nutrition
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        StringBuilder caller = new StringBuilder();
        for (int i = 3; i < Math.min(st.length, 10); i++) {
            String cn = st[i].getClassName();
            if (cn.contains("ArmorAugmentation") || cn.contains("OldFood") || cn.contains("FoodDebugListener")) {
                caller.append(cn).append("#").append(st[i].getMethodName()).append(":").append(st[i].getLineNumber()).append(" ");
            }
        }
        if (caller.length() > 0) DebugCommand.debug("FoodDebug caller: " + caller);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player p = event.getPlayer();
        DebugCommand.debug("FoodDebug PlayerItemConsume: player=" + p.getName()
                + " item=" + event.getItem().getType()
                + " food=" + p.getFoodLevel()
                + " health=" + p.getHealth()
                + " cancelled=" + event.isCancelled()
                + " replacement=" + event.getItem() // legacy getItem copy
                + " handRaisedBefore=" + p.isHandRaised());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.EATING
                && event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) return;
        Player p = (Player) event.getEntity();
        DebugCommand.debug("FoodDebug RegainHealth: player=" + p.getName()
                + " reason=" + event.getRegainReason()
                + " amount=" + event.getAmount()
                + " cancelled=" + event.isCancelled()
                + " food=" + p.getFoodLevel()
                + " health=" + p.getHealth());
    }
}
