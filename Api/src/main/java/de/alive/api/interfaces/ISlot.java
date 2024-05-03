package de.alive.api.interfaces;

import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.networking.DataAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ISlot {
    PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData);

    boolean isStackNbtNull();
    int stackNameHash();
    int stackNbtHash();
    boolean isStackEmpty();

    boolean isStackNull();
}
