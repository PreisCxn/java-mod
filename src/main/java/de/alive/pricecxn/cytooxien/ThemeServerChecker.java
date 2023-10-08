package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.listener.ServerListener;
import de.alive.pricecxn.listener.TabListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

/**
 * This class is used to check the theme server for the current mode.
 */
public class ThemeServerChecker extends TabListener {

    private static final List<String> DEFAULT_SEARCHES = List.of("Du befindest dich auf");

    private Modes mode = Modes.NOTHING;

    private final AtomicBoolean onServer;

    private final ServerListener serverListener;

    public ThemeServerChecker(@Nullable ServerListener serverListener, @NotNull DataAccess searches, @NotNull AtomicBoolean onServer) {
        super(searches);
        this.onServer = onServer;
        this.serverListener = serverListener;
    }

    public ThemeServerChecker(@Nullable ServerListener serverListener, @NotNull AtomicBoolean onServer) {
        this(serverListener, SearchDataAccess.THEME_SERVER_SEARCH, onServer);
    }

    public ThemeServerChecker(@NotNull AtomicBoolean onServer) {
        this(null, SearchDataAccess.THEME_SERVER_SEARCH, onServer);
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

        if(serverListener != null)
            serverListener.onTabChange();

        printDebug("New Mode: " + this.mode.toString());
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.translatable("cxn_listener.theme_checker.changed", this.mode.toString()).setStyle(PriceCxnMod.DEFAULT_TEXT).formatted(Formatting.ITALIC), true);
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

    @Override
    public void onJoinEvent() {
        if (serverListener != null)
            serverListener.onJoinEvent();
    }

    public Modes getMode() {
        return mode;
    }
}
