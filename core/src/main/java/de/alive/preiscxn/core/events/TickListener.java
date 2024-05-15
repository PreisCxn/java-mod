package de.alive.preiscxn.core.events;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class TickListener {
    private final List<Consumer<IMinecraftClient>> tickConsumers;
    private final List<Consumer<IMinecraftClient>> joinConsumers;
    private final List<Consumer<IMinecraftClient>> disconnectConsumers;
    private final Callable<IMinecraftClient> client;

    public TickListener(Callable<IMinecraftClient> client){
        this.client = client;
        this.tickConsumers = new ArrayList<>();
        this.joinConsumers = new ArrayList<>();
        this.disconnectConsumers = new ArrayList<>();
    }

    public void addTickConsumer(Consumer<IMinecraftClient> consumer){
        this.tickConsumers.add(consumer);
    }

    public void addJoinConsumer(Consumer<IMinecraftClient> consumer){
        this.joinConsumers.add(consumer);
    }

    public void addDisconnectConsumer(Consumer<IMinecraftClient> consumer){
        this.disconnectConsumers.add(consumer);
    }

    private IMinecraftClient getClient(){
        try {
            return client.call();
        } catch (Exception e) {
            PriceCxn.getMod().getLogger().error("Failed to get client", e);
            return null;
        }
    }

    @Subscribe
    public void onGameTick(GameTickEvent event) {
        if (event.phase() != Phase.PRE) {
            return;
        }

        this.tickConsumers.forEach(consumer -> consumer.accept(getClient()));
    }

    @Subscribe
    public void onJoin(ServerJoinEvent event){
        this.joinConsumers.forEach(consumer -> consumer.accept(getClient()));
    }

    @Subscribe
    public void onDisconnect(ServerDisconnectEvent event){
        this.disconnectConsumers.forEach(consumer -> consumer.accept(getClient()));
    }
}
