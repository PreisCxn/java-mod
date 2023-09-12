package de.alive.pricecxn.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to provide some useful methods for Strings
 */
public class StringUtil {

    public static Pair<String, Formatting> TextComponent(String str, Formatting formatting) {
        return Pair.of(str, formatting);
    }

    public static MutableText getColorizedString(String string, Formatting formatting){
        return MutableText.of(new LiteralTextContent(string)).setStyle(Style.EMPTY.withColor(formatting));
    }

    public static MutableText getColorizedString(List<Pair<String, Formatting>> parts) {

        String first = parts.get(0).getFirst();

        MutableText msg = StringUtil.getColorizedString(parts.get(0).getFirst(), parts.get(0).getSecond());

        for (Pair<String, Formatting> part : parts) {

            if(Objects.equals(part.getFirst(), first)) continue;

            msg.append(StringUtil.getColorizedString(part.getFirst(), part.getSecond()));
        }

        return msg;
    }

    /**
     * This method converts all Strings in a list to lower case
     * @param list The list to convert
     * @return The converted list
     */
    public static String[] listToLowerCase(@Nullable String[] list) {
        if(list == null) return null;

        String[] newList = new String[list.length];
        for (int i = 0; i < list.length; i++) {
            if(list[i] != null)
                newList[i] = list[i].toLowerCase();
        }
        return newList;
    }

    /**
     * This method converts all Strings in a list to lower case
     * @param list The list to convert
     * @return The converted list
     */
    public static List<String> listToLowerCase(@Nullable List<String> list) {
        if (list == null) return null;

        List<String> lowercaseList = new ArrayList<>(list);
        lowercaseList.replaceAll(String::toLowerCase);
        return lowercaseList;
    }

}
