package de.alive.preiscxn.api.interfaces;

import com.google.gson.JsonObject;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.networking.DataAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public interface IItemStack {
    Pattern JSON_KEY_PATTERN = Pattern.compile("([{,])(\\w+):");
    Pattern TO_DELETE_PATTERN = Pattern.compile("[\\\\']");

    PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData);

    List<String> priceCxn$getLore();
    String priceCxn$getItemName();
    String priceCxn$getDisplayName();

    int priceCxn$getCount();

    boolean priceCxn$isTrimTemplate();
    boolean priceCxn$isNetheriteUpgradeSmithingTemplate();

    Optional<String> priceCxn$getRegistryKey();

    JsonObject priceCxn$getComponentsAsJson();
}
