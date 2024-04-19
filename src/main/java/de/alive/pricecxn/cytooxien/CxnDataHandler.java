package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonObject;
import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.ServerChecker;
import de.alive.pricecxn.networking.sockets.WebSocketCompletion;
import de.alive.pricecxn.utils.StringUtil;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.alive.pricecxn.PriceCxnMod.LOGGER;

public class CxnDataHandler {

    private final ServerChecker serverChecker;
    private final ThemeServerChecker themeChecker;
    private final Map<String, DataHandler> data = new HashMap<>();

    public CxnDataHandler(ServerChecker serverChecker, ThemeServerChecker themeChecker) {
        this.serverChecker = serverChecker;
        this.themeChecker = themeChecker;
    }

    public @NotNull Mono<Void> initData() {
        LOGGER.debug("initData");

        if (!this.data.containsKey("pricecxn.data.mod_users")) {
            data.put("pricecxn.data.mod_users", new DataHandler(serverChecker, "/datahandler/mod_users", DataHandler.MODUSER_REFRESH_INTERVAL));
        }

        if (this.data.containsKey("cxnprice.translation"))
            return Mono.empty();
        else
            return new WebSocketCompletion(serverChecker.getWebsocket(), "translationLanguages")
                    .getMono()
                    .map(StringUtil::stringToList)
                    .doOnSuccess(this::createTranslationHandler).then();
    }

    public @NotNull Mono<Void> refreshItemData(String dataKey, boolean isNook) {
        if (!this.data.containsKey(dataKey) || this.data.get(dataKey).getDataObject() == null) {

            if (this.themeChecker.getMode().equals(Modes.SKYBLOCK)) {
                data.put(dataKey, new DataHandler(serverChecker, "/datahandler/items/skyblock/true/" + (isNook ? "true" : "false"), DataHandler.ITEM_REFRESH_INTERVAL));
            } else if (this.themeChecker.getMode().equals(Modes.CITYBUILD)) {
                data.put(dataKey, new DataHandler(serverChecker, "/datahandler/items/citybuild/true/" + (isNook ? "true" : "false"), DataHandler.ITEM_REFRESH_INTERVAL));
            } else return Mono.empty();

        } else {
            JsonObject jsonObject = data.get(dataKey).getDataObject();
            if (jsonObject == null || !jsonObject.has("mode")) return Mono.empty();
            String mode = jsonObject.get("mode").getAsString();

            if (this.themeChecker.getMode().equals(Modes.SKYBLOCK) && !mode.equals(Modes.SKYBLOCK.getTranslationKey())) {
                data.get(dataKey).setUri("/datahandler/items/skyblock/true/" + (isNook ? "true" : "false"));
            } else if (this.themeChecker.getMode().equals(Modes.CITYBUILD) && !mode.equals(Modes.CITYBUILD.getTranslationKey())) {
                data.get(dataKey).setUri("/datahandler/items/citybuild/true/" + (isNook ? "true" : "false"));
            } else return Mono.empty();

        }

        return data.get(dataKey).refresh(true);
    }

    public @NotNull Mono<Void> refreshData(boolean forced) {
        return Flux.fromIterable(data.entrySet())
                .flatMap(entry -> entry.getValue().refresh(forced))
                .then();
    }

    public void putData(String key, DataHandler dataHandler) {
        data.put(key, dataHandler);
    }

    public DataHandler get(String key) {
        return data.get(key);
    }

    private void createTranslationHandler(@NotNull List<String> langList) {
        // Method implementation...
    }

    public DataHandler getData(String key) {
        return data.get(key);
    }

    public @NotNull ServerChecker getServerChecker() {
        return serverChecker;
    }
}