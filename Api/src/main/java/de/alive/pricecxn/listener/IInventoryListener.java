package de.alive.pricecxn.listener;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface IInventoryListener {
    @NotNull
    Mono<Boolean> hadItemsChangeAsync(@NotNull MinecraftClient client, ScreenHandler handler);

    @NotNull
    Mono<Void> initSlotsAsync(ScreenHandler handler);
}
