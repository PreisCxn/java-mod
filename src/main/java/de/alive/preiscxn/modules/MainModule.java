package de.alive.preiscxn.modules;

import de.alive.api.module.Module;
import reactor.core.publisher.Mono;

public class MainModule implements Module {
    @Override
    public Mono<Void> load(ClassLoader parentClassloader) {
        return Mono.empty();
    }

    @Override
    public ClassLoader getModuleClassLoader() {
        return this.getClass().getClassLoader();
    }
}
