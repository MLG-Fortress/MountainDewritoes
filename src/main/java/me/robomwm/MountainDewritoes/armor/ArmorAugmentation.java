package me.robomwm.MountainDewritoes.armor;

import me.robomwm.MountainDewritoes.MountainDewritoes;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 1/3/2018.
 *
 * Handles the armor abilities, controls energy bar
 *
 * @author RoboMWM
 */
public class ArmorAugmentation implements Listener
{
    private MountainDewritoes plugin;

    public ArmorAugmentation(MountainDewritoes plugin)
    {
        this.plugin = plugin;
        //plugin.getCustomItemRecipes().removeRecipe(new HashSet<>(Arrays.asList(Material.BARRIER, Material.BEDROCK))); //todo fill
        new GoldArmor(this.plugin, this);
        new IronArmor(this.plugin, this);
        new DiamondArmor(this.plugin, this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        new OldFood(this.plugin);
        ATPgeneration();
    }

    public MountainDewritoes getPlugin()
    {
        return plugin;
    }

    public boolean isEquipped(Player player, Material armorToMatch)
    {
        ItemStack equippedArmor = null;
        switch (armorToMatch)
        {
            case GOLD_LEGGINGS:
            case IRON_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
                equippedArmor = player.getInventory().getLeggings();
                break;
            case GOLD_BOOTS:
            case IRON_BOOTS:
            case DIAMOND_BOOTS:
            case CHAINMAIL_BOOTS:
                equippedArmor = player.getInventory().getBoots();
                break;
        }
        return equippedArmor != null && equippedArmor.getType() == armorToMatch;
    }

    //Used by sprint ability usually
    public boolean usePowerAbility(PlayerToggleSprintEvent event, Material leggings, int power)
    {
        Player player = event.getPlayer();
        if (!event.isSprinting())
            return false;
        if (!this.isEquipped(player, leggings))
            return false;
        if (player.getFoodLevel() < power)
        {
            player.sendActionBar("Insufficient power, requires " + power + " doritos.");
            return false;
        }
        player.setFoodLevel(player.getFoodLevel() - power);
        return true;
    }

    public int usePowerAbility(PlayerToggleSprintEvent event, Material leggings)
    {
        Player player = event.getPlayer();
        if (!event.isSprinting())
            return 0;
        if (!this.isEquipped(player, leggings))
            return 0;
        final int foodlevel = player.getFoodLevel();
        player.setFoodLevel(0);
        return foodlevel;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPlayerFallDamageWearingLongFallBoots(EntityDamageEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        LivingEntity entity = (LivingEntity)event.getEntity();

        if (entity.getEquipment() == null || entity.getEquipment().getBoots() == null)
            return;

        switch(entity.getEquipment().getBoots().getType())
        {
            case GOLD_BOOTS:
            case IRON_BOOTS:
            case DIAMOND_BOOTS:
                entity.getWorld().playSound(entity.getLocation(), "fortress.longfallboots", 1.0f, 1.0f);
                event.setCancelled(true);
        }
    }

    //Misc. gameplay changes to accomodate

    //There's no such thing as starving
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onHungerDamage(EntityDamageEvent event)
    {
        if (event.getCause() == EntityDamageEvent.DamageCause.STARVATION)
            event.setCancelled(true);
    }

    //Since when does having a full stomach make your wounds heal faster?
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onStomachHeal(EntityRegainHealthEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED)
            event.setCancelled(true);
    }

    //That's ~~an energy~~ power bar, not a hunger bar.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onGettingHungry(FoodLevelChangeEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onSprint(PlayerToggleSprintEvent event)
    {
        Player player = event.getPlayer();
        if (plugin.isNoModifyWorld(player.getWorld()))
            return;
        if (event.isSprinting() && player.getFoodLevel() > 1)
            player.setFoodLevel(player.getFoodLevel() - 1);
    }

    //Refill energy bar gradually, unless sprinting
    //iron leggings are exempt
    private void ATPgeneration()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : plugin.getServer().getOnlinePlayers())
                {
                    if (player.getFoodLevel() >= 20 || plugin.isNoModifyWorld(player.getWorld()))
                        continue;
                    player.setFoodLevel(player.getFoodLevel() + 1);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    //Cancel minute falling damage, do goomba stomp
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onHardlyAnyFalling(EntityDamageEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL || event.getEntityType() != EntityType.PLAYER)
            return;
        if (event.getDamage() < 5.0)
            event.setCancelled(true);
//        //TODO: goomba stomp
//        Player player = (Player)event.getEntity();
//        Collection<LivingEntity> entities = player.getLocation().getNearbyLivingEntities(0.5, 0.5, 0.5);
//        if (entities.size() == 0)
//            return;
//        FishHook hook = (FishHook)player.getWorld().spawnEntity(player.getLocation(), EntityType.FISHING_HOOK);
//        //TODO: possible to make invisible? Yes? No?
//        hook.setShooter(player);
//        Vector vector = new Vector(0, -4, 0);
//        for (LivingEntity entity : entities)
//        {
//            entity.damage(20, hook); //TODO: damage resist for wearing an armored hat??
//            entity.setVelocity(vector);
//            if (entity.getType() == EntityType.PLAYER)
//                ((Player)entity).sendTitle(ChatColor.RED + "GOOMBA STOMPED!", "", 0, 40, 20);
//            //TODO: tag entity, monitor deathEvent to alter death message
//        }
//        player.getWorld().playSound(player.getLocation(), "fortress.goombastoped", SoundCategory.PLAYERS, 1.0f, 1.0f);
//        vector.setY(0.5);
//        player.setVelocity(player.getVelocity().add(vector));
//        player.sendMessage("Goomba Stomped!");
    }

}

//trash
//Sprinting takes energy
//    private Map<Player, Long> sprinters = new HashMap<>();
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
//    private void onSprint(PlayerToggleSprintEvent event)
//    {
//        Player player = event.getPlayer();
//
//        if (!event.isSprinting())
//        {
//            sprinters.remove(player);
//            player.setSaturation(20f);
//            return;
//        }
//        if (player.getFoodLevel() < 1 || plugin.isNoModifyWorld(player.getWorld()))
//            return;
//
//        final long time = System.currentTimeMillis();
//        sprinters.put(player, time);
//
//        player.setFoodLevel(player.getFoodLevel() - 1);
//        player.setSaturation(0f);
//
//        new BukkitRunnable()
//        {
//
//            @Override
//            public void run()
//            {
//                if (sprinters.containsKey(player) && sprinters.get(player) == time)
//                    player.setFoodLevel(player.getFoodLevel() - 2);
//                else
//                    cancel();
//            }
//        }.runTaskTimer(plugin, 10L, 10L);
//    }
//    //Loreize items
//    private boolean realHolder(InventoryHolder holder)
//    {
//        return holder instanceof Player || holder instanceof Container || holder instanceof DoubleChest;
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    private void onInventoryClick(InventoryClickEvent event)
//    {
//        if (!realHolder(event.getInventory().getHolder()))
//            return;
//        loreize(event.getCurrentItem());
//        loreize(event.getCursor());
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    private void onCraft(CraftItemEvent event)
//    {
//        loreize(event.getCurrentItem());
//    }
//
//    private void loreize(ItemStack itemStack)
//    {
//        if (itemStack == null || itemStack.getType() == Material.AIR)
//            return;
//        ItemMeta itemMeta = itemStack.getItemMeta();
//        if (itemMeta.hasLore())
//            return;
//
//        List<String> lore = new ArrayList<>();
//
//        switch (itemStack.getType())
//        {
//            case GOLD_LEGGINGS:
//                lore.add("Gold sanic???");
//                lore.add("");
//                lore.add(ChatColor.YELLOW + "Super Dash");
//                lore.add("At full power, sprint to dash.");
//                break;
//            case GOLD_BOOTS:
//                lore.add("Compressed air makes it easier to defy gravity!");
//                lore.add("");
//                lore.add(ChatColor.YELLOW + "Jump+Dive");
//                lore.add("Sneak in midair to doublejump.");
//                lore.add("Sneak again to airdive.");
//                lore.add("");
//                lore.add(ChatColor.GRAY + "Passives:");
//                lore.add(ChatColor.GRAY + "Fall damage protection");
//                lore.add(ChatColor.GRAY + "No power cost");
//                break;
//            case IRON_LEGGINGS:
//                lore.add("Electromagnets work");
//                lore.add("");
//                lore.add(ChatColor.WHITE + "Hover");
//                lore.add("Sneak in midair to hover.");
//                lore.add("");
//                lore.add(ChatColor.GRAY + "Passives:");
//                lore.add(ChatColor.GRAY + "Fall damage protection");
//                break;
//            case IRON_BOOTS:
//                lore.add("Magnets, how do they work?");
//                lore.add("");
//                lore.add(ChatColor.WHITE + "Hover");
//                lore.add("Sneak in midair to hover.");
//                lore.add("");
//                lore.add(ChatColor.GRAY + "Passives:");
//                lore.add(ChatColor.GRAY + "Fall damage protection");
//                break;
//            default:
//                return;
//        }
//        itemMeta.setLore(lore);
//        NSA.setItemVersion(itemMeta, 2, 1);
//        itemStack.setItemMeta(itemMeta);
//    }
//        CustomItemRecipes customItems = plugin.getCustomItemRecipes();
//
//        List<String> bootsLore = new ArrayList<>();
//        bootsLore.add("The standard gravity-defying doublejump");
//        bootsLore.add("");
//        bootsLore.add(ChatColor.YELLOW + "Jump+Dive");
//        bootsLore.add("Sneak in midair to doublejump.");
//        bootsLore.add("Sneak again to airdive.");
//        bootsLore.add("");
//        bootsLore.add(ChatColor.GRAY + "Passives:");
//        bootsLore.add(ChatColor.GRAY + "Fall damage protection");
//        bootsLore.add(ChatColor.GRAY + "No power cost");
//        customItems.registerItem(customItems.loreize(new ItemStack(Material.GOLD_BOOTS), bootsLore), "goldBoots");
//        ShapedRecipe bootsRecipe = customItems.getShapedRecipe(plugin, "goldBoots");
//        bootsRecipe.shape("gag", "gag").setIngredient('g', Material.GOLD_INGOT).setIngredient('a', Material.AIR);
//        plugin.getServer().addRecipe(bootsRecipe);
//
//        List<String> leggingsLore = new ArrayList<>();
//        leggingsLore.add("ur 2 slow");
//        leggingsLore.add("");
//        leggingsLore.add(ChatColor.YELLOW + "Sonic Dash");
//        leggingsLore.add("At full power, sprint to dash.");
//        customItems.registerItem(customItems.loreize(new ItemStack(Material.GOLD_LEGGINGS), leggingsLore), "goldLeggings");
//        ShapedRecipe leggingsRecipe = customItems.getShapedRecipe(plugin, "goldLeggings");
//        leggingsRecipe.shape("ggg", "gag", "gag").setIngredient('g', Material.GOLD_INGOT).setIngredient('a', Material.AIR);
//        plugin.getServer().addRecipe(leggingsRecipe);
