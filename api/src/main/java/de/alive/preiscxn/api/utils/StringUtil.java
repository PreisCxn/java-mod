package de.alive.preiscxn.api.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import de.alive.preiscxn.api.interfaces.IItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * This class is used to provide some useful methods for Strings
 */
public final class StringUtil {
    private StringUtil() {
    }

    public static @NotNull String removeChars(@NotNull String text) {
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
        if (time >= 100000) {
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
     *
     * @param list The list to convert
     * @return The converted list
     */
    public static List<String> listToLowerCase(@Nullable List<String> list) {
        if (list == null) return null;
        List<String> lowercaseList = new ArrayList<>(list);
        lowercaseList.replaceAll(String::toLowerCase);
        return lowercaseList;
    }

    public static @NotNull List<String> stringToList(@NotNull String input) {
        // Teilen Sie den Eingabestring an den Kommas auf
        String[] words = input.split(",\\s*");

        // Konvertieren Sie das Array in eine Liste
        return new ArrayList<>(Arrays.asList(words));
    }

    public static List<String> getToolTips(@Nullable IItemStack stack) {
        if (stack == null) return null;

        return stack.getLore();
    }

    public static @Nullable String extractBetweenParts(@NotNull String s, @NotNull String start, @NotNull String end) {
        int sIndex = s.indexOf(start);
        if (sIndex == -1) {
            return null; // start string not found
        }
        sIndex += start.length();
        int eIndex = s.indexOf(end, sIndex);
        if (eIndex == -1) {
            return null; // end string not found after start string
        }
        return s.substring(sIndex, eIndex);
    }

    public static @Nullable String getFirstSuffixStartingWith(@NotNull List<String> strings, @NotNull String prefix) {
        for (String s : strings) {
            if (s.startsWith(prefix)) {
                return s.substring(prefix.length());
            }
        }
        return null;
    }

    public static boolean containsString(@NotNull String string, @NotNull List<String> searches) {
        return searches.stream().anyMatch(string::contains);
    }

    public static boolean isValidPrice(@NotNull String s) {
        return s.matches("[0-9.,]*");
    }

    public static @NotNull JsonElement removeLastChar(@NotNull JsonElement element) {
        if (!element.isJsonPrimitive()) return JsonNull.INSTANCE;
        String string = element.getAsString();
        return new JsonPrimitive(string.substring(0, string.length() - 1));
    }

}
