package de.alive.pricecxn.cytooxien.listener;

import com.google.gson.JsonArray;
import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.SearchDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class TomNookListener extends InventoryListener {

    private final List<PriceCxnItemStack> items = new ArrayList<>();

    private final Pair<Integer, Integer> itemRange = new Pair<>(0, 9);

    private final Map<String, DataAccess> searchData = new HashMap<>();

     /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public TomNookListener(@NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean active) {
        super(inventoryTitles, inventorySize <= 0 ? 1*9 : inventorySize, active);

        searchData.put("buyPrice", SearchDataAccess.NOOK_BUY_SEARCH);
    }

    public TomNookListener(@Nullable AtomicBoolean active) {
        this(SearchDataAccess.INV_NOOK_SEARCH, 0, active);
    }

    @Override
    protected void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("TomNook open");
        updateItemsAsync(this.items, handler, this.itemRange, this.searchData);
    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("TomNook close");

        JsonArray array = new JsonArray();

        if(!items.isEmpty()) {
            for (PriceCxnItemStack item : items) {
                array.add(item.getData());
            }
        }

        System.out.println("Nook: " + array.size() + " items");
        System.out.println(array);

    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("TomNook updated");
        updateItemsAsync(this.items, handler, this.itemRange, this.searchData);
    }
}
