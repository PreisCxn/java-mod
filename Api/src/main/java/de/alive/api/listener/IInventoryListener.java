package de.alive.api.listener;

import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IScreenHandler;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface IInventoryListener {
    @NotNull
    Mono<Boolean> hadItemsChangeAsync(@NotNull IMinecraftClient client, IScreenHandler handler);

    @NotNull
    Mono<Void> initSlotsAsync(IScreenHandler handler);
}
