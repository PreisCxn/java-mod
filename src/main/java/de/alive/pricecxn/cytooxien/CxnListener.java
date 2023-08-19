package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.ServerListener;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

import java.util.List;

public class CxnListener extends ServerListener {

    private ThemeServerChecker themeChecker;

    public CxnListener() {
        super(List.of("Cytooxien"), List.of("beta"));

        this.themeChecker = new ThemeServerChecker(List.of("Du befindest dich auf"), this.isOnServer());

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
