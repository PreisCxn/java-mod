package de.alive.preiscxn.modules;

import de.alive.api.module.Module;
import de.alive.api.module.ModuleLoader;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

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
        Set<Class<? extends I>> list = new HashSet<>();
        for (Module module : modules) {
            list.addAll(getInterfaces(module.getPrimaryPackage(), interfaceClass));
        }
        list.remove(interfaceClass);
        return list;
    }

    private <I> List<Class<? extends I>> getInterfaces(String primaryPackage, Class<I> interfaceClass) {
        LOGGER.info("Loading interfaces for {}", interfaceClass.getName());
        List<Class<? extends I>> list = new ArrayList<>();
        try{
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(primaryPackage.replace(".", File.separator));

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Path path = Path.of(url.toURI());
                try(Stream<Path> pathStream = Files.walk(path)) {
                    pathStream.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".class"))
                            .filter(p -> !p.toString().toLowerCase().contains("mixin"))//mixins cannot be loaded in this phase
                            .map(p -> p.toString()
                                    .replace(".class", "")
                                    .replace(File.separator, "."))
                            .forEach(className -> {
                                int index = className.indexOf(primaryPackage);
                                if (index != -1) {
                                    className = className.substring(index);
                                }
                                try{
                                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                                    if (interfaceClass.isAssignableFrom(clazz)) {
                                        list.add(clazz.asSubclass(interfaceClass));
                                    }
                                }catch(ClassNotFoundException e){
                                    LOGGER.error("Error while loading class", e);
                                }catch (RuntimeException e){
                                    LOGGER.info("Got RuntimeException while loading class {}. This may be caused by a Mixin.", className);
                                }
                            });

                }
            }
        }catch(Exception e){
            LOGGER.error("Error while loading module", e);
        }
        return list;
    }
}
