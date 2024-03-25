package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.cytooxien.dataobservers.AuctionHouseListener;
import de.alive.pricecxn.cytooxien.dataobservers.ItemShopListener;
import de.alive.pricecxn.cytooxien.dataobservers.TomNookListener;
import de.alive.pricecxn.cytooxien.dataobservers.TradeListener;
import de.alive.pricecxn.listener.InventoryListener;
import de.alive.pricecxn.listener.ServerListener;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.NetworkingState;
import de.alive.pricecxn.networking.ServerChecker;
import de.alive.pricecxn.networking.sockets.WebSocketCompletion;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.alive.pricecxn.PriceCxnMod.LOGGER;
import static de.alive.pricecxn.PriceCxnMod.MOD_TEXT;

public class CxnListener extends ServerListener {

    private static final List<String> DEFAULT_IPS = List.of("cytooxien");
    private static final List<String> DEFAULT_IGNORED_IPS = List.of("beta");
    private final @NotNull ThemeServerChecker themeChecker;
    private final @NotNull List<InventoryListener> listeners;
    private final @NotNull ServerChecker serverChecker;
    private final Map<String, DataHandler> data = new HashMap<>();
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
        listeners = List.of(
                new AuctionHouseListener(this.isOnServer(), listenerActive),
                new ItemShopListener(this.isOnServer(), listenerActive),
                new TomNookListener(this.isOnServer(), listenerActive),
                new TradeListener(this.isOnServer(), listenerActive)
        );

        //checking connection and activating mod
        checkConnectionAsync(false)
                .doOnSuccess((a) -> LOGGER.info("Mod active?" + this.active.get()))
                .subscribe();

    }

    public static void sendConnectionInformation(@NotNull Pair<Boolean, ActionNotification> messageInformation, boolean force) {

        if (force || messageInformation.getLeft()) {
            if (MinecraftClient.getInstance().player != null) {
                ActionNotification message = messageInformation.getRight();

                MutableText msg;
                if (message.hasTextVariables()) {

                    msg = MOD_TEXT.copy()
                            .append(Text.translatable(message.getTranslationKey(), (Object[]) message.getTextVariables()))
                            .setStyle(PriceCxnMod.DEFAULT_TEXT);

                } else {

                    msg = MOD_TEXT.copy()
                            .append(Text.translatable(message.getTranslationKey()))
                            .setStyle(PriceCxnMod.DEFAULT_TEXT);

                }
                MinecraftClient.getInstance().player.sendMessage(msg);
            }
        }

    }

    public static void sendConnectionInformation(@NotNull Pair<Boolean, ActionNotification> messageInformation) {
        sendConnectionInformation(messageInformation, false);
    }

    @Override
    public @NotNull Mono<Void> onTabChange() {
        if (!this.isOnServer().get())
            return Mono.empty();

        return refreshItemData("pricecxn.data.item_data", false)
                .then(refreshItemData("pricecxn.data.nook_data", true))
                .then();

    }

    @Override
    public @NotNull Mono<Void> onJoinEvent() {
        if (!this.isOnServer().get())
            return Mono.empty();
        boolean activeBackup = this.active.get();

        return checkConnectionAsync()
                .flatMap(messageInformation -> {
                    sendConnectionInformation(messageInformation);
                    if (activeBackup)
                        return refreshData(false);
                    return Mono.empty();
                });
    }

    @Override
    public @NotNull Mono<Void> onServerJoin() {

        return checkConnectionAsync()
                .doOnSuccess(messageInformation -> CxnListener.sendConnectionInformation(messageInformation, true))
                .then();

    }

    @Override
    public void onServerLeave() {
        LOGGER.debug("Cytooxien left : " + this.isOnServer().get());
        deactivate();
    }

    private Mono<Void> refreshItemData(String dataKey, boolean isNook) {
        if (!this.data.containsKey(dataKey) || this.data.get(dataKey).getDataObject() == null) {

            if (this.themeChecker.getMode().equals(Modes.SKYBLOCK)) {
                data.put(dataKey, new DataHandler(serverChecker, "/datahandler/items/skyblock/true/" + (isNook ? "true" : "false"), DataHandler.ITEM_REFRESH_INTERVAL));
            } else if (this.themeChecker.getMode().equals(Modes.CITYBUILD)) {
                data.put(dataKey, new DataHandler(serverChecker, "/datahandler/items/citybuild/true/" + (isNook ? "true" : "false"), DataHandler.ITEM_REFRESH_INTERVAL));
            } else return Mono.empty();

        } else {
            JsonObject jsonObject = data.get(dataKey).getDataObject();
            if (jsonObject == null || !jsonObject.has("mode")) return Mono.empty();
            String mode = jsonObject.get("mode").getAsString();

            if (this.themeChecker.getMode().equals(Modes.SKYBLOCK) && !mode.equals(Modes.SKYBLOCK.getTranslationKey())) {
                data.get(dataKey).setUri("/datahandler/items/skyblock/true/" + (isNook ? "true" : "false"));
            } else if (this.themeChecker.getMode().equals(Modes.CITYBUILD) && !mode.equals(Modes.CITYBUILD.getTranslationKey())) {
                data.get(dataKey).setUri("/datahandler/items/citybuild/true/" + (isNook ? "true" : "false"));
            } else return Mono.empty();

        }

        return data.get(dataKey).refresh(true);
    }

    public @NotNull Mono<Void> activate(boolean themeRefresh) {
        if (this.active.get()) return Mono.empty(); //return wenn schon aktiviert

        return initData()
                .then(refreshData(true))
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

    private @NotNull Mono<Void> initData() {
        LOGGER.debug("initData");

        if (!this.data.containsKey("pricecxn.data.mod_users")) {
            data.put("pricecxn.data.mod_users", new DataHandler(serverChecker, "/datahandler/mod_users", DataHandler.MODUSER_REFRESH_INTERVAL));
        }

        if (this.data.containsKey("cxnprice.translation"))
            return Mono.empty();
        else
            return new WebSocketCompletion(serverChecker.getWebsocket(), "translationLanguages")
                    .getMono()
                    .map(StringUtil::stringToList)
                    .doOnSuccess(this::createTranslationHandler).then();
    }

    private void createTranslationHandler(@NotNull List<String> langList) {

        DataAccess[] translationAccess = {
                TranslationDataAccess.INV_AUCTION_HOUSE_SEARCH,
                TranslationDataAccess.INV_ITEM_SHOP_SEARCH,
                TranslationDataAccess.INV_NOOK_SEARCH,
                TranslationDataAccess.INV_TRADE_SEARCH,
                TranslationDataAccess.TIMESTAMP_SEARCH,
                TranslationDataAccess.SELLER_SEARCH,
                TranslationDataAccess.BID_SEARCH,
                TranslationDataAccess.AH_BUY_SEARCH,
                TranslationDataAccess.THEME_SERVER_SEARCH,
                TranslationDataAccess.HIGHEST_BIDDER_SEARCH,
                TranslationDataAccess.NOOK_BUY_SEARCH,
                TranslationDataAccess.SHOP_BUY_SEARCH,
                TranslationDataAccess.SHOP_SELL_SEARCH,
                TranslationDataAccess.TRADE_BUY_SEARCH,
                TranslationDataAccess.HOUR_SEARCH,
                TranslationDataAccess.MINUTE_SEARCH,
                TranslationDataAccess.SECOND_SEARCH,
                TranslationDataAccess.NOW_SEARCH,
                TranslationDataAccess.SKYBLOCK_INV_BLOCK,
                TranslationDataAccess.CITYBUILD_INV_BLOCK
        };


        data.put("cxnprice.translation",
                new DataHandler(serverChecker,
                        "/settings/translations",
                        langList,
                        "translation_key",
                        DataHandler.TRANSLATION_REFRESH_INTERVAL, translationAccess));
    }

    private @NotNull Mono<Void> refreshData(boolean forced) {
        return Flux.fromIterable(data.entrySet())
                .flatMap(entry -> entry.getValue().refresh(forced))
                .then();
    }

    public void deactivate() {
        if (!this.active.get()) return; //return wenn schon deaktiviert

        deactivateListeners();
        this.active.set(false);
    }

    public DataHandler getData(String key) {
        return data.get(key);
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
    public @NotNull Mono<Boolean> isMinVersion() {
        return this.serverChecker.getServerMinVersion()
                .map(serverMinVersion -> PriceCxnMod.getIntVersion(PriceCxnMod.MOD_VERSION)
                        .filter(value -> PriceCxnMod.getIntVersion(serverMinVersion)
                                .filter(integer -> value >= integer)
                                .isPresent())
                        .isPresent());
    }

    public @NotNull Mono<Boolean> isSpecialUser() {
        if (MinecraftClient.getInstance().player == null)
            return Mono.just(false);

        return new WebSocketCompletion(serverChecker.getWebsocket(), "isSpecialUser", MinecraftClient.getInstance().player.getUuidAsString()).getMono()
                .map(s -> s.equals("true"))
                .onErrorReturn(false);
    }

    public @NotNull Mono<Pair<Boolean, ActionNotification>> checkConnection(boolean themeRefresh) {
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
                .subscribeOn(Schedulers.fromExecutor(ServerChecker.EXECUTOR))
                .flatMap(result -> result);
    }

    public Mono<Pair<Boolean, ActionNotification>> checkConnectionAsync() {
        return checkConnectionAsync(true);
    }

    public @NotNull Optional<List<String>> getModUsers() {
        List<String> stringList = new ArrayList<>();

        JsonArray array;

        try {
            array = this.data.get("pricecxn.data.mod_users").getDataArray();

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
