package de.alive.pricecxn.listener;

import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.utils.TimeUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class InventoryListener {

    private static final int REFRESH_INTERVAL = 200;

    private final DataAccess inventoryTitles;
    private final int inventorySize; //Anzahl an Slots
    protected static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final List<Integer> slotNbt = new ArrayList<>();

    private final AtomicBoolean[] active;

    private boolean isOpen = false;

    private long lastUpdate = 0;

    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     */
    public InventoryListener(@NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean... active) {

        this.inventorySize = inventorySize;
        this.inventoryTitles = inventoryTitles;
        this.active = active;

        init();
    }

    //setup of Listeners
    private void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (active != null && Arrays.stream(active).anyMatch(bool -> !bool.get())) return;
            if (client.player == null) return;
            if (client.player.currentScreenHandler == null) return;

            if (this.isOpen && !(client.currentScreen instanceof HandledScreen)) {
                this.isOpen = false;
                onInventoryClose(client, client.player.currentScreenHandler);
            }

            if (client.currentScreen == null) return;
            if (client.currentScreen.getTitle().getString() == null || client.currentScreen.getTitle().getString().isEmpty())
                return;


            if (!this.isOpen && client.currentScreen instanceof HandledScreen && isInventoryTitle(client, inventoryTitles.getData())) {
                ScreenHandler handler = client.player.currentScreenHandler;
                initSlotsAsync(handler).thenRun(() -> {
                    this.isOpen = true;
                    lastUpdate = System.currentTimeMillis();
                    onInventoryOpen(client, handler);
                });
                return;
            }

            hadItemsChangeAsync(client, client.player.currentScreenHandler)
                    .thenAccept(hasChanged -> {
                        if (hasChanged) {
                            lastUpdate = System.currentTimeMillis();
                            onInventoryUpdate(client, client.player.currentScreenHandler);
                        }
                    });

        });
    }

    /**
     * This method is called when the inventory is opened
     *
     * @param client  The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    /**
     * This method is called when the inventory is closed
     *
     * @param client  The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    /**
     * This method is called when the inventory is updated
     *
     * @param client  The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    private boolean isInventoryTitle(@NotNull MinecraftClient client, @Nullable List<String> inventoryTitles) {
        if (client.currentScreen == null) return false;
        if (inventoryTitles == null) return false;

        for (String title : inventoryTitles) {
            if (client.currentScreen.getTitle().getString().equals(title))
                return true;
        }

        return false;
    }

    private boolean hadItemsChange(@NotNull MinecraftClient client, @Nullable ScreenHandler handler) {
        if (lastUpdate + REFRESH_INTERVAL > System.currentTimeMillis()) return false;
        if (client.player == null) return false;
        if (handler == null) return false;
        if (!isInventoryTitle(client, inventoryTitles.getData())) return false;

        for (int i = 0; i < this.inventorySize; i++) {

            if (handler.getSlot(i).getStack() != null && !slotNbt.contains(getSlotUniqueHash(handler.getSlot(i)))) {
                initSlots(handler);
                return true;
            }

        }

        return false;
    }

    public CompletableFuture<Boolean> hadItemsChangeAsync(MinecraftClient client, ScreenHandler handler) {
        return CompletableFuture.supplyAsync(() -> hadItemsChange(client, handler), EXECUTOR);
    }

    public CompletableFuture<Void> initSlotsAsync(ScreenHandler handler) {
        return CompletableFuture.runAsync(() -> initSlots(handler), EXECUTOR);
    }

    private void initSlots(@Nullable ScreenHandler handler) {
        if (handler == null) return;

        this.slotNbt.clear();

        for (int i = 0; i < this.inventorySize; i++) {
            if (handler.getSlot(i).getStack() != null) {
                slotNbt.add(getSlotUniqueHash(handler.getSlot(i)));
            }
        }
    }

    private int getSlotUniqueHash(@NotNull Slot slot) {
        return slot.getStack().getNbt() == null ? slot.getStack().getName().hashCode() : slot.getStack().getNbt().hashCode();
    }

    public static void updateItemsAsync(@NotNull List<PriceCxnItemStack> items,
                                        @NotNull ScreenHandler handler,
                                        @NotNull Pair<Integer, Integer> range,
                                        @Nullable Map<String, DataAccess> searchData,
                                        boolean addComment) {
        CompletableFuture.supplyAsync(() -> {
            for (int i = range.getLeft(); i <= range.getRight(); i++) {
                Slot slot = handler.getSlot(i);
                if (slot.getStack().isEmpty()) continue;

                PriceCxnItemStack newItem = new PriceCxnItemStack(slot.getStack(), searchData, addComment);

                boolean add = true;

                synchronized (items) {
                    for (PriceCxnItemStack item : items) {
                        if (item.equals(newItem)) {

                            if (searchData != null
                                    && searchData.containsKey("timestamp")
                                    && !TimeUtil.timestampsEqual(
                                    item.getData().get("timestamp").getAsLong(),
                                    newItem.getData().get("timestamp").getAsLong(),
                                    5)) {
                                continue;
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

            return null;
        }, EXECUTOR);

    }

    public static Optional<PriceCxnItemStack> updateItem(@Nullable PriceCxnItemStack item,
                                                         @NotNull ScreenHandler handler,
                                                         final int slotIndex,
                                                         @Nullable Map<String, DataAccess> searchData,
                                                         boolean addComment) {
        Slot slot = handler.getSlot(slotIndex);
        if (slot.getStack().isEmpty()) return Optional.empty();

        PriceCxnItemStack newItem = new PriceCxnItemStack(slot.getStack(), searchData, addComment);
        if(item == null) return Optional.of(newItem);

        if (item.equals(newItem)) {
            if (searchData != null
                    && searchData.containsKey("timestamp")
                    && !TimeUtil.timestampsEqual(
                    item.getData().get("timestamp").getAsLong(),
                    newItem.getData().get("timestamp").getAsLong(),
                    5)) {
                return Optional.empty();
            } else if(!item.deepEquals(newItem)) {
                item.updateData(newItem);
                return Optional.of(item);
            } else
                return Optional.empty();
        }

        return Optional.of(newItem);
    }

    public static Optional<PriceCxnItemStack> updateItem(@Nullable PriceCxnItemStack item,
                                                         @NotNull ScreenHandler handler,
                                                         final int slotIndex) {
        return updateItem(item, handler, slotIndex, null, true);
    }

    public static void updateItemsAsync(@NotNull List<PriceCxnItemStack> items,
                                        @NotNull ScreenHandler handler,
                                        @NotNull Pair<Integer, Integer> range,
                                        @Nullable Map<String, DataAccess> searchData) {
        updateItemsAsync(items, handler, range, searchData, true);
    }

}