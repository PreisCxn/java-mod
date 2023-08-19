package de.alive.pricecxn.cytooxien.listener;

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

public abstract class InventoryListener {

    private final List<String> inventoryTitles;
    private final int inventorySize; //Anzahl an Slots

    private final List<Slot> slots = new ArrayList<>();

    private boolean isOpen = false;

    /**
     *  This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize The size of the inventories to listen to (in slots)
     */
    public InventoryListener(@NotNull List<String> inventoryTitles, int inventorySize){

        this.inventorySize = inventorySize;
        this.inventoryTitles = inventoryTitles;

        init();
    }

    //setup of Listeners
    private void init(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (client.player.currentScreenHandler == null) return;

            if(!this.isOpen && client.currentScreen instanceof HandledScreen && isInventoryTitle(client, inventoryTitles)){
                ScreenHandler handler = client.player.currentScreenHandler;
                initSlots(handler);
                this.isOpen = true;
                onInventoryOpen(client, handler);
            }

            if(hadItemsChange(client, client.player.currentScreenHandler)) {
                onInventoryUpdate(client, client.player.currentScreenHandler);
            }

            if(this.isOpen && !(client.currentScreen instanceof HandledScreen)) {
                this.isOpen = false;
                onInventoryClose(client, client.player.currentScreenHandler);
            }

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
        if(!isInventoryTitle(client, inventoryTitles)) return false;

        for (int i = 0; i < this.inventorySize; i++){
            if(!Objects.equals(slots.get(i).getStack().getName().toString(), handler.getSlot(i).getStack().getName().toString())){
                initSlots(handler);
                return true;
            }
        }

        return false;
    }

    private void initSlots(@Nullable ScreenHandler handler){
        if(handler == null) return;

        this.slots.clear();

        for (int i = 0; i < this.inventorySize; i++){
            slots.add(handler.getSlot(i));
        }
    }


}
