package me.robomwm.MountainDewritoes;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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

        public ItemStack toBook()
        {
            BookMeta meta = getBookMeta();
            getBookMeta().spigot().setPages(buildPages("\\p", baseComponents));
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
     * Combines a list of components into pages, split by a defined "new page" character or string.
     * Primarily for setting BookMeta.
     *
     * Does not provide any line or page wrapping.
     *
     * @param newPageChar String or character that represents a page break.
     * @param components BaseComponents
     * @return
     */
    public static List<BaseComponent[]> buildPages(String newPageChar, @Nonnull List<BaseComponent> components)
    {
        newPageChar = Pattern.quote(newPageChar);
        List<BaseComponent[]> completedPages = new ArrayList<>();
        List<BaseComponent> workingPage = new ArrayList<>();

        for (BaseComponent component : components)
        {
           String plainText = component.toPlainText();

            //Handle new page char
            if (plainText.contains(newPageChar) && component instanceof TextComponent)
            {
                String[] strings = plainText.split(newPageChar);
                String text;
                int length = strings.length - 1;
                for (int i = 0; i < strings.length; i++)
                {
                    text = strings[i];
                    TextComponent textComponent = (TextComponent)(component.duplicate());
                    textComponent.setText(text);
                    workingPage.add(textComponent);
                    Bukkit.broadcastMessage(text);
                    //Don't append page break with last element
                    if (i < length)
                        completedPages.add(workingPage.toArray(new BaseComponent[0]));
                }
                continue;
            }

            //add component to page
            workingPage.add(component);
        }

        //add last page to collection
        completedPages.add(workingPage.toArray(new BaseComponent[0]));

        return completedPages;
    }

    private static int indexOf(String string, char c)
    {
        int index = string.indexOf(c);
        if (index < 0)
            return string.length();
        return index;
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
