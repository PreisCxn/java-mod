package de.alive.api.cytooxien;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface IThemeServerChecker {

    @NotNull
    Modes getMode();

    Mono<Void> refreshAsync();

}
