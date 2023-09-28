package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.cytooxien.dataobservers.*;
import de.alive.pricecxn.listener.InventoryListener;
import de.alive.pricecxn.listener.ServerListener;
import de.alive.pricecxn.networking.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class CxnListener extends ServerListener {

    private static final List<String> DEFAULT_IPS = List.of("cytooxien");
    private static final List<String> DEFAULT_IGNORED_IPS = List.of("beta");
    private final ThemeServerChecker themeChecker;
    private final List<InventoryListener> listeners;
    private final ServerChecker serverChecker;
    private final Map<String, DataHandler> data = new HashMap<>();

    private NetworkingState state = NetworkingState.OFFLINE;

    private AtomicBoolean active = new AtomicBoolean(false);

    public CxnListener() {
        super(DEFAULT_IPS, DEFAULT_IGNORED_IPS);

        //getting Data from server
        this.serverChecker = new ServerChecker();
        data.put("pricecxn.data.item_data", new DataHandler(serverChecker, "", List.of(""), "", 0));
        data.put("pricecxn.data", new DataHandler(serverChecker, "", List.of(""), "", 0, SearchDataAccess.TIMESTAMP_SEARCH));

        serverChecker.isConnected().thenCompose(isConnected -> {
            if(isConnected)
                System.out.println("connected to server");
            return null;
        });

        serverChecker.addSocketListener(message -> System.out.println("CytooxienListener received message: " + message));

        //setting up theme checker and listeners
        this.themeChecker = new ThemeServerChecker(this, this.isOnServer());
        listeners = List.of(
                new AuctionHouseListener(this.isOnServer(), this.active),
                new ItemShopListener(this.isOnServer(), this.active),
                new TomNookListener(this.isOnServer(), this.active),
                new TradeListener(this.isOnServer(), this.active)
        );

    }

    //refreshes the data after mode change
    @Override
    public void onTabChange(){
        data.get("pricecxn.data.item_data").refresh();
    }

    @Override
    public void onServerJoin() {

        //überprüfen ob die Mod bereits aktiv ist
          //wenn ja, dann mach weiter
          //wenn nein dann versuche Verbindung zum Server aufzubauen
            //wenn Verbindung erfolgreich, überprüfe Maintenance Status und Min-Version nummer
                //wenn Maintenance Status true, überprüfe User Berechtigung
                    //wenn User Berechtigung nicht erfüllt, dann deaktiviere Mod und gib Fehlermeldung aus
                    //wenn User Berechtigung hat, dann aktiviere Mod
                //wenn Min-Version nicht erfüllt, dann deaktiviere Mod und gib Fehlermeldung aus
            //wenn Verbindung nicht erfolgreich, dann deaktiviere Mod und gib Fehlermeldung aus

        if(this.state == NetworkingState.OFFLINE){

        }

        themeChecker.refreshAsync().thenRun(() -> {

            printDebug("joined Cytooxien: " + this.isOnServer().get());
            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("test.translatable.cxnListener").formatted(Formatting.RED));

        });
    }

    @Override
    public void onServerLeave() {
        System.out.println("Cytooxien left : " + this.isOnServer().get());
    }

    public void activate(){
        this.state = NetworkingState.ONLINE;
        activateListeners();
    }

    public void deactivate(){
        this.state = NetworkingState.OFFLINE;
        deactivateListeners();
    }

    public DataHandler getData(String key) {
        return data.get(key);
    }

    public ServerChecker getServerChecker() {
        return serverChecker;
    }

    private void activateListeners(){
        this.active.set(true);
    }

    private void deactivateListeners(){
        this.active.set(false);
    }

    public ThemeServerChecker getThemeChecker() {
        return themeChecker;
    }

    public NetworkingState getState() {
        return state;
    }
}
