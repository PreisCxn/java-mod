package de.alive.preiscxn.api.listener;

import de.alive.preiscxn.api.utils.StringUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to check if the player is on a specific server.
 */
public abstract class ServerListener {

    private final AtomicBoolean onServer = new AtomicBoolean(Boolean.FALSE);
    private final List<String> ips;
    private final List<String> ignoredIps;

    public ServerListener(@NotNull String ip, @Nullable String ignoredIp) {
        this(List.of(ip), ignoredIp == null ? List.of() : List.of(ignoredIp));
    }

    public ServerListener(@NotNull List<String> ips, @Nullable List<String> ignoredIps) {
        this.ips = StringUtil.listToLowerCase(ips);
        this.ignoredIps = ignoredIps == null ? new ArrayList<>() : StringUtil.listToLowerCase(ignoredIps);

        init();
    }

    private void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client == null) return;
            if (client.getCurrentServerEntry() == null) return;
            if (onServer.get()) return;

            String lowerCasedAddress = client.getCurrentServerEntry().address.toLowerCase();
            Flux.fromIterable(ips)
                    .filter(lowerCasedAddress::contains)
                    .filter(ip -> ignoredIps.stream().noneMatch(lowerCasedAddress::contains))
                    .next()
                    .doOnNext(s -> onServer.set(true))
                    .flatMap(ip -> onServerJoin())
                    .subscribe();

        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            boolean doMethod = this.onServer.get();
            this.onServer.set(false);
            if (doMethod) this.onServerLeave();
        });
    }

    public @NotNull Mono<Void> onTabChange() {
        return Mono.empty();
    }

    public @NotNull Mono<Void> onJoinEvent() {
        return Mono.empty();
    }

    /**
     * This method is called when the player joins the server
     */
    public abstract Mono<Void> onServerJoin();

    /**
     * This method is called when the player leaves the server
     */
    public abstract void onServerLeave();

    /**
     * Returns the AtomicBoolean that is used to check if the player is on the server.
     *
     * @return The AtomicBoolean
     */
    public @NotNull AtomicBoolean isOnServer() {
        return onServer;
    }

}
