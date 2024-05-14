package de.alive.preiscxn.impl.modules;

import de.alive.api.module.Module;
import de.alive.api.module.ModuleLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.alive.api.LogPrinter.LOGGER;

public class ModuleLoaderImpl implements ModuleLoader {

    private final List<Module> modules = new ArrayList<>();

    public ModuleLoaderImpl() {
    }

    @Override
    public void addModule(Module module) {
        this.modules.add(module);
    }

    @Override
    public <I> Set<Class<? extends I>> loadInterfaces(Class<I> interfaceClass) {
        LOGGER.info("Loading interfaces for {}", interfaceClass.getName());

        Set<Class<? extends I>> set = new HashSet<>();
        for (Module module : modules) {
            set.addAll(getInterfacesFromModule(module, interfaceClass));
        }

        set.remove(interfaceClass);
        return set;
    }

    private <I> List<Class<? extends I>> getInterfacesFromModule(Module module, Class<I> interfaceClass) {
        List<Class<? extends I>> list = new ArrayList<>();

        module.forEach(aClass -> {
            if (interfaceClass.isAssignableFrom(aClass)) {
                list.add(aClass.asSubclass(interfaceClass));
            }
        });

        return list;
    }
}
