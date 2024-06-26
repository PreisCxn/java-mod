package de.alive.preiscxn.impl.modules;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.module.Module;
import de.alive.preiscxn.api.module.ModuleLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



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
        PriceCxn.getMod().getLogger().info("Loading interfaces for {}", interfaceClass.getName());

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
