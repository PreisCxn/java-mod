package de.alive.api.module;

import reactor.core.publisher.Mono;

public interface Module {
    Mono<Void> load(ClassLoader parentClassloader);
    ClassLoader getModuleClassLoader();
}
