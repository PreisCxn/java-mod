package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.DataHandler;
import de.alive.pricecxn.ServerChecker;
import de.alive.pricecxn.ServerListener;
import de.alive.pricecxn.cytooxien.listener.*;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CxnListener extends ServerListener {

    private static final String PRICE_CXN_URI = "";
    private final ThemeServerChecker themeChecker;
    private final List<InventoryListener> listeners;
    private final ServerChecker serverChecker;
    private final Map<String, DataHandler> data = new HashMap<>();

    public CxnListener() {
        super(List.of("Cytooxien"), List.of("beta"));

        this.themeChecker = new ThemeServerChecker(List.of("Du befindest dich auf"), this.isOnServer());
        this.serverChecker = new ServerChecker(CxnListener.PRICE_CXN_URI);

        data.put("pricecxn.data.item_data", new DataHandler(serverChecker, "", List.of(""), "", 0));

        listeners = List.of(
                new AuctionHouseListener(this.isOnServer()),
                new ItemShopListener(this.isOnServer()),
                new TomNookListener(this.isOnServer()),
                new TradeListener(this.isOnServer())
        );

    }

    @Override
    public void onServerJoin() {
        themeChecker.refreshAsync().thenRun(() -> {
            System.out.println("Cytooxien joined : " + this.isOnServer().get());
            MinecraftClient.getInstance().player.sendMessage(StringUtil.getColorizedString("Cytooxien joined : " + this.isOnServer().get() + " : " + themeChecker.getMode().toString(), Formatting.AQUA));
        });
    }

    @Override
    public void onServerLeave() {
        System.out.println("Cytooxien left : " + this.isOnServer().get());
    }
}
