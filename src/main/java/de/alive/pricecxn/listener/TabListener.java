package de.alive.pricecxn.listener;

import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.PriceCxnModClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TabListener {

    private String notInValue;
    private final int refreshesAfterJoinEvent = getRefreshesAfterJoinEvent();

    private static final int MAX_REFRESH = 15;

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

            refreshAsync(this.notInValue, refreshesAfterJoinEvent).thenRun(this::onJoinEvent);
        });
    }

    public boolean refresh(@Nullable String notInValue) {
        System.out.println("refresh");
        InGameHud gameHud = MinecraftClient.getInstance().inGameHud;
        if (gameHud == null) return false;
        PlayerListHud playerListHud = gameHud.getPlayerListHud();
        if (playerListHud == null) return false;

        System.out.println("refresh2");

        AtomicBoolean found = new AtomicBoolean(false);

        Arrays.stream(playerListHud.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .map(field -> {
                    try {
                        return field.get(playerListHud);
                    } catch (IllegalAccessException e) {
                        System.out.println("error");
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(value -> {
                    this.searches.getData().stream()
                            .filter(search -> value.toString().contains(search))
                            .forEach(search -> {
                                if (notInValue != null && value.toString().toLowerCase().contains(notInValue.toLowerCase()))
                                    return;
                                this.handleData(value.toString());
                                found.set(true);
                            });
                });

        return found.get();
    }

    public CompletableFuture<Void> refreshAsync(@Nullable String notInValue, @Nullable int maxRefresh) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        AtomicInteger attempts = new AtomicInteger(0);

        int finalMaxRefresh = maxRefresh <= 0 ? TabListener.MAX_REFRESH : maxRefresh;
        Runnable refreshTask = new Runnable() {
            @Override
            public void run() {
                System.out.println("try");
                if (refresh(notInValue)) {
                    System.out.println("found : " + notInValue);
                    future.complete(null);
                    return;
                }

                if (attempts.incrementAndGet() >= finalMaxRefresh) {
                    future.complete(null);
                    return;
                }

                PriceCxnModClient.EXECUTOR_SERVICE.schedule(this, 200 + attempts.get() * 50L, TimeUnit.MILLISECONDS);
            }
        };

        PriceCxnModClient.EXECUTOR_SERVICE.schedule(refreshTask, 200, TimeUnit.MILLISECONDS);

        return future;
    }

    public CompletableFuture<Void> refreshAsync() {
        return refreshAsync(null, 0);
    }

    /**
     * After every Refresh this method is called with the data from the tab found to decide what to do with it
     *
     * @param data The String line from the tab
     */
    protected abstract void handleData(@NotNull String data);

    /**
     * Decide if the refresh method should be called after the player joins a server or change the server on a network
     *
     * @return True if the refresh method should be called
     */
    protected abstract boolean refreshAfterJoinEvent();

    public abstract void onJoinEvent();

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
