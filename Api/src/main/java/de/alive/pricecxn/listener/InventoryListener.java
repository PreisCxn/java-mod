package de.alive.pricecxn.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.alive.pricecxn.PriceCxn;
import de.alive.pricecxn.cytooxien.ICxnConnectionManager;
import de.alive.pricecxn.cytooxien.ICxnListener;
import de.alive.pricecxn.cytooxien.Modes;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.networking.Http;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class InventoryListener implements IInventoryListener {
    static final int REFRESH_INTERVAL = 200;

    private final @NotNull DataAccess inventoryTitles;
    private final int inventorySize; //Anzahl an Slots
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

            Mono<Void> mono = Mono.empty();
            if (this.isOpen && !(client.currentScreen instanceof HandledScreen)) {
                this.isOpen = false;
                mono = mono.then(onInventoryClose(client, client.player.currentScreenHandler)).then();
            }

            if (client.currentScreen == null) {
                mono.subscribe();
                return;
            }
            if (client.currentScreen.getTitle().getString() == null || client.currentScreen.getTitle().getString().isEmpty()) {
                mono.subscribe();
                return;
            }

            if (!this.isOpen && client.currentScreen instanceof HandledScreen && isInventoryTitle(client, inventoryTitles.getData())) {
                if (!(client.player.currentScreenHandler.getSlot(0).inventory.size() == inventorySize)) return;
                ScreenHandler handler = client.player.currentScreenHandler;
                mono.then(initSlotsAsync(handler)
                                 .doOnSuccess((a) -> {
                                     this.isOpen = true;
                                     lastUpdate = System.currentTimeMillis();
                                 }).then(onInventoryOpen(client, handler)))
                        .subscribe();
                return;
            }

            mono.then(
                    hadItemsChangeAsync(client, client.player.currentScreenHandler)
                            .flatMap(hasChanged -> {
                                if (hasChanged) {
                                    lastUpdate = System.currentTimeMillis();
                                    return onInventoryUpdate(client, client.player.currentScreenHandler);
                                }
                                return Mono.empty();
                            })
            ).subscribe();

        });
    }

    /**
     * This method is called when the inventory is opened
     *
     * @param client  The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract Mono<Void> onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    /**
     * This method is called when the inventory is closed
     *
     * @param client  The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract Mono<Void> onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    /**
     * This method is called when the inventory is updated
     *
     * @param client  The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract Mono<Void> onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler);

    private boolean isInventoryTitle(@NotNull MinecraftClient client, @Nullable List<String> inventoryTitles) {
        if (client.currentScreen == null) return false;
        if (inventoryTitles == null) return false;

        for (String title : inventoryTitles) {

            if (title.contains("--##--")) {
                String[] split = title.split("--##--");
                boolean allContained = true;
                for (String s : split) {
                    if (!client.currentScreen.getTitle().getString().contains(s)) {
                        allContained = false;
                        break;
                    }
                }
                return allContained;
            }

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
        if (!(client.player.currentScreenHandler.getSlot(0).inventory.size() == inventorySize)) return false;

        for (int i = 0; i < this.inventorySize; i++) {

            if (handler.getSlot(i).getStack() != null && !slotNbt.contains(getSlotUniqueHash(handler.getSlot(i)))) {
                initSlots(handler);
                return true;
            }

        }

        return false;
    }

    @Override
    public @NotNull Mono<Boolean> hadItemsChangeAsync(@NotNull MinecraftClient client, ScreenHandler handler) {
        return Mono.fromSupplier(() -> hadItemsChange(client, handler))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public @NotNull Mono<Void> initSlotsAsync(ScreenHandler handler) {
        return Mono.fromRunnable(() -> initSlots(handler));
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

    protected @NotNull Mono<Void> sendData(@NotNull String datahandlerUri, @Nullable MinecraftClient instance, @NotNull JsonElement data) {
        ICxnListener listener = PriceCxn.getMod().getCxnListener();

        if (instance == null || instance.player == null) {
            return Mono.error(new NullPointerException("Instance or player is null"));
        }

        String uuid = instance.player.getUuidAsString();
        JsonObject obj = new JsonObject();
        String uri = datahandlerUri.contains("/") ? datahandlerUri.replace("/", "") : datahandlerUri;

        return listener.getConnectionManager().checkConnectionAsync(ICxnConnectionManager.Refresh.THEME).then(Mono.defer(() -> {
            if (listener.isActive()) {
                Modes mode = listener.getThemeChecker().getMode();
                if (mode == Modes.NOTHING) {
                    return Mono.error(new NullPointerException("Mode is null"));
                }

                obj.addProperty("listener", uri);
                obj.addProperty("mode", mode.getTranslationKey());
                obj.addProperty("uuid", uuid);
                obj.addProperty("username", instance.player.getName().getString());
                obj.add("data", data);
                return Http.getInstance().POST("/datahandler/" + uri, obj).then();
            } else
                return Mono.error(new NullPointerException("Not connected"));
        }));
    }

    protected @NotNull Mono<Void> sendData(@NotNull String datahandlerUri, @NotNull JsonElement data) {
        return sendData(datahandlerUri, MinecraftClient.getInstance(), data);
    }

}
