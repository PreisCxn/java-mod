package de.alive.pricecxn.listener;

import de.alive.pricecxn.networking.DataAccess;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client == null) return;
            if (client.getCurrentServerEntry() == null) return;
            if (!refreshAfterJoinEvent()) return;

            refreshAsync(this.notInValue, refreshesAfterJoinEvent, 0)
                    .then(this.onJoinEvent())
                    .subscribe();
        });
    }

    public Mono<Boolean> refresh(@Nullable String notInValue) {
        System.out.println("refresh");
        InGameHud gameHud = MinecraftClient.getInstance().inGameHud;
        if (gameHud == null) return Mono.just(false);
        PlayerListHud playerListHud = gameHud.getPlayerListHud();
        if (playerListHud == null) return Mono.just(false);

        System.out.println("refresh2");

        AtomicBoolean found = new AtomicBoolean(false);

        Flux<Void> flux = Flux.fromArray(playerListHud.getClass().getDeclaredFields())
                .doOnNext(field -> field.setAccessible(true))
                .mapNotNull(field -> {
                    try{
                        return field.get(playerListHud);
                    }catch(IllegalAccessException e){
                        System.out.println("error");
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(value -> Flux.fromIterable(this.searches.getData())
                        .filter(search -> value.toString().contains(search))
                        .flatMap(search -> {
                            if (notInValue != null && value.toString().toLowerCase().contains(notInValue.toLowerCase())) {
                                return Mono.empty();
                            }
                            found.set(true);
                            return this.handleData(value.toString());
                        })
                );

        return flux.then(Mono.just(found.get()));
    }

    public Mono<Void> refreshAsync(@Nullable String notInValue, int maxRefresh, int currentRefresh) {
        AtomicInteger attempts = new AtomicInteger(currentRefresh);
        int finalMaxRefresh = maxRefresh <= 0 ? TabListener.MAX_REFRESH : maxRefresh;

        return refresh(notInValue)
                .flatMap(refresh -> {
                    if (refresh) {
                        return Mono.empty();
                    }

                    if (attempts.incrementAndGet() >= finalMaxRefresh) {
                        return Mono.empty();
                    }

                    return Mono.delay(Duration.ofMillis(200 + attempts.get() * 50L))
                            .then(refreshAsync(notInValue, maxRefresh, attempts.get()));
                });
    }

    public Mono<Void> refreshAsync() {
        return refreshAsync(null, 0, 0);
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
        return searches.getData();
    }

    public void setDataAccess(DataAccess searches) {
        this.searches = searches;
    }

    public void setNotInValue(String notInValue) {
        this.notInValue = notInValue;
    }

}
