package de.alive.pricecxn.networking.cdn;

import de.alive.pricecxn.networking.Http;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static de.alive.pricecxn.networking.cdn.CdnDeliveryType.LIST_VERSIONS;
import static de.alive.pricecxn.networking.cdn.CdnDeliveryType.NEWEST_VERSION;

public class ModDeliveryAgent {
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

    public static @NotNull ModDeliveryAgent getInstance() {
        if (instance == null) {
            instance = new ModDeliveryAgent();
        }
        return instance;
    }

    public @NotNull Mono<List<String>> getModVersions() {
        return LIST_VERSIONS.generateResponse(MOD_PATH, jsonElements -> {
            List<String> versions = new ArrayList<>();
            for (int i = 0; i < jsonElements.size(); i++)
                versions.add(jsonElements.get(i).getAsString());
            return versions;
        });
    }

    public @NotNull Mono<String> getNewestVersion() {
        return NEWEST_VERSION.generateResponse(MOD_PATH, Function.identity());
    }
}
