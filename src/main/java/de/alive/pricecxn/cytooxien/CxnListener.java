package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.PriceCxn;
import de.alive.pricecxn.PriceCxnModClient;
import de.alive.pricecxn.interfaces.Mod;
import de.alive.pricecxn.listener.IInventoryListener;
import de.alive.pricecxn.listener.InventoryListener;
import de.alive.pricecxn.listener.ServerListener;
import de.alive.pricecxn.modules.ModuleLoader;
import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.IServerChecker;
import de.alive.pricecxn.networking.ServerChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.LogPrinter.LOGGER;

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
                    for (Class<? extends IInventoryListener> clazz : classes) {
                        LOGGER.info("Found listener: {}", clazz.getName());
                        try{
                            clazz.getConstructor(Mod.class, AtomicBoolean[].class)
                                    .newInstance(PriceCxn.getMod(), new AtomicBoolean[]{this.isOnServer(), listenerActive});
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

}
