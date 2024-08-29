package de.alive.preiscxn.api.networking;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.alive.preiscxn.api.PriceCxn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class DataHandler {

    public static final int TRANSLATION_REFRESH_INTERVAL = 1000 * 60 * 60; // 1 Stunde
    public static final int MODUSER_REFRESH_INTERVAL = 1000 * 60 * 60 * 6; // 6 Stunden
    public static final int ITEM_REFRESH_INTERVAL = 1000 * 60 * 60 * 3; // 6 Stunden
    private final @NotNull IServerChecker serverChecker;
    private final @Nullable List<String> columnNames;
    private final @Nullable String keyColumnName;
    private long lastUpdate = 0;
    private String uri;
    private int refreshInterval = 0;
    private @Nullable Map<String, List<String>> data = null;
    private @Nullable JsonArray dataArray = null;
    private @Nullable JsonObject dataObject = null;

    /**
     * This constructor is used to create a DataHandler
     *
     * @param serverChecker   The ServerChecker that should be used to check if the server is reachable
     * @param uri             The uri of the server
     * @param columnNames     The names of the columns that should be returned
     * @param keyColumnName   The name of the column that should be used as key
     * @param refreshInterval The interval in which the data should be refreshed in milliseconds
     */
    public DataHandler(@NotNull IServerChecker serverChecker,
                       @NotNull String uri,
                       @Nullable List<String> columnNames,
                       @Nullable String keyColumnName,
                       int refreshInterval) {
        PriceCxn.getMod().getDataHandlers().add(this);
        this.uri = uri;
        this.serverChecker = serverChecker;
        this.refreshInterval = refreshInterval;
        this.columnNames = columnNames;
        this.keyColumnName = keyColumnName;
    }

    public DataHandler(@NotNull IServerChecker serverChecker, @NotNull String uri, int refreshInterval) {
        this(serverChecker, uri, null, null, refreshInterval);
    }

    /**
     * This method is used to refresh the data
     *
     * @param isForced If the refresh is forced, the data will be refreshed even if it is up-to-date
     * @return A CompletableFuture which returns null if the refresh was successful
     */
    public @NotNull Mono<Void> refresh(boolean isForced) {
        PriceCxn.getMod().getLogger().debug("refreshData 1");

        // If the data is already up-to-date and the refresh is not forced, we can return the CompletableFuture
        if (!isForced && (lastUpdate == 0 || System.currentTimeMillis() - this.lastUpdate < this.refreshInterval)) {
            PriceCxn.getMod().getLogger().debug("Data is already up-to-date");
            return Mono.empty();
        }

        PriceCxn.getMod().getLogger().debug("refreshData 2");

        // Check the server connection asynchronously
        return this.serverChecker.isConnected()
                .filter(aBoolean -> aBoolean)
                .flatMap(isConnected ->
                        getServerDataAsync(this.uri, this.columnNames, this.keyColumnName))
                .doOnNext(data -> {
                    this.data = data;
                    this.lastUpdate = System.currentTimeMillis();
                })
                .doOnError(ex -> PriceCxn.getMod().getLogger().error("Failed to refresh data", ex))
                .then();
    }

    /**
     * This method is used to refresh the data only if it is not up-to-date
     *
     * @return A CompletableFuture which returns null if the refresh was successful
     */
    public @NotNull Mono<Void> refresh() {
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
    private @NotNull Mono<Map<String, List<String>>> getServerDataAsync(String url,
                                                                        @Nullable List<String> columnNames,
                                                                        @Nullable String keyColumnName) {
        return PriceCxn.getMod().getHttp().get(url)
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

                    if (this.dataArray == null || this.dataArray.isEmpty()) {
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
                                for (int i = 0; i < rowData.length; i++) {
                                    rowData[i] = rowData[i].replace("Ã¤", "ä");
                                    rowData[i] = rowData[i].replace("Ã¶", "ö");
                                    rowData[i] = rowData[i].replace("Ã¼", "ü");
                                    rowData[i] = rowData[i].replace("ÃŸ", "ß");
                                    rowData[i] = rowData[i].replace("Ã„", "Ä");
                                    rowData[i] = rowData[i].replace("Ã–", "Ö");
                                    rowData[i] = rowData[i].replace("Ãœ", "Ü");
                                }
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

    public void setUri(String uri) {
        this.uri = uri;
    }

}
