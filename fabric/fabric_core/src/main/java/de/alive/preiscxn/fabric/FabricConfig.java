package de.alive.preiscxn.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.interfaces.PriceCxnConfig;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfig implements PriceCxnConfig {
    private Boolean active;
    private Boolean displayCoin;

    public FabricConfig() {
    }

    @Override
    public Boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Boolean isDisplayCoin() {
        return displayCoin;
    }

    public void setDisplayCoin(boolean displayCoin) {
        this.displayCoin = displayCoin;
    }

    public static Mono<FabricConfig> loadConfig(Path filePath) {
        return Mono.fromCallable(() -> {
            Gson gson = new Gson();
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                return gson.fromJson(reader, FabricConfig.class);
            } catch (IOException e) {
                PriceCxn.getMod().getLogger().error("Failed to load config file: {}", e, filePath);
                return new FabricConfig();
            }
        }).publishOn(Schedulers.boundedElastic());
    }

    public static Mono<Void> saveDefault(Path filePath) {
        return Mono.fromRunnable(() -> {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FabricConfig defaultConfig = new FabricConfig();
            defaultConfig.setActive(true);
            defaultConfig.setDisplayCoin(true);

            if (Files.exists(filePath)) {
                FabricConfig existingConfig;
                try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                    existingConfig = gson.fromJson(reader, FabricConfig.class);
                }catch (IOException e) {
                    PriceCxn.getMod().getLogger().error("Failed to load config file: {}", e, filePath);
                    return;
                }

                if (existingConfig.isActive() == null) {
                    existingConfig.setActive(defaultConfig.isActive());
                }
                if (existingConfig.isDisplayCoin() == null) {
                    existingConfig.setDisplayCoin(defaultConfig.isDisplayCoin());
                }

                // Write the updated config back
                try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                    gson.toJson(existingConfig, writer);
                }catch (IOException e) {
                    PriceCxn.getMod().getLogger().error("Failed to save config file: {}", e, filePath);
                }
            } else {
                // File does not exist, write the default config
                try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                    gson.toJson(defaultConfig, writer);
                }catch (IOException e) {
                    PriceCxn.getMod().getLogger().error("Failed to save config file: {}", e, filePath);
                }
            }
        }).publishOn(Schedulers.boundedElastic()).then();

    }
}