package me.robomwm.MountainDewritoes.combat.twoshot;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Provides convenience methods to access and mutate a weapon's state, currently stored in ItemStack ItemMeta.
 */

public class WeaponState
{
    private static final String KEY = "TS_WEAPONSTATE";
    private final ItemStack item;
    private final WeaponProperties properties;
    private BukkitTask reloadTask;

    public WeaponState(ItemStack itemStack, WeaponProperties properties)
    {
        this.item = itemStack;
        this.properties = properties;
    }

    public WeaponProperties getProperties()
    {
        return properties;
    }

    public long getLastFired()
    {
        ItemMeta meta = getItemMeta();
        String ammoCount = revealText(meta.getLore().get(0));

        if (ammoCount == null || ammoCount.isEmpty())
            return 0;

        return Long.parseLong(ammoCount);
    }

    private ItemMeta getItemMeta()
    {
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName())
            meta.setDisplayName(item.getI18NDisplayName());
        if (!meta.getDisplayName().endsWith("/" + properties.getCapacity() + ">"))
        {
            meta.setDisplayName(meta.getDisplayName() + ChatColor.YELLOW + " < 0 / " + properties.getCapacity() + " >");
            item.setItemMeta(meta);
        }

        return meta;
    }

    public int getRemainingAmmo()
    {
        String[] name = getItemMeta().getDisplayName().split(" ");
        return Integer.parseInt(name[name.length - 4]);
    }

    public boolean canFire(long currentTick)
    {
        if (reloadTask == null && getLastFired() + properties.getFireRate() < currentTick
                && getRemainingAmmo() > 0)
        {
            setLastFired(currentTick);
            setRemainingAmmo(getRemainingAmmo() - properties.getAmmoPerShot());
            return true;
        }
        return false;
    }

    private void setLastFired(long tick)
    {
        ItemMeta meta = getItemMeta();
        List<String> lore = meta.getLore();
        lore.set(0, hideText(Long.toString(tick)));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void setRemainingAmmo(int amount)
    {
        amount = Math.max(0, amount);
        String[] name = getItemMeta().getDisplayName().split(" ");
        name[name.length - 4] = Integer.toString(amount);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(String.join(" ", name));
        item.setItemMeta(itemMeta);
    }

    /**
     *
     * @return false if already reloading
     */
    public boolean reloadWeapon(Plugin plugin)
    {
        if (reloadTask != null)
            return false;

        reloadTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                setRemainingAmmo(properties.getCapacity());
                reloadTask = null;
            }
        }.runTaskLater(plugin, properties.getReloadTime());
        return true;
    }

    //hex lore hider thingy from https://www.spigotmc.org/threads/how-to-hide-item-lore-how-to-bind-data-to-itemstack.196008/#post-2043170

    /**
     * Hides text in color codes
     *
     * @param text The text to hide
     * @return The hidden text
     */
    private static String hideText(String text) {
        Objects.requireNonNull(text, "text can not be null!");

        StringBuilder output = new StringBuilder();

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String hex = Hex.encodeHexString(bytes);

        for (char c : hex.toCharArray()) {
            output.append(ChatColor.COLOR_CHAR).append(c);
        }

        return output.toString();
    }

    /**
     * Reveals the text hidden in color codes
     *
     * @param text The hidden text
     * @throws IllegalArgumentException if an error occurred while decoding.
     * @return The revealed text
     */
    private static String revealText(String text) {
        Objects.requireNonNull(text, "text can not be null!");

        if (text.isEmpty()) {
            return text;
        }

        char[] chars = text.toCharArray();

        try
        {
            char[] hexChars = new char[chars.length / 2];

            IntStream.range(0, chars.length)
                    .filter(value -> value % 2 != 0)
                    .forEach(value -> hexChars[value / 2] = chars[value]);
            return new String(Hex.decodeHex(hexChars), StandardCharsets.UTF_8);
        } catch (DecoderException e) {
            //e.printStackTrace();
            //throw new IllegalArgumentException("Couldn't decode text", e);
            return null;
        }
    }
}
