package de.alive.preiscxn.impl.modules;

public class MainModule extends ClasspathModule {
    public MainModule() {
        super("de.alive.preiscxn", Thread.currentThread().getContextClassLoader());
    }

}
