package de.alive.pricecxn;

import de.alive.pricecxn.utils.Http;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ServerChecker {

    private static final String DEFAULT_CHECK_URI = "localhost:8080";
    private static final int DEFAULT_CHECK_INTERVAL = 1000;
    private boolean connected = false;
    private final String uri;
    private final int checkInterval;
    private long lastCheck = 0;

    /**
     * This constructor is used to check if the server is reachable
     *
     * @param uri           The uri of the server
     * @param checkInterval The interval in which the server is checked in milliseconds
     */
    public ServerChecker(@Nullable String uri, int checkInterval) {
        this.uri = uri == null ? DEFAULT_CHECK_URI : uri;
        this.checkInterval = checkInterval < 0 ? DEFAULT_CHECK_INTERVAL : checkInterval;
        checkConnection();
    }

    /**
     * This constructor is used to check if the server is reachable
     * Uses the default check interval and uri
     */
    public ServerChecker() {
        this(null, -1);
    }

    /**
     * This method is used to check if the server is reachable
     * @return A CompletableFuture which returns true if the server is reachable and false if not
     */
    public CompletableFuture<Boolean> checkConnection() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Http.GET(this.uri, response -> {
            future.complete(true);
            this.lastCheck = System.currentTimeMillis();
            return null;
        }, null);

        future.exceptionally(ex -> {
            future.complete(false);
            return null;
        });

        return future;
    }

    /**
     * This method is used to check if the server is reachable
     * Only checks if the server is reachable if the last check was more than the check interval ago or the last check was never
     * @return A CompletableFuture which returns true if the server is reachable and false if not
     */
    public CompletableFuture<Boolean> isConnected() {
        if (this.connected && (System.currentTimeMillis() - this.lastCheck < this.checkInterval) && this.lastCheck > 0)
            return CompletableFuture.completedFuture(true);
        else
            return checkConnection();
    }


}
