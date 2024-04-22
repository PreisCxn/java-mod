package de.alive.pricecxn.listener;

import com.google.gson.JsonNull;
import de.alive.pricecxn.PriceCxn;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.utils.TimeUtil;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StaticListenerMethods {
    public static @NotNull Mono<Void> updateItemsAsync(@NotNull List<PriceCxnItemStack> items,
                                                       @NotNull ScreenHandler handler,
                                                       @NotNull Pair<Integer, Integer> range,
                                                       @Nullable Map<String, DataAccess> searchData,
                                                       boolean addComment) {
        return Mono.fromRunnable(() -> {
            for (int i = range.getLeft(); i <= range.getRight(); i++) {
                Slot slot = handler.getSlot(i);
                if (slot.getStack().isEmpty()) continue;

                PriceCxnItemStack newItem = PriceCxn.getMod().createItemStack(slot.getStack(), searchData, addComment);

                boolean add = true;

                synchronized(items){
                    for (PriceCxnItemStack item : items) {
                        if (item.equals(newItem)) {

                            if (searchData != null && searchData.containsKey("timestamp")) {
                                if (newItem.getData().get("timestamp") != JsonNull.INSTANCE &&
                                    item.getData().get("timestamp") != JsonNull.INSTANCE &&
                                    !TimeUtil.timestampsEqual(
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
                                                                  @NotNull ScreenHandler handler,
                                                                  final int slotIndex,
                                                                  @Nullable Map<String, DataAccess> searchData,
                                                                  boolean addComment) {
        Slot slot = handler.getSlot(slotIndex);
        if (slot.getStack().isEmpty()) return Optional.empty();

        PriceCxnItemStack newItem = PriceCxn.getMod().createItemStack(slot.getStack(), searchData, addComment);
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
                                                                  @NotNull ScreenHandler handler,
                                                                  final int slotIndex) {
        return updateItem(item, handler, slotIndex, null, true);
    }

    public static @NotNull Mono<Void> updateItemsAsync(@NotNull List<PriceCxnItemStack> items,
                                                       @NotNull ScreenHandler handler,
                                                       @NotNull Pair<Integer, Integer> range,
                                                       @Nullable Map<String, DataAccess> searchData) {
        return updateItemsAsync(items, handler, range, searchData, true);
    }
}
