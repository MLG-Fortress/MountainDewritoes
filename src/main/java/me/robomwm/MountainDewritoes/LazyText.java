package me.robomwm.MountainDewritoes;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 3/10/2018.
 *
 * @author RoboMWM
 */
public class LazyText
{
    //Ok, now this is looking like that Text library thing
    //Oh well
    public static class Builder
    {
        List<BaseComponent> baseComponents = new ArrayList<>();

        public Builder add(BaseComponent component)
        {
            baseComponents.add(component);
            return this;
        }

        public Builder add(BaseComponent[] components)
        {
            baseComponents.addAll(Arrays.asList(components));
            return this;
        }

        public Builder add(List<BaseComponent> components)
        {
            baseComponents.addAll(components);
            return this;
        }

        public Builder add(String text)
        {
            baseComponents.addAll(Arrays.asList(TextComponent.fromLegacyText(text)));
            return this;
        }

        public Builder add(String text, ChatColor color)
        {
            baseComponents.addAll(Arrays.asList(TextComponent.fromLegacyText(text, color)));
            return this;
        }

        public Builder color(ChatColor color)
        {
            last().setColor(color);
            return this;
        }

        public BaseComponent last()
        {
            return baseComponents.get(baseComponents.size() - 1);
        }

        public List<BaseComponent> getBaseComponents()
        {
            return baseComponents;
        }

        public BaseComponent[] getBaseComponentsArray()
        {
            return baseComponents.toArray(new BaseComponent[0]);
        }

        public ItemStack getBook(int maxWidth, int maxLines)
        {
            BookMeta meta = getBookMeta();
            getBookMeta().spigot().setPages(buildPages(maxWidth, maxLines, baseComponents));
            return LazyText.getBook(meta);
        }
    }

    public static BookMeta getBookMeta()
    {
        return (BookMeta)(new ItemStack(Material.WRITTEN_BOOK).getItemMeta());
    }

    public static ItemStack getBook(BookMeta bookMeta)
    {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.setItemMeta(bookMeta);
        return book;
    }

    @Deprecated
    public static BaseComponent[] buildPage(Object... components)
    {
        List<BaseComponent> baseComponents = new ArrayList<>(components.length);
        for (Object object : components)
        {
            if (object instanceof BaseComponent)
                baseComponents.add((BaseComponent)object);
            else if (object instanceof String)
                baseComponents.addAll(Arrays.asList(TextComponent.fromLegacyText((String)object)));
        }
        return baseComponents.toArray(new BaseComponent[0]);
    }

    /**
     * Combines components into an array split into an element for each page.
     * Primarily for setting BookMeta.
     *
     *
     * @apiNote Currently lacking: page overflow (splitting component across pages when too large)
     * @param maxWidth max characters that can be in a line.
     * @param lineCount max lines that can be in a page.
     * @param components BaseComponents
     * @return
     */
    public static List<BaseComponent[]> buildPages(int maxWidth, int lineCount, @Nonnull List<BaseComponent> components)
    {
        List<BaseComponent[]> pages = new ArrayList<>();
        List<BaseComponent> page = new ArrayList<>();
        int currentLineWidth = 0;
        int lines = 0;

        for (BaseComponent component : components)
        {
            String text = component.toPlainText();

            //Handle "new page" character: \p
            //For now, "new page" character has to be its own string/component
            if (text.equalsIgnoreCase("\\p"))
            {
                pages.add(page.toArray(new BaseComponent[0]));
                page.clear();
                currentLineWidth = 0;
                lines = 0;
                continue;
            }

            //Add newlines
            if (text.contains("\n"))
            {
                lines += StringUtils.countMatches(text, "\n");
                text = text.substring(text.lastIndexOf("\n"));
            }

            currentLineWidth += text.length();

            if (currentLineWidth > maxWidth)
            {
                lines += Math.ceil(currentLineWidth / (double)maxWidth);
            }

            if (lines > lineCount)
            {
                pages.add(page.toArray(new BaseComponent[0]));
                page.clear();
                currentLineWidth = text.length();
                lines = (int)Math.ceil(currentLineWidth / (double)maxWidth);
            }

            page.add(component);
        }

        return pages;
    }

    @Deprecated
    public static List<BaseComponent> addLegacyText(String string, List<BaseComponent> baseComponents)
    {
        for (BaseComponent baseComponent : TextComponent.fromLegacyText(string))
            baseComponents.add(baseComponent);
        return baseComponents;
    }

    @Deprecated
    public static TextComponent command(String message, String command)
    {
        return command(message, command, command);
    }

    @Deprecated
    public static TextComponent command(String message, String command, String hover)
    {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setColor(ChatColor.DARK_AQUA);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        if (hover != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return textComponent;
    }

    @Deprecated
    public static TextComponent url(String message, String URL, String hover)
    {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setColor(ChatColor.DARK_AQUA);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, URL));
        if (hover != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return textComponent;
    }

    @Deprecated
    public static TextComponent suggest(String message, String suggestion)
    {
        return suggest(message, suggestion, suggestion);
    }

    @Deprecated
    public static TextComponent suggest(String message, String suggestion, String hover)
    {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setColor(ChatColor.DARK_AQUA);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion));
        if (hover != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return textComponent;
    }

    @Deprecated
    public static TextComponent hover(String message, String hover)
    {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setColor(ChatColor.AQUA);
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return textComponent;
    }
}
