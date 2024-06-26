package de.alive.preiscxn.listener.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.alive.preiscxn.api.Mod;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.interfaces.IScreenHandler;
import de.alive.preiscxn.api.listener.InventoryListener;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.api.utils.StringUtil;
import de.alive.preiscxn.listener.InventoryDataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.preiscxn.api.utils.ItemUpdater.updateItemsAsync;

public class TomNookListener extends InventoryListener {
    private final List<PriceCxnItemStack> items = new ArrayList<>();

    private final Tuple2<Integer, Integer> itemRange = Tuples.of(13, 13);

    private final DataAccess searchData = InventoryDataAccess.NOOK_BUY_SEARCH;

    private @Nullable String invBuyPrice = null;

    /**
     * This constructor is used to listen to a specific inventory.
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public TomNookListener(@NotNull Mod mod, @NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean... active) {
        super(mod, inventoryTitles, inventorySize <= 0 ? 4 * 9 : inventorySize, active);
    }

    public TomNookListener(Mod mod, @Nullable AtomicBoolean... active) {
        this(mod, InventoryDataAccess.INV_NOOK_SEARCH, 0, active);
    }

    @Override
    public @NotNull Mono<Void> onInventoryOpen(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        PriceCxn.getMod().printDebug("TomNook open");

        items.clear();
        this.invBuyPrice = getBuyPriceFromInvName(client);
        return updateItemsAsync(this.items, handler, this.itemRange, null);
    }

    @Override
    public @NotNull Mono<Void> onInventoryClose(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        PriceCxn.getMod().printDebug("TomNook close");

        JsonArray array = new JsonArray();

        if (!items.isEmpty()) {
            for (PriceCxnItemStack item : items) {
                if (this.invBuyPrice != null) {
                    JsonObject obj = item.getDataWithoutDisplay();
                    obj.addProperty("buyPrice", this.invBuyPrice);
                    array.add(obj);
                }
            }
        }

        PriceCxn.getMod().getLogger().debug("Nook: " + array.size() + " items");
        PriceCxn.getMod().getLogger().debug(array.toString());

        if (!array.isEmpty())
            return sendData("/tomnook", array).doOnSuccess(aVoid -> PriceCxn.getMod().printTester("Nook data sent"));

        return Mono.empty();
    }

    private @Nullable String getBuyPriceFromInvName(@NotNull IMinecraftClient client) {
        if (client.isCurrentScreenNull() || client.getInventory().getTitle() == null)
            return null;

        String screenTitle = client.getInventory().getTitle();

        for (String s : this.searchData.getData().getData()) {
            if (s.contains("--##--")) {
                String[] split = s.split("--##--");

                if (split.length == 5) {
                    String result = StringUtil.extractBetweenParts(screenTitle, split[2], split[3]);
                    if (result != null && StringUtil.isValidPrice(result)) {
                        return result;
                    }
                }

            }
        }

        return null;
    }

    @Override
    public @NotNull Mono<Void> onInventoryUpdate(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        PriceCxn.getMod().printDebug("TomNook updated");
        this.invBuyPrice = getBuyPriceFromInvName(client);
        return updateItemsAsync(this.items, handler, this.itemRange, null);
    }
}