package de.alive.api.module;

import reactor.core.publisher.Mono;

import java.util.Set;

public interface ModuleLoader {
    Mono<Void> addModule(Module module);
    <I> Set<Class<? extends I>> loadInterfaces(Class<I> interfaceClass);
}
