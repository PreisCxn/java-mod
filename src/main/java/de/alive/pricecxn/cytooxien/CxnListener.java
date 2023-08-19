package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.ServerListener;
import de.alive.pricecxn.cytooxien.listener.*;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

import java.util.List;

public class CxnListener extends ServerListener {

    private final ThemeServerChecker themeChecker;

    public CxnListener() {
        super(List.of("Cytooxien"), List.of("beta"));

        this.themeChecker = new ThemeServerChecker(List.of("Du befindest dich auf"), this.isOnServer());

        InventoryListener auctionListener = new AuctionHouseListener(this.isOnServer());
        InventoryListener itemListener = new ItemShopListener(this.isOnServer());
        InventoryListener nookListener = new TomNookListener(this.isOnServer());
        InventoryListener tradeListener = new TradeListener(this.isOnServer());

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
