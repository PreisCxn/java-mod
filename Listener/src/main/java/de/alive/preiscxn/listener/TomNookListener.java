package de.alive.preiscxn.listener;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.TranslationDataAccess;
import de.alive.pricecxn.listener.InventoryListener;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.LogPrinter.printDebug;
import static de.alive.pricecxn.LogPrinter.printTester;
import static de.alive.pricecxn.listener.StaticListenerMethods.updateItemsAsync;

public class TomNookListener extends InventoryListener {
    private final List<PriceCxnItemStack> items = new ArrayList<>();

    private final Pair<Integer, Integer> itemRange = new Pair<>(13, 13);

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
    protected @NotNull Mono<Void> onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("TomNook open");

        items.clear();
        this.invBuyPrice = getBuyPriceFromInvName(client);
        return updateItemsAsync(this.items, handler, this.itemRange, null);
    }

    @Override
    protected @NotNull Mono<Void> onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
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

    private @Nullable String getBuyPriceFromInvName(@NotNull MinecraftClient client) {
        if (client.currentScreen == null || client.currentScreen.getTitle() == null)
            return null;

        String screenTitle = client.currentScreen.getTitle().getString();

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
    protected @NotNull Mono<Void> onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("TomNook updated");
        this.invBuyPrice = getBuyPriceFromInvName(client);
        return updateItemsAsync(this.items, handler, this.itemRange, null);
    }
}
