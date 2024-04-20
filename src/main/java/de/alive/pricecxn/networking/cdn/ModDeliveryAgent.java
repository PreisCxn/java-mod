package de.alive.pricecxn.networking.cdn;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.alive.pricecxn.networking.Http;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModDeliveryAgent {
    private final ModDeliveryType<JsonArray> LIST_FILES = new ModDeliveryType<>("type=list-files", s -> JsonParser.parseString(s).getAsJsonArray());
    private final ModDeliveryType<JsonArray> LIST_VERSIONS = new ModDeliveryType<>("type=list-versions", s -> JsonParser.parseString(s).getAsJsonArray());
    private final ModDeliveryType<String> NEWEST_VERSION = new ModDeliveryType<>("type=newest-version", Function.identity());

    private static final String BASE_URL = "https://cdn.preiscxn.de/";
    private static final String MOD_PATH = "PriceCxnMod.jar";
    private static ModDeliveryAgent instance;
    private final Http http;

    protected ModDeliveryAgent(Http http) {
        this.http = http;
    }

    private ModDeliveryAgent() {
        this.http = Http.getInstance();
    }

    public static ModDeliveryAgent getInstance() {
        if (instance == null) {
            instance = new ModDeliveryAgent();
        }
        return instance;
    }

    public String getModPath() {
        return BASE_URL + MOD_PATH;
    }

    public Mono<List<String>> getModVersions() {
        return LIST_VERSIONS.generateResponse(jsonElements -> {
            List<String> versions = new ArrayList<>();
            for (int i = 0; i < jsonElements.size(); i++)
                versions.add(jsonElements.get(i).getAsString());
            return versions;
        });
    }

    public Mono<String> getNewestVersion() {
        return NEWEST_VERSION.generateResponse(Function.identity());
    }


    private class ModDeliveryType<T> {

        private final String type;
        private final Function<String, T> stringTFunction;

        private ModDeliveryType(String type, Function<String, T> stringTFunction) {
            this.type = type;
            this.stringTFunction = stringTFunction;
        }

        public <P> Mono<P> generateResponse(Function<T, P> function) {
            return http.GET(BASE_URL, MOD_PATH + "?" + this.type, stringTFunction)
                    .map(function);
        }

    }

}
