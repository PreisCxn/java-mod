package de.alive.preiscxn.modules;

import de.alive.api.module.Module;

public class ClasspathModule implements Module {
    private final String primaryPackage;

    public ClasspathModule(String primaryPackage) {
        this.primaryPackage = primaryPackage;
    }

    @Override
    public String getPrimaryPackage() {
        return primaryPackage;
    }
}
