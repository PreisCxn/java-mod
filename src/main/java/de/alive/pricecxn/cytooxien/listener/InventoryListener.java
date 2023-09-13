package de.alive.pricecxn.cytooxien.listener;

import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.DataHandler;
import de.alive.pricecxn.utils.StringUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class InventoryListener {

    private final DataAccess inventoryTitles;
    private final int inventorySize; //Anzahl an Slots
    private final Executor executor = Executors.newSingleThreadExecutor();

    private final List<String> slotNbt = new ArrayList<>();

    private final AtomicBoolean active;

    private boolean isOpen = false;

    /**
     *  This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize The size of the inventories to listen to (in slots)
     */
    public InventoryListener(@NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean active){

        this.inventorySize = inventorySize;
        this.inventoryTitles = inventoryTitles;
        this.active = active;

        init();
    }

    //setup of Listeners
    private void init(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(active != null && !active.get()) return;
            if (client.player == null) return;
            if (client.player.currentScreenHandler == null) return;

            if(this.isOpen && !(client.currentScreen instanceof HandledScreen)) {
                this.isOpen = false;
                onInventoryClose(client, client.player.currentScreenHandler);
            }

            if(client.currentScreen == null) return;
            if(client.currentScreen.getTitle().getString() == null || client.currentScreen.getTitle().getString().equals("")) return;


            if(!this.isOpen && client.currentScreen instanceof HandledScreen && isInventoryTitle(client, inventoryTitles.getData())){
                ScreenHandler handler = client.player.currentScreenHandler;
                initSlotsAsync(handler).thenRun(() -> {
                    this.isOpen = true;
                    onInventoryOpen(client, handler);
                });
                return;
            }

            hadItemsChangeAsync(client, client.player.currentScreenHandler)
                    .thenAccept(hasChanged -> {
                        if (hasChanged) {
                            onInventoryUpdate(client, client.player.currentScreenHandler);
                        }
                    });

        });
    }

    /**
     * This method is called when the inventory is opened
     * @param client The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    /**
     * This method is called when the inventory is closed
     * @param client The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    /**
     * This method is called when the inventory is updated
     * @param client The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    private boolean isInventoryTitle(@NotNull MinecraftClient client, @Nullable List<String> inventoryTitles){
        if(client.currentScreen == null) return false;
        if(inventoryTitles == null) return false;

        for(String title : inventoryTitles){
            if(client.currentScreen.getTitle().getString().equals(title))
                return true;
        }

        return false;
    }

    private boolean hadItemsChange(@NotNull MinecraftClient client, @Nullable ScreenHandler handler){
        if(client.player == null) return false;
        if(handler == null) return false;
        if(!isInventoryTitle(client, inventoryTitles.getData())) return false;

        for (int i = 0; i < this.inventorySize; i++){

            if(handler.getSlot(i).getStack() != null && !slotNbt.contains(getSlotUniqueString(handler.getSlot(i)))){
                initSlots(handler);
                return true;
            }

        }

        return false;
    }

    public CompletableFuture<Boolean> hadItemsChangeAsync(MinecraftClient client, ScreenHandler handler) {
        return CompletableFuture.supplyAsync(() -> hadItemsChange(client, handler), executor);
    }

    public CompletableFuture<Void> initSlotsAsync(ScreenHandler handler) {
        return CompletableFuture.runAsync(() -> initSlots(handler), executor);
    }

    private void initSlots(@Nullable ScreenHandler handler){
        if(handler == null) return;

        this.slotNbt.clear();

        for (int i = 0; i < this.inventorySize; i++){
            if(handler.getSlot(i).getStack() != null) {
                slotNbt.add(getSlotUniqueString(handler.getSlot(i)));
            }
        }
    }

    private String getSlotUniqueString(@NotNull Slot slot){
        return slot.getStack().getNbt() == null ? slot.getStack().getName().toString() : slot.getStack().getNbt().toString();
    }


}
