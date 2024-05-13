package de.alive.api.cytooxien;

import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface ICxnConnectionManager {

    @NotNull
    Mono<Pair<Boolean, ActionNotification>> checkConnectionAsync(ICxnConnectionManager.Refresh refresh);

    @NotNull
    Mono<Pair<Boolean, ActionNotification>> checkConnection(ICxnConnectionManager.Refresh refresh);

    @NotNull
    Mono<Boolean> isMinVersion();

    @NotNull
    Mono<Boolean> isSpecialUser();

    @NotNull
    Mono<Void> activate(ICxnConnectionManager.Refresh refresh);

    void deactivate();

    boolean isActive();

    enum Refresh {
        NONE,
        THEME
    }
}
