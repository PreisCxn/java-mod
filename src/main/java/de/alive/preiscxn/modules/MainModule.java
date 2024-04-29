package de.alive.preiscxn.modules;

import de.alive.api.module.Module;

public class MainModule implements Module {
    @Override
    public void load(ClassLoader parentClassloader) {
    }

    @Override
    public ClassLoader getModuleClassLoader() {
        return this.getClass().getClassLoader();
    }
}
