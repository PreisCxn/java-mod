package de.alive.preiscxn.impl.cytooxien;

import de.alive.preiscxn.api.Mod;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.ICxnConnectionManager;
import de.alive.preiscxn.api.cytooxien.ICxnDataHandler;
import de.alive.preiscxn.api.cytooxien.ICxnListener;
import de.alive.preiscxn.api.cytooxien.IThemeServerChecker;
import de.alive.preiscxn.api.listener.InventoryListener;
import de.alive.preiscxn.api.listener.ServerListener;
import de.alive.preiscxn.api.module.ModuleLoader;
import de.alive.preiscxn.api.networking.DataHandler;
import de.alive.preiscxn.api.networking.IServerChecker;
import de.alive.preiscxn.impl.networking.ServerChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public class CxnListener extends ServerListener implements ICxnListener {

    private static final List<String> DEFAULT_IPS = List.of("cytooxien");
    private static final List<String> DEFAULT_IGNORED_IPS = List.of("beta");
    private final @NotNull IThemeServerChecker themeChecker;
    private final @NotNull IServerChecker serverChecker;
    private final @NotNull ICxnDataHandler dataHandler;
    private final @NotNull ICxnConnectionManager connectionManager;
    private final @NotNull AtomicBoolean listenerActive;

    public CxnListener() {
        super(DEFAULT_IPS, DEFAULT_IGNORED_IPS);

        //setting up server checker
        this.serverChecker = new ServerChecker();

        //setting up theme checker and listeners
        this.themeChecker = new ThemeServerChecker(this, this.isOnServer());

        this.dataHandler = new CxnDataHandler(serverChecker, themeChecker);
        this.listenerActive = new AtomicBoolean(false);
        this.connectionManager = new CxnConnectionManager(dataHandler, serverChecker, themeChecker, listenerActive);

        //checking connection and activating mod
        connectionManager.checkConnectionAsync(ICxnConnectionManager.Refresh.NONE)
                .doOnSuccess(a -> PriceCxn.getMod().getLogger().info("Mod active? {}", connectionManager.isActive()))
                .subscribe();

    }

    public void loadModules(ModuleLoader cxnListenerModuleLoader) {
        Set<Class<? extends InventoryListener>> classes = cxnListenerModuleLoader
                .loadInterfaces(InventoryListener.class);

        PriceCxn.getMod().getLogger().info("Found {} listeners", classes.size());

        for (Class<? extends InventoryListener> clazz : classes) {
            PriceCxn.getMod().getLogger().info("Found listener: {}", clazz.getName());
            try {
                InventoryListener inventoryListener = clazz.getConstructor(Mod.class, AtomicBoolean[].class)
                        .newInstance(PriceCxn.getMod(), new AtomicBoolean[]{this.isOnServer(), listenerActive});

                init(inventoryListener);
            } catch (Exception e) {
                PriceCxn.getMod().getLogger().error("Could not instantiate listener", e);
            }
        }
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
                    CxnConnectionManager.sendConnectionInformation(messageInformation.getT1(), messageInformation.getT2());
                    if (activeBackup)
                        return dataHandler.refreshData(false);
                    return Mono.empty();
                });
    }

    @Override
    public @NotNull Mono<Void> onServerJoin() {

        return connectionManager.checkConnectionAsync(ICxnConnectionManager.Refresh.THEME)
                .doOnSuccess(messageInformation ->
                        CxnConnectionManager
                                .sendConnectionInformation(
                                        messageInformation.getT1(),
                                        messageInformation.getT2(),
                                        true))
                .then();

    }

    @Override
    public void onServerLeave() {
        PriceCxn.getMod().getLogger().debug("Cytooxien left : {}", this.isOnServer().get());
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
