package de.alive.pricecxn;

import de.alive.pricecxn.cytooxien.ICxnListener;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.networking.DataAccess;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Mod {
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData);

    ICxnListener getCxnListener();
}
