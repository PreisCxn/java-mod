package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.cytooxien.dataobservers.AuctionHouseListener;
import de.alive.pricecxn.cytooxien.dataobservers.ItemShopListener;
import de.alive.pricecxn.cytooxien.dataobservers.TomNookListener;
import de.alive.pricecxn.cytooxien.dataobservers.TradeListener;
import de.alive.pricecxn.listener.ServerListener;
import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.ServerChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.LOGGER;

public class CxnListener extends ServerListener {

    private static final List<String> DEFAULT_IPS = List.of("cytooxien");
    private static final List<String> DEFAULT_IGNORED_IPS = List.of("beta");
    private final @NotNull ThemeServerChecker themeChecker;
    private final @NotNull ServerChecker serverChecker;
    private final @NotNull CxnDataHandler dataHandler;
    private final @NotNull CxnConnectionManager connectionManager;

    public CxnListener() {
        super(DEFAULT_IPS, DEFAULT_IGNORED_IPS);

        //setting up server checker
        this.serverChecker = new ServerChecker();

        //setting up theme checker and listeners
        this.themeChecker = new ThemeServerChecker(this, this.isOnServer());

        this.dataHandler = new CxnDataHandler(serverChecker, themeChecker);
        AtomicBoolean listenerActive = new AtomicBoolean(false);
        this.connectionManager = new CxnConnectionManager(dataHandler, serverChecker, themeChecker, listenerActive);

        new AuctionHouseListener(this.isOnServer(), listenerActive);
        new ItemShopListener(this.isOnServer(), listenerActive);
        new TomNookListener(this.isOnServer(), listenerActive);
        new TradeListener(this.isOnServer(), listenerActive);

        //checking connection and activating mod
        connectionManager.checkConnectionAsync(false)
                .doOnSuccess((a) -> LOGGER.info("Mod active? {}", connectionManager.isActive()))
                .subscribe();

    }

    @Override
    public @NotNull Mono<Void> onTabChange() {
        if (!this.isOnServer().get())
            return Mono.empty();

        return dataHandler.refreshItemData("pricecxn.data.item_data", false)
                .then(dataHandler.refreshItemData("pricecxn.data.nook_data", true))
                .then();

    }

    @Override
    public @NotNull Mono<Void> onJoinEvent() {
        if (!this.isOnServer().get())
            return Mono.empty();
        boolean activeBackup = connectionManager.isActive();

        return connectionManager.checkConnectionAsync(true)
                .flatMap(messageInformation -> {
                    CxnConnectionManager.sendConnectionInformation(messageInformation.getLeft(), messageInformation.getRight());
                    if (activeBackup)
                        return dataHandler.refreshData(false);
                    return Mono.empty();
                });
    }

    @Override
    public @NotNull Mono<Void> onServerJoin() {

        return connectionManager.checkConnectionAsync(true)
                .doOnSuccess(messageInformation -> CxnConnectionManager.sendConnectionInformation(messageInformation.getLeft(), messageInformation.getRight(), true))
                .then();

    }

    @Override
    public void onServerLeave() {
        LOGGER.debug("Cytooxien left : {}", this.isOnServer().get());
        connectionManager.deactivate();
    }

    public @NotNull CxnConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public DataHandler getData(String key) {
        return dataHandler.get(key);
    }

    public @NotNull ServerChecker getServerChecker() {
        return serverChecker;
    }

    public @NotNull ThemeServerChecker getThemeChecker() {
        return themeChecker;
    }


    public @Nullable List<String> getModUsers() {
        return dataHandler.getModUsers();
    }

    public boolean isActive() {
        return connectionManager.isActive();
    }

}
