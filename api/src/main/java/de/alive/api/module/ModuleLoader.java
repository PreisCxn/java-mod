package de.alive.api.module;

import java.util.Set;

public interface ModuleLoader {
    void addModule(Module module);
    <I> Set<Class<? extends I>> loadInterfaces(Class<I> interfaceClass);
}
