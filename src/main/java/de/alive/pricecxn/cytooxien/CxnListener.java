package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.cytooxien.dataobservers.*;
import de.alive.pricecxn.listener.InventoryListener;
import de.alive.pricecxn.listener.ServerListener;
import de.alive.pricecxn.networking.*;
import de.alive.pricecxn.networking.sockets.WebSocketCompletion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class CxnListener extends ServerListener {

    private static final List<String> DEFAULT_IPS = List.of("cytooxien");
    private static final List<String> DEFAULT_IGNORED_IPS = List.of("beta");
    private final ThemeServerChecker themeChecker;
    private final List<InventoryListener> listeners;
    private final ServerChecker serverChecker;
    private final Map<String, DataHandler> data = new HashMap<>();

    private AtomicBoolean active = null;
    private Boolean isRightVersion = null;
    NetworkingState state = NetworkingState.OFFLINE;
    private AtomicBoolean listenerActive = new AtomicBoolean(false);

    public CxnListener() {
        super(DEFAULT_IPS, DEFAULT_IGNORED_IPS);

        //getting Data from server
        this.serverChecker = new ServerChecker();
        /*
        serverChecker.isConnected().thenCompose(isConnected -> {
            if(isConnected) {
                System.out.println("server connected");
                System.out.println("isVersion: " + isMinVersion());
                NetworkingState state = serverChecker.getState();
                System.out.println("State: " + (state == NetworkingState.MAINTENANCE ? "Maintenance" : state == NetworkingState.ONLINE ? "Online" : "Offline"));
            }
            return null;
        });
        */
        /*
        serverChecker.addSocketListener(message -> {
            System.out.println("isVersion: " + isMinVersion());
            NetworkingState state = serverChecker.getState();
            System.out.println("State: " + (state == NetworkingState.MAINTENANCE ? "Maintenance" : state == NetworkingState.ONLINE ? "Online" : "Offline"));
        });
         */

        //setting up theme checker and listeners
        this.themeChecker = new ThemeServerChecker(this, this.isOnServer());
        listeners = List.of(
                new AuctionHouseListener(this.isOnServer(), listenerActive),
                new ItemShopListener(this.isOnServer(), listenerActive),
                new TomNookListener(this.isOnServer(), listenerActive),
                new TradeListener(this.isOnServer(), listenerActive)
        );

        System.out.println("trying activating");
        checkConnectionAsync(false).thenRun(() -> {
            System.out.println("Mod active?" + this.active.get());
        });

    }

    @Override
    public void onTabChange(){

    }

    @Override
    public void onJoinEvent() {
        if(!this.isOnServer().get()) return;
        boolean activeBackup = this.active.get();
        checkConnectionAsync().thenAccept(CxnListener::sendConnectionInformation).thenRun(() -> {
            System.out.println("test ende");
            if(activeBackup) refreshData(true);
        });
    }

    @Override
    public void onServerJoin() {
        System.out.println("onServerJoin");

        checkConnectionAsync().thenAccept(message -> {
            System.out.println("send info started");
            CxnListener.sendConnectionInformation(message, true);
            System.out.println("send info ended");
        });
        /*

        themeChecker.refreshAsync().thenRun(() -> {

            printDebug("joined Cytooxien: " + this.isOnServer().get());
            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("test.translatable.cxnListener").formatted(Formatting.RED));

        });

         */
    }

    @Override
    public void onServerLeave() {
        System.out.println("Cytooxien left : " + this.isOnServer().get());
    }

    public CompletableFuture<Void> activate(boolean themeRefresh){
        System.out.println("test2");
        if (this.active.get()) return CompletableFuture.completedFuture(null); //return wenn schon aktiviert

        CompletableFuture<Void> future = new CompletableFuture<>();

        System.out.println("test3");
        //daten initialisieren
        data.put("pricecxn.data.item_data", new DataHandler(serverChecker, "", List.of(""), "", 0));
        data.put("pricecxn.data", new DataHandler(serverChecker, "", List.of(""), "", 0, SearchDataAccess.TIMESTAMP_SEARCH));

        refreshData(true).thenAccept(Void -> {
            if(themeRefresh)
                this.themeChecker.refreshAsync().thenRun(() -> future.complete(null));
            else future.complete(null);
        });

        System.out.println("test4");

        activateListeners();
        this.active.set(true);
        isRightVersion = true;

        System.out.println("test5");

        return future;
    }

    private CompletableFuture<Void> refreshData(boolean forced){
        CompletableFuture<Void> future = new CompletableFuture<>();
        for(Map.Entry<String, DataHandler> entry : data.entrySet()){
            CompletableFuture.allOf(entry.getValue().refresh(forced)).thenAccept(Void -> future.complete(null));
        }
        return future;
    }

    public void deactivate(){
        if(!this.active.get()) return; //return wenn schon deaktiviert

        deactivateListeners();
        this.active.set(false);
    }

    public static void sendConnectionInformation(Pair<Boolean, ActionNotification> messageInformation, boolean force){

        System.out.println("sendInfo: " + force + " " + messageInformation.getLeft() + " " + messageInformation.getRight());

        if(force || messageInformation.getLeft()){
            if(MinecraftClient.getInstance().player != null){
                ActionNotification message = messageInformation.getRight();
                if(message.hasTextVariables()) {
                    MinecraftClient.getInstance().player.sendMessage(Text.translatable(message.getTranslationKey(), (Object[]) message.getTextVariables()));
                } else
                    MinecraftClient.getInstance().player.sendMessage(Text.translatable(message.getTranslationKey()));
            }
        }

    }
    public static void sendConnectionInformation(Pair<Boolean, ActionNotification> messageInformation){
        sendConnectionInformation(messageInformation, false);
    }


    public DataHandler getData(String key) {
        return data.get(key);
    }

    public ServerChecker getServerChecker() {
        return serverChecker;
    }

    private void activateListeners(){
        this.listenerActive.set(true);
    }

    private void deactivateListeners(){
        this.listenerActive.set(false);
    }

    public ThemeServerChecker getThemeChecker() {
        return themeChecker;
    }

    /** Gibt zurück, ob die Min-Version des Servers die aktuelle Version der Mod erfüllt.
        (Mod Version > Server Min-Version -> true)
     */
    public boolean isMinVersion(){
        return PriceCxnMod.getIntVersion(PriceCxnMod.MOD_VERSION)
                .filter(value -> PriceCxnMod.getIntVersion(this.serverChecker.getServerMinVersion())
                        .filter(integer -> value >= integer)
                        .isPresent())
                .isPresent();
    }

    public CompletableFuture<Boolean> isSpecialUser(){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if(MinecraftClient.getInstance().player == null) future.complete(false);
        CompletableFuture<String> websocketFuture = new WebSocketCompletion(serverChecker.getWebsocket(), "isSpecialUser", MinecraftClient.getInstance().player.getUuidAsString()).getFuture();
        if(websocketFuture.isCompletedExceptionally()) future.complete(false);
        websocketFuture.thenCompose(version -> {
            future.complete(version.equals("true"));
            return null;
        });
        return future;
    }

    public CompletableFuture<Pair<Boolean, ActionNotification>> checkConnection(boolean themeRefresh) {
        CompletableFuture<Pair<Boolean, ActionNotification>> future = new CompletableFuture<>();

        AtomicBoolean activeBackup = this.active == null ? null : new AtomicBoolean(this.active.get());
        Boolean isRightVersionBackup = isRightVersion;
        NetworkingState stateBackup = this.state;

        System.out.println("starting check! " + activeBackup + " " + isRightVersionBackup + " " + stateBackup);

        if(this.active == null) this.active = new AtomicBoolean(false);

        serverChecker.isConnected().thenCompose(isConnected -> {
            System.out.println("isConnected: " + isConnected);
            if (!isConnected) {
                // Server nicht erreichbar
                System.out.println("Server nicht erreichbar");
                this.deactivate();
                future.complete(new Pair<>(activeBackup == null || activeBackup.get(), ActionNotification.SERVER_OFFLINE));
            } else if (!isMinVersion()) {
                // Version nicht korrekt
                System.out.println("Version nicht korrekt");
                this.deactivate();
                System.out.println("VV");
                ActionNotification.WRONG_VERSION.setTextVariables(this.serverChecker.getServerMinVersion());
                System.out.println("VVV");
                System.out.println(isRightVersionBackup + " " + this.serverChecker.getServerMinVersion());
                this.isRightVersion = false;
                future.complete(new Pair<>(isRightVersionBackup == null || isRightVersionBackup, ActionNotification.WRONG_VERSION));
            } else {
                NetworkingState serverCheckerState = serverChecker.getState();
                System.out.println("State: " + (serverCheckerState == NetworkingState.MAINTENANCE ? "Maintenance" : serverCheckerState == NetworkingState.ONLINE ? "Online" : "Offline"));
                if (serverCheckerState == NetworkingState.ONLINE) {
                    // Server im Online-Modus
                    System.out.println("Server im Online-Modus");
                    this.activate(themeRefresh).thenRun(() -> {
                        future.complete(new Pair<>(activeBackup == null || !activeBackup.get(), ActionNotification.MOD_STARTED));
                    });
                } else if (serverCheckerState == NetworkingState.MAINTENANCE) {
                    isSpecialUser().thenApply(isSpecialUser -> {
                        if (isSpecialUser) {
                            // Benutzer hat Berechtigung
                            System.out.println("Benutzer hat Berechtigung");
                            this.activate(themeRefresh).thenRun(() -> {
                                future.complete(new Pair<>(stateBackup != NetworkingState.MAINTENANCE || activeBackup == null || !activeBackup.get(), ActionNotification.SERVER_MAINTEANCE_WITH_PERMISSON));
                                System.out.println("test1");
                            });
                        } else {
                            // Benutzer hat keine Berechtigung
                            System.out.println("Benutzer hat keine Berechtigung");
                            this.deactivate();
                            future.complete(new Pair<>(stateBackup != NetworkingState.MAINTENANCE, ActionNotification.SERVER_MAINTENANCE));
                        }
                        return null;
                    });
                } else {
                    // Server im Offline-Modus
                    System.out.println("Server im Offline-Modus");
                    this.deactivate();
                    future.complete(new Pair<>(activeBackup == null || activeBackup.get(), ActionNotification.SERVER_OFFLINE));
                }
            }
            this.state = serverChecker.getState();
            return null;
        });

        return future;
    }

    public CompletableFuture<Pair<Boolean, ActionNotification>> checkConnectionAsync(boolean themeRefresh) {
        return CompletableFuture.supplyAsync(() -> this.checkConnection(themeRefresh), ServerChecker.EXECUTOR).thenCompose(result -> result);
    }

    public CompletableFuture<Pair<Boolean, ActionNotification>> checkConnectionAsync() {
        return CompletableFuture.supplyAsync(() -> this.checkConnection(true), ServerChecker.EXECUTOR).thenCompose(result -> result);
    }

}
