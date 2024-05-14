package de.alive.preiscxn.core.cytooxien;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.ICxnDataHandler;
import de.alive.api.cytooxien.IThemeServerChecker;
import de.alive.api.cytooxien.Modes;
import de.alive.api.networking.DataAccess;
import de.alive.api.networking.DataHandler;
import de.alive.api.networking.IServerChecker;
import de.alive.api.utils.StringUtil;
import de.alive.preiscxn.core.networking.sockets.WebSocketCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.alive.api.LogPrinter.LOGGER;

public class CxnDataHandler implements ICxnDataHandler {

    private final IServerChecker serverChecker;
    private final IThemeServerChecker themeChecker;
    private final Map<String, DataHandler> data = new HashMap<>();

    public CxnDataHandler(IServerChecker serverChecker, IThemeServerChecker themeChecker) {
        this.serverChecker = serverChecker;
        this.themeChecker = themeChecker;
    }

    @Override
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
                    .doOnNext(this::createTranslationHandler)
                    .then();
    }

    @Override
    public @NotNull Mono<Void> refreshItemData(String dataKey, boolean isNook) {
        if (!this.data.containsKey(dataKey) || this.data.get(dataKey).getDataObject() == null) {

            if (this.themeChecker.getMode().equals(Modes.SKYBLOCK)) {
                data.put(dataKey, new DataHandler(
                        serverChecker,
                        "/datahandler/items/skyblock/true/"
                        + (isNook ? "true" : "false"),
                        DataHandler.ITEM_REFRESH_INTERVAL));
            } else if (this.themeChecker.getMode().equals(Modes.CITYBUILD)) {
                data.put(dataKey,
                        new DataHandler(
                                serverChecker,
                                "/datahandler/items/citybuild/true/"
                                + (isNook ? "true" : "false"),
                                DataHandler.ITEM_REFRESH_INTERVAL));
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

    @Override
    public @NotNull Mono<Void> refreshData(boolean forced) {
        return Flux.fromIterable(data.entrySet())
                .flatMap(entry -> entry.getValue().refresh(forced))
                .then();
    }

    @Override
    public DataHandler get(String key) {
        return data.get(key);
    }

    @Override
    public DataHandler getData(String key) {
        return data.get(key);
    }

    @Override
    public @Nullable List<String> getModUsers() {
        List<String> stringList = new ArrayList<>();

        JsonArray array;

        try {
            array = data.get("pricecxn.data.mod_users").getDataArray();

            if (array == null) return null;

            array.forEach(element -> {
                if (!element.isJsonNull())
                    stringList.add(element.getAsString());
            });

            if (stringList.isEmpty()) return null;

            return stringList;
        } catch (Exception e) {
            LOGGER.error("Error while getting mod users", e);
            return null;
        }
    }

    public void createTranslationHandler(@NotNull List<String> langList) {
        Set<Class<? extends DataAccess>> classes = PriceCxn.getMod()
                .getProjectLoader()
                .loadInterfaces(DataAccess.class);

        List<DataAccess> dataList = new ArrayList<>();
        for (Class<? extends DataAccess> aClass : classes) {

            if (aClass.isEnum()) {
                try {
                    DataAccess[] values = (DataAccess[]) aClass.getMethod("values").invoke(null);
                    dataList.addAll(Arrays.asList(values));
                } catch (Exception e) {
                    LOGGER.error("Error while loading enum values", e);
                    return;
                }
            }
        }
        LOGGER.info("Loaded {} DataAccess", dataList.size());

        data.put("cxnprice.translation",
                new DataHandler(serverChecker,
                        "/settings/translations",
                        langList,
                        "translation_key",
                        DataHandler.TRANSLATION_REFRESH_INTERVAL,
                        dataList.toArray(new DataAccess[0])
                ));
    }
}
