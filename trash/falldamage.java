
    /**
     * Play fall damage sound only if the player actually took fall damage
     * (We only care about players, but this could be extended to all entities)
     * (resource pack sets sound to silence for players)
     */
    Map<EntityDamageEvent, Double> originalDamageValue = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onPlayerFallDamageGetOriginalFallDamageValue(EntityDamageEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER || event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        originalDamageValue.put(event, event.getDamage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerFallDamagePlayFallSound(EntityDamageEvent event)
    {
        //Always remove, even if canceled
        Double damage = originalDamageValue.remove(event);

        if (damage == null)
            return;
        if (event.isCancelled())
            return;

        Location location = event.getEntity().getLocation();
        World world = location.getWorld();

        if (damage < 5.0D) //Fell less than 8 blocks
            world.playSound(location, "fortress.small_fall", SoundCategory.PLAYERS, 1.0f, 1.0f);
        else
            world.playSound(location, "fortress.big_fall", SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
