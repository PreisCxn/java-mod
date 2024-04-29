package de.alive.api.module;

public interface Module {
    void load(ClassLoader parentClassloader);
    ClassLoader getModuleClassLoader();
}
