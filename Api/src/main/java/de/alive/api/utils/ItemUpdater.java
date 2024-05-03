package de.alive.api.utils;

import com.google.gson.JsonNull;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.IScreenHandler;
import de.alive.api.interfaces.ISlot;
import de.alive.api.networking.DataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ItemUpdater {
    private ItemUpdater() {
    }
    public static @NotNull Mono<Void> updateItemsAsync(@NotNull List<PriceCxnItemStack> items,
                                                       @NotNull IScreenHandler handler,
                                                       @NotNull Tuple2<Integer, Integer> range,
                                                       @Nullable Map<String, DataAccess> searchData,
                                                       boolean addComment) {
        return Mono.fromRunnable(() -> {
            for (int i = range.getT1(); i <= range.getT2(); i++) {
                ISlot slot = handler.getSlot(i);
                if (slot.isStackEmpty()) continue;

                PriceCxnItemStack newItem = slot.createItemStack(searchData, addComment);

                boolean add = true;

                synchronized (items) {
                    for (PriceCxnItemStack item : items) {
                        if (item.equals(newItem)) {

                            if (searchData != null && searchData.containsKey("timestamp")) {
                                if (newItem.getData().get("timestamp") != JsonNull.INSTANCE
                                    && item.getData().get("timestamp") != JsonNull.INSTANCE
                                    && !TimeUtil.timestampsEqual(
                                            item.getData().get("timestamp").getAsLong(),
                                            newItem.getData().get("timestamp").getAsLong(),
                                            5)) {
                                    continue;
                                }
                            }

                            add = false;
                            if (!item.deepEquals(newItem)) {
                                item.updateData(newItem);
                            }
                            break;
                        }
                    }

                    if (add) {
                        items.add(newItem);
                    }
                }

            }
        });

    }

    public static @NotNull Optional<PriceCxnItemStack> updateItem(@Nullable PriceCxnItemStack item,
                                                                  @NotNull IScreenHandler handler,
                                                                  final int slotIndex,
                                                                  @Nullable Map<String, DataAccess> searchData,
                                                                  boolean addComment) {
        ISlot slot = handler.getSlot(slotIndex);
        if (slot.isStackEmpty()) return Optional.empty();

        PriceCxnItemStack newItem = slot.createItemStack(searchData, addComment);
        if (item == null) return Optional.of(newItem);

        if (item.equals(newItem)) {
            if (searchData != null
                && searchData.containsKey("timestamp")
                && !TimeUtil.timestampsEqual(
                    item.getData().get("timestamp").getAsLong(),
                    newItem.getData().get("timestamp").getAsLong(),
                    5)) {
                return Optional.empty();
            } else if (!item.deepEquals(newItem)) {
                item.updateData(newItem);
                return Optional.of(item);
            } else
                return Optional.empty();
        }

        return Optional.of(newItem);
    }

    public static @NotNull Optional<PriceCxnItemStack> updateItem(@Nullable PriceCxnItemStack item,
                                                                  @NotNull IScreenHandler handler,
                                                                  final int slotIndex) {
        return updateItem(item, handler, slotIndex, null, true);
    }

    public static @NotNull Mono<Void> updateItemsAsync(@NotNull List<PriceCxnItemStack> items,
                                                       @NotNull IScreenHandler handler,
                                                       @NotNull Tuple2<Integer, Integer> range,
                                                       @Nullable Map<String, DataAccess> searchData) {
        return updateItemsAsync(items, handler, range, searchData, true);
    }
}
