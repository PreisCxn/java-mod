package de.alive.pricecxn.networking;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataHandler {

    public static final int TRANSLATION_REFRESH_INTERVAL = 1000 * 60 * 60; // 1 Stunde
    public static final int MODUSER_REFRESH_INTERVAL = 1000 * 60 * 60 * 6; // 6 Stunden
    public static final int ITEM_REFRESH_INTERVAL = 1000 * 60 * 60 * 3; // 6 Stunden
    private static final Logger LOGGER = Logger.getLogger(DataHandler.class.getName());
    private final ServerChecker serverChecker;
    private final List<String> columnNames;
    private final String keyColumnName;
    private long lastUpdate = 0;
    private String uri;
    private int refreshInterval = 0;
    private Map<String, List<String>> data = null;
    private JsonArray dataArray = null;
    private JsonObject dataObject = null;

    /**
     * This constructor is used to create a DataHandler
     *
     * @param serverChecker   The ServerChecker that should be used to check if the server is reachable
     * @param uri             The uri of the server
     * @param columnNames     The names of the columns that should be returned
     * @param keyColumnName   The name of the column that should be used as key
     * @param refreshInterval The interval in which the data should be refreshed in milliseconds
     */
    public DataHandler(@NotNull ServerChecker serverChecker, @NotNull String uri, @Nullable List<String> columnNames, @Nullable String keyColumnName, int refreshInterval, @Nullable DataAccess... dataAccess) {
        this.uri = uri;
        this.serverChecker = serverChecker;
        this.refreshInterval = refreshInterval;
        this.columnNames = columnNames;
        this.keyColumnName = keyColumnName;
        if (dataAccess != null) {
            for (DataAccess access : dataAccess)
                if (access != null) access.setDataHandler(this);
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
     *
     * @param isForced If the refresh is forced, the data will be refreshed even if it is up-to-date
     * @return A CompletableFuture which returns null if the refresh was successful
     */
    public Mono<Void> refresh(boolean isForced) {
        LOGGER.log(Level.INFO, "refreshData 1");

        // If the data is already up-to-date and the refresh is not forced, we can return the CompletableFuture
        if (!isForced && (lastUpdate == 0 || System.currentTimeMillis() - this.lastUpdate < this.refreshInterval)) {
            LOGGER.log(Level.INFO, "Data is already up-to-date");
            return Mono.empty();
        }

        LOGGER.log(Level.INFO, "refreshData 2");

        // Check the server connection asynchronously
        return this.serverChecker.isConnected()
                .flatMap(isConnected -> {
                    if (!isConnected) {
                        return Mono.empty();
                    }

                    // Request the data asynchronously
                    return getServerDataAsync(this.uri, this.columnNames, this.keyColumnName);
                })
                .doOnNext(data -> {
                    this.data = data;
                    this.lastUpdate = System.currentTimeMillis();
                })
                .doOnError(ex -> LOGGER.log(Level.SEVERE, "Failed to refresh data", ex))
                .then();
    }

    /**
     * This method is used to refresh the data only if it is not up-to-date
     *
     * @return A CompletableFuture which returns null if the refresh was successful
     */
    public Mono<Void> refresh() {
        return refresh(false);
    }

    /**
     * This method is used to get the data async from the server
     *
     * @param url           The url to the server
     * @param columnNames   The names of the columns that should be returned
     * @param keyColumnName The name of the column that should be used as key
     * @return A CompletableFuture which returns the data as a Map with the key as the key and the values as a List
     */
    private Mono<Map<String, List<String>>> getServerDataAsync(String url, List<String> columnNames, String keyColumnName) {
        return Http.GET(url, response -> response, jsonString -> jsonString)
                .mapNotNull(jsonString -> {
                    if (JsonParser.parseString(jsonString).isJsonArray()) {
                        dataArray = JsonParser.parseString(jsonString).getAsJsonArray();
                    } else if (JsonParser.parseString(jsonString).isJsonObject()) {
                        dataObject = JsonParser.parseString(jsonString).getAsJsonObject();
                    }

                    if (keyColumnName == null || columnNames == null) {
                        return null;
                    }

                    JsonArray array = this.dataArray;

                    if (this.dataArray.isEmpty()) {
                        return null;
                    }

                    data = new HashMap<>();

                    for (JsonElement object : array) {
                        JsonObject json = object.getAsJsonObject();
                        String key;

                        try{
                            key = json.get(keyColumnName).getAsString();
                        }catch(Exception e){
                            return null;
                        }

                        List<String> values = new ArrayList<>();

                        for (String columnName : columnNames) {
                            try{
                                JsonNull no = json.get(columnName).getAsJsonNull();
                            }catch(Exception e){
                                String[] rowData = json.get(columnName).getAsString().split(", ");
                                values.addAll(Arrays.asList(rowData));
                            }
                        }

                        data.put(key, values);
                    }

                    return data;
                });
    }

    public @Nullable Map<String, List<String>> getData() {
        if (keyColumnName == null || columnNames == null)
            return null;
        return data;
    }

    public @Nullable JsonArray getDataArray() {
        return dataArray;
    }

    public @Nullable JsonObject getDataObject() {
        return dataObject;
    }

    public void setDataAccess(DataAccess dataAccess) {
        dataAccess.setDataHandler(this);
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
