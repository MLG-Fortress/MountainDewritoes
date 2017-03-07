
//AtomicBoolean over10Minutes = new AtomicBoolean(true);
//Pattern hello = Pattern.compile("\\bhello\\b|\\bhi\\b|\\bhey\\b|\\bhai\\b");
//Pattern bye = Pattern.compile("\\bsee you\\b|\\bc u\\b|\\bbye\\b");

/* Play sounds globally based on certain keywords
 * Totally not even close to ready yet, I might even scrap this idea*/
//@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
//    void onPlayerChatPlaySounds(AsyncPlayerChatEvent event)
//    {
//        //Don't care about muted/semi-muted chatters
//        if (event.getRecipients().size() < instance.getServer().getOnlinePlayers().size())
//            return;
//
//        if (!hasItBeen10minutes(false))
//            return;
//
//        String message = ChatColor.stripColor(event.getMessage().toLowerCase());
//
//        //No need to block the event to check this
//        new BukkitRunnable()
//        {
//            public void run()
//            {
//                if (hello.matcher(message).matches())
//                    playSoundGlobal("fortress.hello", 41);
//                else if (bye.matcher(message).matches())
//                    playSoundGlobal("fortress.bye", 35);
//            }
//        }.runTaskAsynchronously(instance);
//    }

    /*
     * Has it been 10 minutes since we last (globally) played a song?
     * Used for global chat trigger primarily
     */
//    boolean hasItBeen10minutes(boolean reset)
//    {
//        if (over10Minutes.get())
//        {
//            if (reset)
//                new BukkitRunnable()
//                {
//                    public void run()
//                    {
//                        over10Minutes.set(true);
//                    }
//                }.runTaskLater(instance, 20L * 60L * 10L);
//            return true;
//        }
//        else
//            return false;
//    }