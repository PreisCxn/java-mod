package de.alive.preiscxn.inventory.listener;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.alive.pricecxn.interfaces.IMinecraftClient;
import de.alive.pricecxn.interfaces.IScreenHandler;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.TranslationDataAccess;
import de.alive.pricecxn.interfaces.Mod;
import de.alive.pricecxn.listener.InventoryListener;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.utils.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.LogPrinter.printDebug;
import static de.alive.pricecxn.LogPrinter.printTester;
import static de.alive.pricecxn.listener.StaticListenerMethods.updateItemsAsync;

public class TomNookListener extends InventoryListener {
    private final List<PriceCxnItemStack> items = new ArrayList<>();

    private final Tuple2<Integer, Integer> itemRange = Tuples.of(13, 13);

    private final DataAccess searchData = TranslationDataAccess.NOOK_BUY_SEARCH;

    private @Nullable String invBuyPrice = null;

    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public TomNookListener(@NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean... active) {
        super(inventoryTitles, inventorySize <= 0 ? 4 * 9 : inventorySize, active);
    }

    public TomNookListener(@Nullable AtomicBoolean... active) {
        this(TranslationDataAccess.INV_NOOK_SEARCH, 0, active);
    }

    @Override
    protected @NotNull Mono<Void> onInventoryOpen(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("TomNook open");

        items.clear();
        this.invBuyPrice = getBuyPriceFromInvName(client);
        return updateItemsAsync(this.items, handler, this.itemRange, null);
    }

    @Override
    protected @NotNull Mono<Void> onInventoryClose(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("TomNook close");

        JsonArray array = new JsonArray();

        if (!items.isEmpty()) {
            for (PriceCxnItemStack item : items) {
                if(this.invBuyPrice != null){
                    JsonObject obj = item.getData();
                    obj.addProperty("buyPrice", this.invBuyPrice);
                    array.add(obj);
                }
            }
        }

        //todo LOGGER.debug("Nook: " + array.size() + " items");
        //todo LOGGER.debug(array.toString());


        if(!array.isEmpty())
            return sendData("/tomnook", array).doOnSuccess(aVoid -> printTester("Nook data sent"));

        return Mono.empty();
    }

    private @Nullable String getBuyPriceFromInvName(@NotNull IMinecraftClient client) {
        if (client.isCurrentScreenNull() || client.isCurrentScreenTitleNull())
            return null;

        String screenTitle = client.getCurrentScreenTitle();

        for(String s : this.searchData.getData()) {
            if(s.contains("--##--")) {
                String[] split = s.split("--##--");

                if(split.length == 2) {
                    String result = StringUtil.extractBetweenParts(screenTitle, split[0], split[1]);
                    if(result != null && StringUtil.isValidPrice(result)) {
                        return result;
                    }
                }

            }
        }

        return null;
    }

    @Override
    protected @NotNull Mono<Void> onInventoryUpdate(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("TomNook updated");
        this.invBuyPrice = getBuyPriceFromInvName(client);
        return updateItemsAsync(this.items, handler, this.itemRange, null);
    }
}
