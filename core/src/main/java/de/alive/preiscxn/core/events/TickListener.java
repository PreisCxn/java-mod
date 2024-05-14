package de.alive.preiscxn.core.events;

import de.alive.api.interfaces.IMinecraftClient;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TickListener {
    private final List<Consumer<IMinecraftClient>> consumer;
    private final IMinecraftClient client;

    public TickListener(IMinecraftClient client){
        this.client = client;
        this.consumer = new ArrayList<>();
    }

    public void add(Consumer<IMinecraftClient> consumer){
        this.consumer.add(consumer);
    }

    @Subscribe
    public void onGameTick(GameTickEvent event) {
        if (event.phase() != Phase.PRE) {
            return;
        }

        this.consumer.forEach(consumer -> consumer.accept(client));
    }
}
