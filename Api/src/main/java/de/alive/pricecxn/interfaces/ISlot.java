package de.alive.pricecxn.interfaces;

import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.networking.DataAccess;
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
