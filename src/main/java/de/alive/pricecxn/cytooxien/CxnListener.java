package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.DataHandler;
import de.alive.pricecxn.ServerChecker;
import de.alive.pricecxn.ServerListener;
import de.alive.pricecxn.cytooxien.listener.*;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CxnListener extends ServerListener {

    private static final List<String> DEFAULT_IPS = List.of("cytooxien");
    private static final List<String> DEFAULT_IGNORED_IPS = List.of("beta");

    private final ThemeServerChecker themeChecker;
    private final List<InventoryListener> listeners;
    private final ServerChecker serverChecker;
    private final Map<String, DataHandler> data = new HashMap<>();

    public CxnListener() {
        super(DEFAULT_IPS, DEFAULT_IGNORED_IPS);

        //getting Data from server
        this.serverChecker = new ServerChecker();
        data.put("pricecxn.data.item_data", new DataHandler(serverChecker, "", List.of(""), "", 0));
        data.put("pricecxn.data", new DataHandler(serverChecker, "", List.of(""), "", 0, SearchDataAccess.TIMESTAMP_SEARCH));



        //setting up theme checker and listeners
        this.themeChecker = new ThemeServerChecker(this, this.isOnServer());
        listeners = List.of(
                new AuctionHouseListener(this.isOnServer()),
                new ItemShopListener(this.isOnServer()),
                new TomNookListener(this.isOnServer()),
                new TradeListener(this.isOnServer())
        );

    }

    //refreshes the data after mode change
    @Override
    public void refreshOnTabChange(){
        data.get("pricecxn.data.item_data").refresh();
    }

    @Override
    public void onServerJoin() {
        themeChecker.refreshAsync().thenRun(() -> {



            System.out.println("Cytooxien joined : " + this.isOnServer().get());
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("test.translatable.cxnListener").formatted(Formatting.RED));
            MinecraftClient.getInstance().player.sendMessage(StringUtil.getColorizedString("Cytooxien joined : " + this.isOnServer().get() + " : " + themeChecker.getMode().toString(), Formatting.AQUA));
        });
    }

    @Override
    public void onServerLeave() {
        System.out.println("Cytooxien left : " + this.isOnServer().get());
    }

    public DataHandler getData(String key) {
        return data.get(key);
    }

}
