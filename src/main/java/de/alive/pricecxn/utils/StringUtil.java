package de.alive.pricecxn.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to provide some useful methods for Strings
 */
public class StringUtil {

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

    public static List<String> getToolTips(ItemStack stack){
        if(stack == null) return null;
        List<String> result = new ArrayList<>();

        List<Text> tooltip = stack.getTooltip(MinecraftClient.getInstance().player,
                MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);

        for (Text line : tooltip){
            result.add(line.getString());
        }

        return result;
    }

    public static String getFirstSuffixStartingWith(List<String> strings, String prefix) {
        for (String s : strings) {
            if (s.startsWith(prefix)) {
                return s.substring(prefix.length());
            }
        }
        return null;
    }

    public static boolean containsString(String string, List<String> searches){
        for(String search : searches){
            if(string.contains(search))
                return true;
        }

        return false;
    }


}
