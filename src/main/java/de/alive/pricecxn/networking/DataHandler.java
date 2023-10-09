package de.alive.pricecxn.networking;

import com.google.gson.*;
import de.alive.pricecxn.utils.Http;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DataHandler {

    private final ServerChecker serverChecker;

    public static final int TRANSLATION_REFRESH_INTERVAL = 1000 * 60 * 60; // 1 Stunde
    public static final int MODUSER_REFRESH_INTERVAL = 1000 * 60 * 60 * 6; // 5 Stunden

    private long lastUpdate = 0;

    private final String uri;

    private int refreshInterval = 0;

    private final List<String> columnNames;
    private final String keyColumnName;

    private Map<String, List<String>> data = null;
    private JsonArray dataArray = null;

    /**
     * This constructor is used to create a DataHandler
     * @param serverChecker The ServerChecker that should be used to check if the server is reachable
     * @param uri The uri of the server
     * @param columnNames The names of the columns that should be returned
     * @param keyColumnName The name of the column that should be used as key
     * @param refreshInterval The interval in which the data should be refreshed in milliseconds
     */
    public DataHandler(@NotNull ServerChecker serverChecker, @NotNull String uri, @Nullable List<String> columnNames, @Nullable String keyColumnName, int refreshInterval, @Nullable DataAccess... dataAccess) {
        this.uri = uri;
        this.serverChecker = serverChecker;
        this.refreshInterval = refreshInterval;
        this.columnNames = columnNames;
        this.keyColumnName = keyColumnName;
        if(dataAccess != null) {
            for (DataAccess access : dataAccess)
                if(access != null) access.setDataHandler(this);
        }
    }

    public DataHandler(@NotNull ServerChecker serverChecker, @NotNull String uri, @Nullable List<String> columnNames, @Nullable String keyColumnName, int refreshInterval) {
        this(serverChecker, uri, columnNames, keyColumnName, refreshInterval, (DataAccess) null);
    }

    public DataHandler(@NotNull ServerChecker serverChecker, @NotNull String uri, int refreshInterval) {
        this(serverChecker, uri, null, null, refreshInterval, (DataAccess) null);
    }

    /**
     * This method is used to refresh the data
     * @param isForced If the refresh is forced, the data will be refreshed even if it is up-to-date
     * @return A CompletableFuture which returns null if the refresh was successful
     */
    public CompletableFuture<Void> refresh(boolean isForced) {
        System.out.println("refreshData 1");
        CompletableFuture<Void> future = new CompletableFuture<>();

        // If the data is already up-to-date and the refresh is not forced, we can return the CompletableFuture
        if (!isForced && (lastUpdate == 0 || System.currentTimeMillis() - this.lastUpdate < this.refreshInterval)) {
            System.err.println("Data is already up-to-date");
            return CompletableFuture.completedFuture(null);
        }

        System.out.println("refreshData 2");

        // Check the server connection asynchronously
        this.serverChecker.isConnected().thenCompose(isConnected -> {
            System.out.println("refreshData 3");
            if (!isConnected) {
                System.err.println("Server isn't reachable");
                return CompletableFuture.completedFuture(null);
            }

            System.out.println("refreshData 4");

            // Request the data asynchronously
            return getServerDataAsync(this.uri, this.columnNames, this.keyColumnName);
        }).thenAccept(data -> {
            System.out.println("refreshData 5");
            this.data = data;
            this.lastUpdate = System.currentTimeMillis();
            future.complete(null); // Marking the CompletableFuture as completed
        }).exceptionally(ex -> {
            System.out.println("refreshData 6");
            ex.printStackTrace();
            future.complete(null); // Marking the CompletableFuture as completed exceptionally
            return null;
        });

        System.out.println("refreshData end");

        return future;
    }

    /**
     * This method is used to refresh the data only if it is not up-to-date
     * @return A CompletableFuture which returns null if the refresh was successful
     */
    public CompletableFuture<Void> refresh() {
        return refresh(false);
    }

    /**
     * This method is used to get the data async from the server
     * @param url The url to the server
     * @param columnNames The names of the columns that should be returned
     * @param keyColumnName The name of the column that should be used as key
     * @return A CompletableFuture which returns the data as a Map with the key as the key and the values as a List
     */
    private CompletableFuture<Map<String, List<String>>> getServerDataAsync(String url, List<String> columnNames, String keyColumnName) {
        CompletableFuture<Map<String, List<String>>> future = new CompletableFuture<>();

        System.out.println("getting Server Data");

        Http.GET(url, "", response -> response, jsonString -> {
            Map<String, List<String>> data = null;

            System.out.println("result: " + jsonString);

            try {
                JsonArray array = JsonParser.parseString(jsonString).getAsJsonArray();
                dataArray = array;

                if(keyColumnName == null || columnNames == null) {
                    future.complete(null);
                    return null;
                }

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

    public @Nullable Map<String, List<String>> getData() {
        if(keyColumnName == null || columnNames == null)
            return null;
        return data;
    }

    public @Nullable JsonArray getDataArray(){
        return dataArray;
    }

    public void setDataAccess(DataAccess dataAccess){
        dataAccess.setDataHandler(this);
    }

    public static CompletableFuture<Void> refresh(boolean isForced, DataHandler... dataHandlers){
        CompletableFuture<Void> future = new CompletableFuture<>();

        CompletableFuture.allOf(
                Arrays.stream(dataHandlers)
                        .map(dataHandler -> dataHandler.refresh(isForced))
                        .toArray(CompletableFuture[]::new))
                .thenAccept(Void -> future.complete(null));

        return future;
    }
}
