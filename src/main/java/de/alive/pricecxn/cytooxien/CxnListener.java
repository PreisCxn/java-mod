package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonArray;
import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.cytooxien.dataobservers.AuctionHouseListener;
import de.alive.pricecxn.cytooxien.dataobservers.ItemShopListener;
import de.alive.pricecxn.cytooxien.dataobservers.TomNookListener;
import de.alive.pricecxn.cytooxien.dataobservers.TradeListener;
import de.alive.pricecxn.listener.ServerListener;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.NetworkingState;
import de.alive.pricecxn.networking.ServerChecker;
import de.alive.pricecxn.networking.sockets.WebSocketCompletion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.LOGGER;

public class CxnListener extends ServerListener {

    private static final List<String> DEFAULT_IPS = List.of("cytooxien");
    private static final List<String> DEFAULT_IGNORED_IPS = List.of("beta");
    private final @NotNull ThemeServerChecker themeChecker;
    private final @NotNull ServerChecker serverChecker;
    private final CxnDataHandler dataHandler;
    NetworkingState state = NetworkingState.OFFLINE;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private @Nullable Boolean isRightVersion = null;
    private final AtomicBoolean listenerActive = new AtomicBoolean(false);

    public CxnListener() {
        super(DEFAULT_IPS, DEFAULT_IGNORED_IPS);

        //setting up server checker
        this.serverChecker = new ServerChecker();

        //setting up theme checker and listeners
        this.themeChecker = new ThemeServerChecker(this, this.isOnServer());

        this.dataHandler = new CxnDataHandler(serverChecker, themeChecker);

        new AuctionHouseListener(this.isOnServer(), listenerActive);
        new ItemShopListener(this.isOnServer(), listenerActive);
        new TomNookListener(this.isOnServer(), listenerActive);
        new TradeListener(this.isOnServer(), listenerActive);

        //checking connection and activating mod
        checkConnectionAsync(false)
                .doOnSuccess((a) -> LOGGER.info("Mod active?" + this.active.get()))
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
        boolean activeBackup = this.active.get();

        return checkConnectionAsync(true)
                .flatMap(messageInformation -> {
                    CxnConnectionManager.sendConnectionInformation(messageInformation.getLeft(), messageInformation.getRight());
                    if (activeBackup)
                        return dataHandler.refreshData(false);
                    return Mono.empty();
                });
    }

    @Override
    public @NotNull Mono<Void> onServerJoin() {

        return checkConnectionAsync(true)
                .doOnSuccess(messageInformation -> CxnConnectionManager.sendConnectionInformation(messageInformation.getLeft(), messageInformation.getRight(), true))
                .then();

    }

    @Override
    public void onServerLeave() {
        LOGGER.debug("Cytooxien left : " + this.isOnServer().get());
        deactivate();
    }


    private @NotNull Mono<Void> activate(boolean themeRefresh) {
        if (this.active.get()) return Mono.empty(); //return wenn schon aktiviert

        return dataHandler.initData()
                .then(dataHandler.refreshData(true))
                .then(Mono.just(this.themeChecker))
                .filter(themeServerChecker -> themeRefresh)
                .flatMap(ThemeServerChecker::refreshAsync)
                .doOnSuccess(ignored -> {
                    activateListeners();
                    this.active.set(true);
                    isRightVersion = true;
                })
                .onErrorResume(ex -> {
                    LOGGER.error("Error while activating mod", ex);
                    deactivate();
                    return Mono.error(ex);
                })
                .then();
    }

    private void createTranslationHandler(@NotNull List<String> langList) {


    }

    private void deactivate() {
        if (!this.active.get()) return; //return wenn schon deaktiviert

        deactivateListeners();
        this.active.set(false);
    }

    public DataHandler getData(String key) {
        return dataHandler.get(key);
    }

    public @NotNull ServerChecker getServerChecker() {
        return serverChecker;
    }

    private void activateListeners() {
        this.listenerActive.set(true);
    }

    private void deactivateListeners() {
        this.listenerActive.set(false);
    }

    public @NotNull ThemeServerChecker getThemeChecker() {
        return themeChecker;
    }

    /**
     * Gibt zurück, ob die Min-Version des Servers die aktuelle Version der Mod erfüllt.
     * (Mod Version > Server Min-Version -> true)
     */
    private @NotNull Mono<Boolean> isMinVersion() {
        return this.serverChecker.getServerMinVersion()
                .map(serverMinVersion -> PriceCxnMod.getIntVersion(PriceCxnMod.MOD_VERSION)
                        .filter(value -> PriceCxnMod.getIntVersion(serverMinVersion)
                                .filter(integer -> value >= integer)
                                .isPresent())
                        .isPresent());
    }

    private @NotNull Mono<Boolean> isSpecialUser() {
        if (MinecraftClient.getInstance().player == null)
            return Mono.just(false);

        return new WebSocketCompletion(serverChecker.getWebsocket(), "isSpecialUser", MinecraftClient.getInstance().player.getUuidAsString()).getMono()
                .map(s -> s.equals("true"))
                .onErrorReturn(false);
    }

    private @NotNull Mono<Pair<Boolean, ActionNotification>> checkConnection(boolean themeRefresh) {
        boolean activeCache = this.active.get();
        Boolean isRightVersionBackup = isRightVersion;
        NetworkingState stateBackup = this.state;

        return Mono.zip(serverChecker.isConnected(), isMinVersion(), this.serverChecker.getServerMinVersion())
                .flatMap(tuple3 -> {
                    boolean isConnected = tuple3.getT1();
                    boolean isMinVersion = tuple3.getT2();
                    String serverMinVersion = tuple3.getT3();

                    this.state = serverChecker.getState();
                    if (!isConnected) {
                        // Server nicht erreichbar
                        this.deactivate();

                        LOGGER.info("Server nicht erreichbar");
                        return Mono.just(new Pair<>(activeCache, ActionNotification.SERVER_OFFLINE));
                    } else if (!isMinVersion) {
                        // Version nicht korrekt
                        this.deactivate();

                        ActionNotification.WRONG_VERSION.setTextVariables(serverMinVersion);

                        LOGGER.info(isRightVersionBackup + " " + serverMinVersion);
                        this.isRightVersion = false;
                        return Mono.just(new Pair<>(isRightVersionBackup == null || isRightVersionBackup, ActionNotification.WRONG_VERSION));
                    } else {
                        NetworkingState serverCheckerState = serverChecker.getState();

                        if (serverCheckerState == NetworkingState.ONLINE) {
                            // Server im Online-Modus
                            LOGGER.info("Server im Online-Modus");
                            return this.activate(themeRefresh)
                                    .then(Mono.just(new Pair<>(!activeCache, ActionNotification.MOD_STARTED)));

                        } else if (serverCheckerState == NetworkingState.MAINTENANCE) {
                            return isSpecialUser()
                                    .flatMap(isSpecialUser -> {
                                        if (isSpecialUser) {
                                            // Benutzer hat Berechtigung
                                            LOGGER.info("Benutzer hat Berechtigung");
                                            return this.activate(themeRefresh).then(
                                                    Mono.just(new Pair<>(stateBackup != NetworkingState.MAINTENANCE || !activeCache, ActionNotification.SERVER_MAINTEANCE_WITH_PERMISSON)));
                                        } else {
                                            // Benutzer hat keine Berechtigung
                                            LOGGER.info("Benutzer hat keine Berechtigung");
                                            this.deactivate();
                                            return Mono.just(new Pair<>(stateBackup != NetworkingState.MAINTENANCE, ActionNotification.SERVER_MAINTENANCE));
                                        }
                                    });
                        } else {
                            // Server im Offline-Modus
                            LOGGER.info("Server im Offline-Modus");
                            this.deactivate();
                            return Mono.just(new Pair<>(activeCache, ActionNotification.SERVER_OFFLINE));
                        }
                    }
                });
    }

    public @NotNull Mono<Pair<Boolean, ActionNotification>> checkConnectionAsync(boolean themeRefresh) {
        return Mono.fromCallable(() -> this.checkConnection(themeRefresh))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> result);
    }

    public @NotNull Optional<List<String>> getModUsers() {
        List<String> stringList = new ArrayList<>();

        JsonArray array;

        try {
            array = this.dataHandler.get("pricecxn.data.mod_users").getDataArray();

            if (array == null) return Optional.empty();

            array.asList().forEach(element -> {
                if (!element.isJsonNull())
                    stringList.add(element.getAsString());
            });

            if (stringList.isEmpty()) return Optional.empty();

            return Optional.of(stringList);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public @NotNull AtomicBoolean isActive() {
        return active;
    }

}
