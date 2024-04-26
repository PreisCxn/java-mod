package de.alive.pricecxn.cytooxien;

import de.alive.api.Mod;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.ICxnConnectionManager;
import de.alive.api.cytooxien.ICxnDataHandler;
import de.alive.api.cytooxien.ICxnListener;
import de.alive.api.cytooxien.IThemeServerChecker;
import de.alive.api.listener.InventoryListener;
import de.alive.api.listener.ServerListener;
import de.alive.api.networking.DataHandler;
import de.alive.api.networking.IServerChecker;
import de.alive.pricecxn.modules.ModuleLoader;
import de.alive.pricecxn.networking.ServerChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.api.LogPrinter.LOGGER;

public class CxnListener extends ServerListener implements ICxnListener {

    private static final List<String> DEFAULT_IPS = List.of("cytooxien");
    private static final List<String> DEFAULT_IGNORED_IPS = List.of("beta");
    private final @NotNull IThemeServerChecker themeChecker;
    private final @NotNull IServerChecker serverChecker;
    private final @NotNull ICxnDataHandler dataHandler;
    private final @NotNull ICxnConnectionManager connectionManager;

    public CxnListener(ModuleLoader cxnListenerModuleLoader) {
        super(DEFAULT_IPS, DEFAULT_IGNORED_IPS);

        //setting up server checker
        this.serverChecker = new ServerChecker();

        //setting up theme checker and listeners
        this.themeChecker = new ThemeServerChecker(this, this.isOnServer());

        this.dataHandler = new CxnDataHandler(serverChecker, themeChecker);
        AtomicBoolean listenerActive = new AtomicBoolean(false);
        this.connectionManager = new CxnConnectionManager(dataHandler, serverChecker, themeChecker, listenerActive);

        cxnListenerModuleLoader
                .loadInterfaces(InventoryListener.class)
                .flatMap(classes -> {
                    for (Class<? extends InventoryListener> clazz : classes) {
                        LOGGER.info("Found listener: {}", clazz.getName());
                        try{
                            InventoryListener inventoryListener = clazz.getConstructor(Mod.class, AtomicBoolean[].class)
                                    .newInstance(PriceCxn.getMod(), new AtomicBoolean[]{this.isOnServer(), listenerActive});

                            init(inventoryListener);
                        }catch(Exception e){
                            LOGGER.error("Could not instantiate listener", e);
                        }
                    }
                    return Mono.empty();
                }).subscribe();

        //checking connection and activating mod
        connectionManager.checkConnectionAsync(ICxnConnectionManager.Refresh.NONE)
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

        return connectionManager.checkConnectionAsync(ICxnConnectionManager.Refresh.THEME)
                .flatMap(messageInformation -> {
                    CxnConnectionManager.sendConnectionInformation(messageInformation.getLeft(), messageInformation.getRight());
                    if (activeBackup)
                        return dataHandler.refreshData(false);
                    return Mono.empty();
                });
    }

    @Override
    public @NotNull Mono<Void> onServerJoin() {

        return connectionManager.checkConnectionAsync(ICxnConnectionManager.Refresh.THEME)
                .doOnSuccess(messageInformation -> CxnConnectionManager.sendConnectionInformation(messageInformation.getLeft(), messageInformation.getRight(), true))
                .then();

    }

    @Override
    public void onServerLeave() {
        LOGGER.debug("Cytooxien left : {}", this.isOnServer().get());
        connectionManager.deactivate();
    }

    @Override
    public @NotNull ICxnConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public DataHandler getData(String key) {
        return dataHandler.get(key);
    }

    @Override
    public @NotNull IServerChecker getServerChecker() {
        return serverChecker;
    }

    @Override
    public @NotNull IThemeServerChecker getThemeChecker() {
        return themeChecker;
    }


    @Override
    public @Nullable List<String> getModUsers() {
        return dataHandler.getModUsers();
    }

    @Override
    public boolean isActive() {
        return connectionManager.isActive();
    }

    //setup of Listeners
    private void init(InventoryListener inventoryListener) {
        PriceCxn.getMod().runOnEndClientTick(client -> inventoryListener.onTick().subscribe());
    }
}
