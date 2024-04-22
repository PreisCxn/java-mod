package de.alive.pricecxn.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.alive.pricecxn.interfaces.IMinecraftClient;
import de.alive.pricecxn.interfaces.IScreenHandler;
import de.alive.pricecxn.interfaces.ISlot;
import de.alive.pricecxn.PriceCxn;
import de.alive.pricecxn.cytooxien.ICxnConnectionManager;
import de.alive.pricecxn.cytooxien.ICxnListener;
import de.alive.pricecxn.cytooxien.Modes;
import de.alive.pricecxn.impl.MinecraftClientImpl;
import de.alive.pricecxn.impl.ScreenHandlerImpl;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.networking.Http;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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
        PriceCxn.getMod().runOnEndClientTick(client -> {
            if (active != null && Arrays.stream(active).anyMatch(bool -> !bool.get())) return;
            if (client.isPlayerNull()) return;
            if (client.isCurrentScreenNull()) return;

            Mono<Void> mono = Mono.empty();
            if (this.isOpen && !(client.isCurrentScreenInstanceOfHandledScreen())) {
                this.isOpen = false;
                mono = mono.then(onInventoryClose(client, client.getScreenHandler())).then();
            }

            if (client.isCurrentScreenNull()) {
                mono.subscribe();
                return;
            }
            if (client.isCurrentScreenTitleNull() || client.getCurrentScreenTitle().isEmpty()) {
                mono.subscribe();
                return;
            }

            if (!this.isOpen && client.isCurrentScreenInstanceOfHandledScreen() && isInventoryTitle(client, inventoryTitles.getData())) {
                if (!(client.player.currentScreenHandler.getSlot(0).inventory.size() == inventorySize)) return;
                IScreenHandler handler = new ScreenHandlerImpl(client.player.currentScreenHandler);
                mono.then(initSlotsAsync(handler)
                                 .doOnSuccess((a) -> {
                                     this.isOpen = true;
                                     lastUpdate = System.currentTimeMillis();
                                 }).then(onInventoryOpen(new MinecraftClientImpl(client), handler)))
                        .subscribe();
                return;
            }

            mono.then(
                    hadItemsChangeAsync(new MinecraftClientImpl(client), new ScreenHandlerImpl(client.player.currentScreenHandler))
                            .flatMap(hasChanged -> {
                                if (hasChanged) {
                                    lastUpdate = System.currentTimeMillis();
                                    return onInventoryUpdate(new MinecraftClientImpl(client), new ScreenHandlerImpl(client.player.currentScreenHandler));
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
    protected abstract Mono<Void> onInventoryOpen(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler);

    /**
     * This method is called when the inventory is closed
     *
     * @param client  The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract Mono<Void> onInventoryClose(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler);

    /**
     * This method is called when the inventory is updated
     *
     * @param client  The MinecraftClient
     * @param handler The ScreenHandler
     */
    protected abstract Mono<Void> onInventoryUpdate(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler);

    private boolean isInventoryTitle(@NotNull IMinecraftClient client, @Nullable List<String> inventoryTitles) {
        if (client.isCurrentScreenNull()) return false;
        if (inventoryTitles == null) return false;

        for (String title : inventoryTitles) {

            if (title.contains("--##--")) {
                String[] split = title.split("--##--");
                boolean allContained = true;
                for (String s : split) {
                    if (!client.containsInTitle(s)) {
                        allContained = false;
                        break;
                    }
                }
                return allContained;
            }

            if (client.equalsTitle(title))
                return true;
        }

        return false;
    }

    private boolean hadItemsChange(@NotNull IMinecraftClient client, @Nullable IScreenHandler handler) {
        if (lastUpdate + REFRESH_INTERVAL > System.currentTimeMillis()) return false;
        if (client.isPlayerNull()) return false;
        if (handler == null) return false;
        if (!isInventoryTitle(client, inventoryTitles.getData())) return false;
        if (!(client.getInventorySize() == inventorySize)) return false;

        for (int i = 0; i < this.inventorySize; i++) {

            if (!handler.getSlot(i).isStackNull() && !slotNbt.contains(getSlotUniqueHash(handler.getSlot(i)))) {
                initSlots(handler);
                return true;
            }

        }

        return false;
    }

    @Override
    public @NotNull Mono<Boolean> hadItemsChangeAsync(@NotNull IMinecraftClient client, IScreenHandler handler) {
        return Mono.fromSupplier(() -> hadItemsChange(client, handler))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public @NotNull Mono<Void> initSlotsAsync(IScreenHandler handler) {
        return Mono.fromRunnable(() -> initSlots(handler));
    }

    private void initSlots(@Nullable IScreenHandler handler) {
        if (handler == null) return;

        this.slotNbt.clear();

        for (int i = 0; i < this.inventorySize; i++) {
            if (!handler.getSlot(i).isStackNull()) {
                slotNbt.add(getSlotUniqueHash(handler.getSlot(i)));
            }
        }
    }

    private int getSlotUniqueHash(@NotNull ISlot slot) {
        return slot.isStackNbtNull() ? slot.stackNameHash() : slot.stackNbtHash();
    }

    protected @NotNull Mono<Void> sendData(@NotNull String datahandlerUri, @Nullable IMinecraftClient instance, @NotNull JsonElement data) {
        ICxnListener listener = PriceCxn.getMod().getCxnListener();

        if (instance == null || instance.isPlayerNull()) {
            return Mono.error(new NullPointerException("Instance or player is null"));
        }

        String uuid = instance.getPlayerUuidAsString();
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
                obj.addProperty("username", instance.getPlayerNameString());
                obj.add("data", data);
                return Http.getInstance().POST("/datahandler/" + uri, obj).then();
            } else
                return Mono.error(new NullPointerException("Not connected"));
        }));
    }

    protected @NotNull Mono<Void> sendData(@NotNull String datahandlerUri, @NotNull JsonElement data) {
        return sendData(datahandlerUri, new MinecraftClientImpl(MinecraftClient.getInstance()), data);
    }

}
