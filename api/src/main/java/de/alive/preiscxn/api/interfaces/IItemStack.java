package de.alive.preiscxn.api.interfaces;

import com.google.gson.JsonObject;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.networking.DataAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IItemStack {
    PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData);

    List<String> getLore();
    String getItemName();
    String getDisplayName();

    int getCount();

    boolean isTrimTemplate();
    boolean isNetheriteUpgradeSmithingTemplate();

    Optional<String> getRegistryKey();

    JsonObject getComponentsAsJson();
}