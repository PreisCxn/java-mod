package de.alive.preiscxn.api.listener;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.interfaces.VersionedTabGui;
import de.alive.preiscxn.api.networking.DataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TabListener {

    private static final int MAX_REFRESH = 15;
    private final int refreshesAfterJoinEvent = getRefreshesAfterJoinEvent();
    private String notInValue;
    private DataAccess searches;

    public TabListener(@NotNull DataAccess searches) {
        this.searches = searches;
        init();
    }

    private void init() {
        PriceCxn.getMod().runOnJoin((client) -> {
            if (client == null) return;
            if (client.isCurrentServerEntryNull()) return;
            if (!refreshAfterJoinEvent()) return;

            refreshAsync(this.notInValue, refreshesAfterJoinEvent)
                    .then(this.onJoinEvent())
                    .subscribe();
        });
    }

    public @NotNull Mono<Void> refreshAsync() {
        return refreshAsync(null, 0);
    }

    public @NotNull Mono<Void> refreshAsync(@Nullable String notInValue, int maxRefresh) {
        return refreshAsync(notInValue, maxRefresh, new AtomicInteger());
    }

    private @NotNull Mono<Void> refreshAsync(@Nullable String notInValue, int maxRefresh, @NotNull AtomicInteger attempts) {
        int finalMaxRefresh = maxRefresh <= 0 ? TabListener.MAX_REFRESH : maxRefresh;

        return refresh(notInValue)
                .doOnNext(aBoolean -> PriceCxn.getMod().getLogger().debug("Refreshed: " + aBoolean))
                .filter(aBoolean -> !aBoolean)
                .filter(aBoolean -> attempts.incrementAndGet() < finalMaxRefresh)
                .flatMap(refresh -> Mono.delay(Duration.ofMillis(500 + attempts.get() * 50L)))
                .flatMap(unused -> refreshAsync(notInValue, finalMaxRefresh, attempts));
    }

    private @NotNull Mono<Boolean> refresh(@Nullable String notInValue) {
        PriceCxn.getMod().getLogger().debug("refresh");

        VersionedTabGui gameHud = PriceCxn.getMod().getVersionedTabGui();
        if (gameHud == null) return Mono.just(false);


        return Flux.fromIterable(List.of(gameHud.priceCxn$getHeader(), gameHud.priceCxn$getFooter()))
                .doOnNext(value -> PriceCxn.getMod().getLogger().debug("Value: " + value))
                .filter(value -> value != null && !value.isEmpty())
                .flatMap(value -> Flux.fromIterable(this.searches.getData().getData())
                        .filter(value::contains)
                        .filter(search -> !(notInValue != null && value.toLowerCase().contains(notInValue.toLowerCase())))
                        .flatMap(search -> this.handleData(value).then(Mono.just(search)))
                )
                .any(string -> true);
    }

    /**
     * After every Refresh this method is called with the data from the tab found to decide what to do with it
     *
     * @param data The String line from the tab
     */
    protected abstract Mono<Void> handleData(@NotNull String data);

    /**
     * Decide if the refresh method should be called after the player joins a server or change the server on a network
     *
     * @return True if the refresh method should be called
     */
    protected abstract boolean refreshAfterJoinEvent();

    public abstract Mono<Void> onJoinEvent();

    protected int getRefreshesAfterJoinEvent() {
        return MAX_REFRESH / 2;
    }

    public List<String> getSearches() {
        return searches.getData().getData();
    }

    public void setDataAccess(DataAccess searches) {
        this.searches = searches;
    }

    public void setNotInValue(String notInValue) {
        this.notInValue = notInValue;
    }

}
