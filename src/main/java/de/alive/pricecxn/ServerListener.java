package de.alive.pricecxn;

import de.alive.pricecxn.utils.StringUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to check if the player is on a specific server.
 */
public abstract class ServerListener {

    private static final String DEFAULT_IGNORED_IP = "beta";
    private static final String DEFAULT_IP = "cytooxien";

    private final AtomicBoolean onServer = new AtomicBoolean(Boolean.FALSE);
    private final List<String> ips;
    private final List<String> ignoredIps;

    public ServerListener(@NotNull String ip, @Nullable String ignoredIp) {
        this(List.of(ip), ignoredIp == null ? List.of() : List.of(ignoredIp));
    }

    public ServerListener(@NotNull List<String> ips, @Nullable List<String> ignoredIps) {
        this.ips = StringUtil.listToLowerCase(ips);
        this.ignoredIps = ignoredIps == null ? new ArrayList<>() : StringUtil.listToLowerCase(ignoredIps);

        init();
    }

    private void init(){
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if(client == null) return;
            if(client.getCurrentServerEntry() == null) return;
            if(onServer.get()) return;

            ips.stream()
                    .filter(ip -> client.getCurrentServerEntry().address.toLowerCase().contains(ip))
                    .filter(ip -> ignoredIps.stream().noneMatch(ignoredIp -> client.getCurrentServerEntry().address.toLowerCase().contains(ignoredIp)))
                    .findFirst()
                    .ifPresent(ip -> {
                        onServer.set(true);
                        onServerJoin();
                    });

        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            boolean doMethod = this.onServer.get();
            this.onServer.set(false);
            if(doMethod) this.onServerLeave();
        });
    }

    /**
     * This method is called when the player joins the server
     */
    public abstract void onServerJoin();

    /**
     * This method is called when the player leaves the server
     */
    public abstract void onServerLeave();

    /**
     * Returns the AtomicBoolean that is used to check if the player is on the server
     * @return The AtomicBoolean
     */
    public AtomicBoolean isOnServer(){
        return onServer;
    }

}
