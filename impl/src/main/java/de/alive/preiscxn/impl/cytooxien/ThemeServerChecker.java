package de.alive.preiscxn.impl.cytooxien;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.IThemeServerChecker;
import de.alive.preiscxn.api.cytooxien.Modes;
import de.alive.preiscxn.api.cytooxien.TranslationDataAccess;
import de.alive.preiscxn.api.listener.ServerListener;
import de.alive.preiscxn.api.listener.TabListener;
import de.alive.preiscxn.api.networking.DataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.preiscxn.api.Mod.DEBUG_MODE;

/**
 * This class is used to check the theme server for the current mode.
 */
public class ThemeServerChecker extends TabListener implements IThemeServerChecker {

    private @NotNull Modes mode = Modes.NOTHING;

    private final @NotNull AtomicBoolean onServer;

    private final @Nullable ServerListener serverListener;

    public ThemeServerChecker(@Nullable ServerListener serverListener, @NotNull DataAccess searches, @NotNull AtomicBoolean onServer) {
        super(searches);
        this.onServer = onServer;
        this.serverListener = serverListener;
    }

    public ThemeServerChecker(@Nullable ServerListener serverListener, @NotNull AtomicBoolean onServer) {
        this(serverListener, TranslationDataAccess.THEME_SERVER_SEARCH, onServer);
    }

    //check for the mode from the tab list
    @Override
    protected @NotNull Mono<Void> handleData(@NotNull String data) {
        String lowerCaseData = data.toLowerCase();

        if (lowerCaseData.contains(Modes.SKYBLOCK.toString().toLowerCase())) {
            this.mode = Modes.SKYBLOCK;
        } else if (lowerCaseData.contains(Modes.CITYBUILD.toString().toLowerCase())) {
            this.mode = Modes.CITYBUILD;
        } else if (lowerCaseData.contains(Modes.LOBBY.toString().toLowerCase())) {
            this.mode = Modes.LOBBY;
        } else {
            this.mode = Modes.NOTHING;
        }

        setNotInValue(this.mode.toString());

        Mono<Void> voidMono = Mono.empty();
        if (serverListener != null)
            voidMono = voidMono.then(serverListener.onTabChange());

        PriceCxn.getMod().printDebug("New Mode: " + this.mode);


        if (DEBUG_MODE && !PriceCxn.getMod().getMinecraftClient().isPlayerNull())
            PriceCxn.getMod().getMinecraftClient()
                    .sendTranslatableMessage(
                            "cxn_listener.theme_checker.changed",
                            true,
                            true,
                            this.mode.toString());

        return voidMono;
    }

    @Override
    protected int getRefreshesAfterJoinEvent() {
        return 5;
    }

    //Only when the player is on the server the refresh method should be called after a server change on the Network
    @Override
    protected boolean refreshAfterJoinEvent() {
        return onServer.get();
    }

    @Override
    public Mono<Void> onJoinEvent() {
        if (serverListener != null)
            return serverListener.onJoinEvent();
        return Mono.empty();
    }

    @Override
    public @NotNull Modes getMode() {
        return mode;
    }
}
