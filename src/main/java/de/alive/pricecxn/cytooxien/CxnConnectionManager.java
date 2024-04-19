package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.networking.NetworkingState;
import de.alive.pricecxn.networking.ServerChecker;
import de.alive.pricecxn.networking.sockets.WebSocketCompletion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.LOGGER;

public class CxnConnectionManager {

    private final CxnDataHandler dataHandler;
    private final ServerChecker serverChecker;
    private final ThemeServerChecker themeChecker;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private @Nullable Boolean isRightVersion = null;
    private @NotNull NetworkingState state = NetworkingState.OFFLINE;
    private final AtomicBoolean listenerActive;

    public CxnConnectionManager(CxnDataHandler dataHandler, ServerChecker serverChecker, ThemeServerChecker themeChecker, AtomicBoolean listenerActive) {
        this.dataHandler = dataHandler;
        this.serverChecker = serverChecker;
        this.themeChecker = themeChecker;
        this.listenerActive = listenerActive;
    }

    public @NotNull Mono<Pair<Boolean, ActionNotification>> checkConnectionAsync(boolean themeRefresh) {
        return Mono.fromCallable(() -> checkConnection(themeRefresh))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> result);
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

                        LOGGER.info("Server nicht erreichbar");
                        return Mono.just(new Pair<>(activeCache, ActionNotification.SERVER_OFFLINE));
                    } else if (!isMinVersion) {
                        // Version nicht korrekt
                        this.deactivate();

                        ActionNotification.WRONG_VERSION.setTextVariables(serverMinVersion);

                        LOGGER.info("{} {}", isRightVersionBackup, serverMinVersion);
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

    /**
     * Gibt zurück, ob die Min-Version des Servers die aktuelle Version der Mod erfüllt.
     * (Mod Version > Server Min-Version → true)
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

    public @NotNull Mono<Void> activate(boolean themeRefresh) {
        if (this.active.get()) return Mono.empty(); //return wenn schon aktiviert

        return dataHandler.initData()
                .then(dataHandler.refreshData(true))
                .then(Mono.just(themeChecker))
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

    public void deactivate() {
        if (!this.active.get()) return; //return wenn schon deaktiviert

        deactivateListeners();
        this.active.set(false);
    }

    private void activateListeners() {
        this.listenerActive.set(true);
    }

    private void deactivateListeners() {
        this.listenerActive.set(false);
    }


    public boolean isActive() {
        return this.active.get();
    }
    /**
     * Sends a connection information message to the Minecraft player.
     * This method is used to send messages to the player about the status of the connection.
     * The message is only sent if the force parameter is true or the shouldSend parameter is true.
     *
     * @param shouldSend A boolean indicating whether the message should be sent.
     * @param message An ActionNotification object containing the message to be sent.
     * @param force A boolean that, if true, forces the message to be sent regardless of the shouldSend parameter.
     */
    public static void sendConnectionInformation(boolean shouldSend, ActionNotification message, boolean force) {

        if (force || shouldSend) {
            if (MinecraftClient.getInstance().player != null) {

                MutableText msg;
                if (message.hasTextVariables()) {

                    msg = PriceCxnMod.MOD_TEXT.copy()
                            .append(Text.translatable(message.getTranslationKey(), (Object[]) message.getTextVariables()))
                            .setStyle(PriceCxnMod.DEFAULT_TEXT);

                } else {

                    msg = PriceCxnMod.MOD_TEXT.copy()
                            .append(Text.translatable(message.getTranslationKey()))
                            .setStyle(PriceCxnMod.DEFAULT_TEXT);

                }
                MinecraftClient.getInstance().player.sendMessage(msg);
            }
        }

    }

    /**
     * Sends a connection information message to the Minecraft player.
     * This method is used to send messages to the player about the status of the connection.
     * The message is only sent if the shouldSend parameter is true.
     *
     * @param shouldSend A boolean indicating whether the message should be sent.
     * @param message An ActionNotification object containing the message to be sent.
     */
    public static void sendConnectionInformation(boolean shouldSend, ActionNotification message) {
        sendConnectionInformation(shouldSend, message, false);
    }
}
