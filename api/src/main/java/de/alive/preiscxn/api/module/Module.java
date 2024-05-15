package de.alive.preiscxn.api.module;

import java.util.function.Consumer;

public interface Module {
    void forEach(Consumer<Class<?>> consumer);
}
