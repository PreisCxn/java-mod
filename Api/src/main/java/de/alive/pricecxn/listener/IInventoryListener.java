package de.alive.pricecxn.listener;

import de.alive.pricecxn.interfaces.IMinecraftClient;
import de.alive.pricecxn.interfaces.IScreenHandler;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface IInventoryListener {
    @NotNull
    Mono<Boolean> hadItemsChangeAsync(@NotNull IMinecraftClient client, IScreenHandler handler);

    @NotNull
    Mono<Void> initSlotsAsync(IScreenHandler handler);
}
