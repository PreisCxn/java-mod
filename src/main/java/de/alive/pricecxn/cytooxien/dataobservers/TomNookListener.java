package de.alive.pricecxn.cytooxien.dataobservers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.TranslationDataAccess;
import de.alive.pricecxn.listener.InventoryListener;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class TomNookListener extends InventoryListener {

    private final List<PriceCxnItemStack> items = new ArrayList<>();

    private final Pair<Integer, Integer> itemRange = new Pair<>(13, 13);

    private final DataAccess searchData = TranslationDataAccess.NOOK_BUY_SEARCH;

    private Optional<String> invBuyPrice = Optional.empty();

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
    protected Mono<Void> onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("TomNook open");

        items.clear();
        this.invBuyPrice = getBuyPriceFromInvName(client);
        return updateItemsAsync(this.items, handler, this.itemRange, null);
    }

    @Override
    protected Mono<Void> onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("TomNook close");

        JsonArray array = new JsonArray();

        if (!items.isEmpty()) {
            for (PriceCxnItemStack item : items) {
                this.invBuyPrice.ifPresent(s -> {
                    JsonObject obj = item.getData();
                    obj.addProperty("buyPrice", this.invBuyPrice.get());
                    array.add(obj);
                });
            }
        }

        System.out.println("Nook: " + array.size() + " items");
        System.out.println(array);


        if(!array.isEmpty())
            return sendData("/tomnook", array).doOnSuccess(aVoid -> {
                printDebug("Nook data sent");
            });

        return Mono.empty();
    }

    private Optional<String> getBuyPriceFromInvName(@NotNull MinecraftClient client) {
        if (client.currentScreen == null || client.currentScreen.getTitle() == null)
            return Optional.empty();

        String screenTitle = client.currentScreen.getTitle().getString();

        for(String s : this.searchData.getData()) {
            if(s.contains("--##--")) {
                String[] split = s.split("--##--");

                if(split.length == 2) {
                    String result = StringUtil.extractBetweenParts(screenTitle, split[0], split[1]);
                    if(result != null && StringUtil.isValidPrice(result)) {
                        return Optional.of(result);
                    }
                }

            }
        }

        return Optional.empty();
    }

    @Override
    protected Mono<Void> onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("TomNook updated");
        this.invBuyPrice = getBuyPriceFromInvName(client);
        return updateItemsAsync(this.items, handler, this.itemRange, null);
    }
}
