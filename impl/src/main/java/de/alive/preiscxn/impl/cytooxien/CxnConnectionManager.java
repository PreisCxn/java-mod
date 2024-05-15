package de.alive.preiscxn.impl.cytooxien;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.ActionNotification;
import de.alive.preiscxn.api.cytooxien.ICxnConnectionManager;
import de.alive.preiscxn.api.cytooxien.ICxnDataHandler;
import de.alive.preiscxn.api.cytooxien.IThemeServerChecker;
import de.alive.preiscxn.api.networking.IServerChecker;
import de.alive.preiscxn.api.networking.NetworkingState;
import de.alive.preiscxn.impl.networking.sockets.WebSocketCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class manages the connection to the server and handles the state of the connection.
 */
public class CxnConnectionManager implements ICxnConnectionManager {

    private final ICxnDataHandler dataHandler;
    private final IServerChecker serverChecker;
    private final IThemeServerChecker themeChecker;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private @Nullable Boolean isRightVersion = null;
    private @NotNull NetworkingState state = NetworkingState.OFFLINE;
    private final AtomicBoolean listenerActive;
    /**
     * Constructor for the CxnConnectionManager class.
     *
     * @param dataHandler Handles data related operations.
     * @param serverChecker Checks the server status.
     * @param themeChecker Checks the theme status.
     * @param listenerActive Indicates if the listener is active.
     */
    public CxnConnectionManager(ICxnDataHandler dataHandler,
                                IServerChecker serverChecker,
                                IThemeServerChecker themeChecker,
                                AtomicBoolean listenerActive) {
        this.dataHandler = dataHandler;
        this.serverChecker = serverChecker;
        this.themeChecker = themeChecker;
        this.listenerActive = listenerActive;
    }
    /**
     * Checks the connection to the server asynchronously.
     *
     * @param refresh Indicates if the theme should be refreshed.
     *
     * @return A Mono object containing a Pair of a Boolean and an ActionNotification.
     */
    @Override
    public @NotNull Mono<Tuple2<Boolean, ActionNotification>> checkConnectionAsync(Refresh refresh) {
        return checkConnection(refresh);
    }
    /**
     * Checks the connection to the server.
     *
     * @param refresh Indicates if the theme should be refreshed.
     *
     * @return A Mono object containing a Pair of a Boolean and an ActionNotification.
     */
    @Override
    public @NotNull Mono<Tuple2<Boolean, ActionNotification>> checkConnection(Refresh refresh) {
        boolean activeCache = this.active.get();
        Boolean isRightVersionBackup = isRightVersion;
        NetworkingState stateBackup = this.state;

        return Mono.zip(serverChecker.isConnected(), isMinVersion(), this.serverChecker.getServerMinVersion())
                .flatMap(tuple3 -> processConnectionStatus(refresh,
                        tuple3.getT1(),
                        tuple3.getT2(),
                        tuple3.getT3(),
                        activeCache,
                        isRightVersionBackup,
                        stateBackup));
    }
    /**
     * Processes the connection status.
     *
     * @param refresh Indicates if the theme should be refreshed.
     * @param isConnected Indicates if the server is connected.
     * @param isMinVersion Indicates if the server version is the minimum required version.
     * @param serverMinVersion The minimum version of the server.
     * @param activeCache The cache of the active status.
     * @param isRightVersionBackup A backup of the isRightVersion status.
     * @param stateBackup A backup of the state.
     *
     * @return A Mono object containing a Pair of a Boolean and an ActionNotification.
     */
    private @NotNull Mono<Tuple2<Boolean, ActionNotification>> processConnectionStatus(Refresh refresh,
                                                                                     boolean isConnected,
                                                                                     boolean isMinVersion,
                                                                                     String serverMinVersion,
                                                                                     boolean activeCache,
                                                                                     Boolean isRightVersionBackup,
                                                                                     NetworkingState stateBackup) {
        this.state = serverChecker.getState();
        if (!isConnected) {
            return handleServerNotReachable(activeCache);
        } else if (!isMinVersion) {
            return handleIncorrectVersion(serverMinVersion, isRightVersionBackup);
        } else {
            return handleServerOnline(refresh, activeCache, stateBackup);
        }
    }
    /**
     * Handles the case when the server is not reachable.
     *
     * @param activeCache The cache of the active status.
     *
     * @return A Mono object containing a Pair of a Boolean and an ActionNotification.
     */
    private @NotNull Mono<Tuple2<Boolean, ActionNotification>> handleServerNotReachable(boolean activeCache) {
        this.deactivate();
        PriceCxn.getMod().getLogger().info("Server nicht erreichbar");
        return Mono.just(Tuples.of(activeCache, ActionNotification.SERVER_OFFLINE));
    }
    /**
     * Handles the case when the server version is incorrect.
     *
     * @param serverMinVersion The minimum version of the server.
     * @param isRightVersionBackup A backup of the isRightVersion status.
     *
     * @return A Mono object containing a Pair of a Boolean and an ActionNotification.
     */
    private @NotNull Mono<Tuple2<Boolean, ActionNotification>> handleIncorrectVersion(String serverMinVersion, @Nullable Boolean isRightVersionBackup) {
        this.deactivate();
        ActionNotification.WRONG_VERSION.setTextVariables(serverMinVersion);
        PriceCxn.getMod().getLogger().info("{} {}", isRightVersionBackup, serverMinVersion);
        this.isRightVersion = false;
        return Mono.just(Tuples.of(isRightVersionBackup == null || isRightVersionBackup, ActionNotification.WRONG_VERSION));
    }
    /**
     * Handles the case when the server is online.
     *
     * @param refresh Indicates if the theme should be refreshed.
     * @param activeCache The cache of the active status.
     * @param stateBackup A backup of the state.
     *
     * @return A Mono object containing a Pair of a Boolean and an ActionNotification.
     */
    private @NotNull Mono<Tuple2<Boolean, ActionNotification>> handleServerOnline(Refresh refresh, boolean activeCache, NetworkingState stateBackup) {
        NetworkingState serverCheckerState = serverChecker.getState();
        if (serverCheckerState == NetworkingState.ONLINE) {
            PriceCxn.getMod().getLogger().info("Server im Online-Modus");
            return this.activate(refresh)
                    .then(Mono.just(Tuples.of(!activeCache, ActionNotification.MOD_STARTED)));
        } else if (serverCheckerState == NetworkingState.MAINTENANCE) {
            return handleMaintenance(refresh, activeCache, stateBackup);
        } else {
            PriceCxn.getMod().getLogger().info("Server im Offline-Modus");
            this.deactivate();
            return Mono.just(Tuples.of(activeCache, ActionNotification.SERVER_OFFLINE));
        }
    }
    /**
     * Handles the case when the server is in maintenance mode.
     *
     * @param refresh Indicates if the theme should be refreshed.
     * @param activeCache The cache of the active status.
     * @param stateBackup A backup of the state.
     *
     * @return A Mono object containing a Pair of a Boolean and an ActionNotification.
     */
    private @NotNull Mono<Tuple2<Boolean, ActionNotification>> handleMaintenance(Refresh refresh,
                                                                               boolean activeCache,
                                                                               NetworkingState stateBackup) {
        return isSpecialUser()
                .flatMap(isSpecialUser -> {
                    if (isSpecialUser) {
                        PriceCxn.getMod().getLogger().info("Benutzer hat Berechtigung");
                        return this.activate(refresh).then(
                                Mono.just(Tuples.of(stateBackup != NetworkingState.MAINTENANCE
                                                     || !activeCache, ActionNotification.SERVER_MAINTEANCE_WITH_PERMISSON)));
                    } else {
                        PriceCxn.getMod().getLogger().info("Benutzer hat keine Berechtigung");
                        this.deactivate();
                        return Mono.just(Tuples.of(stateBackup != NetworkingState.MAINTENANCE, ActionNotification.SERVER_MAINTENANCE));
                    }
                });
    }

    /**
     * Checks if the server version is the minimum required version.
     *
     * @return A Mono object containing a Boolean indicating if the server version is the minimum required version.
     */
    @Override
    public @NotNull Mono<Boolean> isMinVersion() {
        return this.serverChecker.getServerMinVersion()
                .map(serverMinVersion -> {
                    String clientVersion = PriceCxn.getMod().getVersion();
                    return isClientVersionSameOrNewer(serverMinVersion, clientVersion);
                });
    }
    /**
     * Checks if the user is a special user.
     *
     * @return A Mono object containing a Boolean indicating if the user is a special user.
     */
    @Override
    public @NotNull Mono<Boolean> isSpecialUser() {
        if (PriceCxn.getMod().getPlayer() == null)
            return Mono.just(false);

        return new WebSocketCompletion(
                serverChecker.getWebsocket(),
                "isSpecialUser",
                PriceCxn.getMod().getPlayer().getUUIDasString()).getMono()
                .map(s -> s.equals("true"))
                .onErrorReturn(false);
    }
    /**
     * Activates the connection manager.
     *
     * @param refresh Indicates if the theme should be refreshed.
     *
     * @return A Mono object.
     */
    @Override
    public @NotNull Mono<Void> activate(Refresh refresh) {
        if (this.active.get()) return Mono.empty(); //return wenn schon aktiviert

        return dataHandler.initData()
                .then(dataHandler.refreshData(true))
                .then(Mono.just(themeChecker))
                .filter(themeServerChecker -> refresh == Refresh.THEME)
                .flatMap(IThemeServerChecker::refreshAsync)
                .doOnSuccess(ignored -> {
                    activateListeners();
                    this.active.set(true);
                    isRightVersion = true;
                })
                .onErrorResume(ex -> {
                    PriceCxn.getMod().getLogger().error("Error while activating mod", ex);
                    deactivate();
                    return Mono.error(ex);
                })
                .then();
    }
    /**
     * Deactivates the connection manager.
     */
    @Override
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

    /**
     * Checks if the connection manager is active.
     *
     * @return A Boolean indicating if the connection manager is active.
     */
    @Override
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
    public static void sendConnectionInformation(boolean shouldSend, @NotNull ActionNotification message, boolean force) {

        if (force || shouldSend) {
            if (PriceCxn.getMod().getPlayer() != null) {

                Object msg;
                if (message.hasTextVariables()) {

                    PriceCxn.getMod().getMinecraftClient()
                            .sendStyledTranslatableMessage(
                                    message.getTranslationKey(),
                                    false,
                                    PriceCxn.getMod().getDefaultStyle(),
                                    message.getTextVariables()
                            );

                } else {

                    PriceCxn.getMod().getMinecraftClient()
                            .sendStyledTranslatableMessage(
                                    message.getTranslationKey(),
                                    false,
                                    PriceCxn.getMod().getDefaultStyle()
                            );

                }
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
    public static void sendConnectionInformation(boolean shouldSend, @NotNull ActionNotification message) {
        sendConnectionInformation(shouldSend, message, false);
    }

    private static boolean isClientVersionSameOrNewer(String serverVersion, String clientVersion) {
        String[] serverVersionParts = serverVersion.split("\\D+");
        String[] clientVersionParts = clientVersion.split("\\D+");

        int length = Math.min(serverVersionParts.length, clientVersionParts.length);
        for (int i = 0; i < length; i++) {
            int serverPart = Integer.parseInt(serverVersionParts[i]);
            int clientPart = Integer.parseInt(clientVersionParts[i]);
            if (clientPart > serverPart) {
                return true;
            } else if (clientPart < serverPart) {
                return false;
            }
        }

        return clientVersionParts.length >= serverVersionParts.length;
    }
}
