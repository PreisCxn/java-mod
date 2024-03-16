package de.alive.pricecxn.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * This class is used to provide some useful methods for Strings
 */
public class StringUtil {

    public static String removeLastChar(String text) {
        if (text == null || text.isEmpty()) {
            return text; // Wenn der Eingabestring leer ist oder null, gibt ihn unverändert zurück.
        }

        return text.substring(0, text.length() - 1);
    }

    public static String removeChars(String text) {
        StringBuilder nurZahlen = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char zeichen = text.charAt(i);
            if (Character.isDigit(zeichen)) {
                nurZahlen.append(zeichen);
            }
        }
        return nurZahlen.toString();
    }

    public static String convertPrice(double time) {
        Locale locale = Locale.GERMAN;
        Locale.setDefault(locale);

        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        if(time >= 100000) {
            decimalFormat.setMaximumFractionDigits(0);
            decimalFormat.setMinimumFractionDigits(0);
        } else {
            decimalFormat.setMaximumFractionDigits(2);
            decimalFormat.setMinimumFractionDigits(2);
        }

        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        decimalFormat.setDecimalFormatSymbols(symbols);

        return decimalFormat.format(time);
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

    public static List<String> stringToList(String input) {
        // Teilen Sie den Eingabestring an den Kommas auf
        String[] words = input.split(",\\s*");

        // Konvertieren Sie das Array in eine Liste
        return new ArrayList<>(Arrays.asList(words));
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

    public static String extractBetweenParts(String s, String start, String end) {
        int sIndex = s.indexOf(start) + start.length();
        int eIndex = s.indexOf(end);
        if (sIndex < eIndex) {
            return s.substring(sIndex, eIndex);
        }
        return null;
    }

    public static String getFirstSuffixStartingWith(List<String> strings, String prefix) {
        for (String s : strings) {
            if (s.startsWith(prefix)) {
                return s.substring(prefix.length());
            }
        }
        return null;
    }

    public static boolean containsString(String string, List<String> searches) {
        return searches.stream().anyMatch(string::contains);
    }

    public static JsonElement removeLastChar(JsonElement element) {
        if (!element.isJsonPrimitive()) return JsonNull.INSTANCE;
        String string = element.getAsString();
        return new JsonPrimitive(string.substring(0, string.length() - 1));
    }

}
