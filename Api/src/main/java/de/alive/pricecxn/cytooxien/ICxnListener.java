package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.IServerChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ICxnListener {
    @NotNull
    ICxnConnectionManager getConnectionManager();

    DataHandler getData(String key);

    @NotNull
    IServerChecker getServerChecker();

    @NotNull
    IThemeServerChecker getThemeChecker();

    @Nullable
    List<String> getModUsers();

    boolean isActive();

    @NotNull AtomicBoolean isOnServer();

}