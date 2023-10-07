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

    private AtomicBoolean listenerActive = new AtomicBoolean(false);

    public CxnListener() {
        super(DEFAULT_IPS, DEFAULT_IGNORED_IPS);

        //getting Data from server
        this.serverChecker = new ServerChecker();
        data.put("pricecxn.data.item_data", new DataHandler(serverChecker, "", List.of(""), "", 0));
        data.put("pricecxn.data", new DataHandler(serverChecker, "", List.of(""), "", 0, SearchDataAccess.TIMESTAMP_SEARCH));
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

        serverChecker.addSocketListener(message -> {
            System.out.println("isVersion: " + isMinVersion());
            NetworkingState state = serverChecker.getState();
            System.out.println("State: " + (state == NetworkingState.MAINTENANCE ? "Maintenance" : state == NetworkingState.ONLINE ? "Online" : "Offline"));
        });

        //setting up theme checker and listeners
        this.themeChecker = new ThemeServerChecker(this, this.isOnServer());
        listeners = List.of(
                new AuctionHouseListener(this.isOnServer(), listenerActive),
                new ItemShopListener(this.isOnServer(), listenerActive),
                new TomNookListener(this.isOnServer(), listenerActive),
                new TradeListener(this.isOnServer(), listenerActive)
        );

    }

    //refreshes the data after mode change
    @Override
    public void onTabChange(){
        data.get("pricecxn.data.item_data").refresh();
    }

    @Override
    public void onServerJoin() {

        checkConnection().thenCompose(pair -> {
            System.out.println("checkResult: " + pair.getLeft() + " " + pair.getRight().getTranslationKey());
                if(MinecraftClient.getInstance().player != null){
                    ActionNotification message = pair.getRight();
                    if(message.hasTextVariables()) {
                        MinecraftClient.getInstance().player.sendMessage(Text.translatable(message.getTranslationKey(), (Object[]) message.getTextVariables()));
                    } else
                        MinecraftClient.getInstance().player.sendMessage(Text.translatable(message.getTranslationKey()));
                }
            return null;
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

    public void activate(){

        activateListeners();
        this.active.set(true);
    }

    public void deactivate(){

        deactivateListeners();
        this.active.set(false);
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

    public CompletableFuture<Pair<Boolean, ActionNotification>> checkConnection() {
        CompletableFuture<Pair<Boolean, ActionNotification>> future = new CompletableFuture<>();

        AtomicBoolean activeBackup = this.active == null ? null : new AtomicBoolean(this.active.get());
        String minVersionBackup = this.serverChecker.getServerMinVersion();
        NetworkingState stateBackup = this.serverChecker.getState();

        System.out.println("starting check! " + activeBackup + " " + minVersionBackup + " " + stateBackup);

        if(this.active == null) this.active = new AtomicBoolean(false);

        serverChecker.isConnected().thenCompose(isConnected -> {
            if (!isConnected) {
                // Server nicht erreichbar
                System.out.println("Server nicht erreichbar");
                this.active.set(false);
                future.complete(new Pair<>(activeBackup == null || activeBackup.get(), ActionNotification.SERVER_OFFLINE));
            } else if (!isMinVersion()) {
                // Version nicht korrekt
                System.out.println("Version nicht korrekt");
                this.active.set(false);
                ActionNotification.WRONG_VERSION.setTextVariables(this.serverChecker.getServerMinVersion());
                future.complete(new Pair<>(!Objects.equals(minVersionBackup, this.serverChecker.getServerMinVersion()), ActionNotification.WRONG_VERSION));
            } else {
                NetworkingState state = serverChecker.getState();
                if (state == NetworkingState.ONLINE) {
                    // Server im Online-Modus
                    System.out.println("Server im Online-Modus");
                    this.active.set(true);
                    future.complete(new Pair<>(activeBackup == null || !activeBackup.get(), ActionNotification.MOD_STARTED));
                } else if (state == NetworkingState.MAINTENANCE) {
                    isSpecialUser().thenApply(isSpecialUser -> {
                        if (isSpecialUser) {
                            // Benutzer hat Berechtigung
                            System.out.println("Benutzer hat Berechtigung");
                            this.active.set(true);
                            future.complete(new Pair<>(stateBackup != NetworkingState.MAINTENANCE, ActionNotification.SERVER_MAINTEANCE_WITH_PERMISSON));
                        } else {
                            // Benutzer hat keine Berechtigung
                            System.out.println("Benutzer hat keine Berechtigung");
                            this.active.set(false);
                            future.complete(new Pair<>(stateBackup != NetworkingState.MAINTENANCE, ActionNotification.SERVER_MAINTENANCE));
                        }
                        return null;
                    });
                } else {
                    // Server im Offline-Modus
                    System.out.println("Server im Offline-Modus");
                    this.active.set(false);
                    future.complete(new Pair<>(activeBackup == null || activeBackup.get(), ActionNotification.SERVER_OFFLINE));
                }
            }
            return null;
        });

        return future;
    }

}
