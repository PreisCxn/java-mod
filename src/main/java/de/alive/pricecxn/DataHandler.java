package de.alive.pricecxn;

import com.google.gson.*;
import de.alive.pricecxn.utils.Http;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DataHandler {

    private final ServerChecker serverChecker;

    private long lastUpdate = 0;

    private final String uri;

    private int refreshInterval = 0;

    private final List<String> columnNames;
    private final String keyColumnName;

    private Map<String, List<String>> data = null;

    public DataHandler(ServerChecker serverChecker, String uri, List<String> columnNames, String keyColumnName, int refreshInterval) {

        this.uri = uri;
        this.serverChecker = serverChecker;
        this.refreshInterval = refreshInterval;
        this.columnNames = columnNames;
        this.keyColumnName = keyColumnName;

        refresh();
    }

    public CompletableFuture<Void> refresh(boolean isForced) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // If the data is already up-to-date and the refresh is not forced, we can return the CompletableFuture
        if(!isForced && (lastUpdate == 0 || System.currentTimeMillis() - this.lastUpdate < this.refreshInterval)) {
            System.err.println("Data is already up-to-date");
            return null;
        }

        // If the server is not connected, we can return the CompletableFuture
        if(!this.serverChecker.isConnected()) {
            System.err.println("Server isn't reachable");
            return null;
        }

        // else request the data
        CompletableFuture<Map<String, List<String>>> importFuture = importSettingsAsync(this.uri, this.columnNames, this.keyColumnName);

        importFuture.thenAccept(data -> {
            this.data = data;
            this.lastUpdate = System.currentTimeMillis();
            future.complete(null); // Marking the CompletableFuture as completed
        }).exceptionally(ex -> {
            ex.printStackTrace();
            future.completeExceptionally(ex); // Marking the CompletableFuture as completed exceptionally
            return null;
        });

        return future;
    }

    public CompletableFuture<Void> refresh() {
        return refresh(false);
    }

    public static CompletableFuture<Map<String, List<String>>> importSettingsAsync(String url, List<String> columnNames, String keyColumnName) {
        CompletableFuture<Map<String, List<String>>> future = new CompletableFuture<>();

        Http.GET(url, "", response -> response, jsonString -> {
            Map<String, List<String>> data = null;

            try {
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(jsonString).getAsJsonArray();

                data = new HashMap<>();

                for (JsonElement object : array) {
                    JsonObject json = object.getAsJsonObject();
                    String key;

                    try {
                        key = json.get(keyColumnName).getAsString();
                    } catch (Exception e) {
                        return null;
                    }

                    List<String> values = new ArrayList<>();

                    for (String columnName : columnNames) {
                        try {
                            JsonNull no = json.get(columnName).getAsJsonNull();
                        } catch (Exception e) {
                            String[] rowData = json.get(columnName).getAsString().split(", ");
                            values.addAll(Arrays.asList(rowData));
                        }
                    }

                    data.put(key, values);
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }

            future.complete(data);
            return null;
        });

        return future;
    }
}
