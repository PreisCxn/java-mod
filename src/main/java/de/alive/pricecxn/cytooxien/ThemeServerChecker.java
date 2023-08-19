package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.TabListener;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to check the theme server for the current mode.
 */
public class ThemeServerChecker extends TabListener {

    private Modes mode = Modes.NOTHING;

    private final AtomicBoolean onServer;

    public ThemeServerChecker(@NotNull List<String> searches, @NotNull AtomicBoolean onServer) {
        super(searches);
        this.onServer = onServer;
    }

    //check for the mode from the tab list
    @Override
    protected void handleData(@NotNull String data) {
        String lowerCaseData = data.toLowerCase();

        if(lowerCaseData.contains(Modes.SKYBLOCK.toString().toLowerCase())) {
            this.mode = Modes.SKYBLOCK;
        } else if(lowerCaseData.contains(Modes.CITYBUILD.toString().toLowerCase())) {
            this.mode = Modes.CITYBUILD;
        } else if(lowerCaseData.contains(Modes.LOBBY.toString().toLowerCase())) {
            this.mode = Modes.LOBBY;
        } else {
            this.mode = Modes.NOTHING;
        }

        setNotInValue(this.mode.toString());

        MinecraftClient.getInstance().player.sendMessage(StringUtil.getColorizedString("New Mode: " + this.mode.toString(), Formatting.AQUA));
    }

    @Override
    protected int getRefreshesAfterJoinEvent(){
        return 5;
    }

    //Only when the player is on the server the refresh method should be called after a server change on the Network
    @Override
    protected boolean refreshAfterJoinEvent() {
        return onServer.get();
    }

    public Modes getMode() {
        return mode;
    }
}
