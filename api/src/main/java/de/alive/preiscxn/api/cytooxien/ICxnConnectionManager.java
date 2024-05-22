package de.alive.preiscxn.api.cytooxien;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface ICxnConnectionManager {

    @NotNull
    Mono<Tuple2<Boolean, ActionNotification>> checkConnectionAsync(ICxnConnectionManager.Refresh refresh);

    @NotNull
    Mono<Tuple2<Boolean, ActionNotification>> checkConnection(ICxnConnectionManager.Refresh refresh);

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
