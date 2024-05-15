package de.alive.preiscxn.impl.modules;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.module.Module;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.stream.Stream;



public class ClasspathModule implements Module {

    private final String primaryPackage;

    public ClasspathModule(String primaryPackage) {
        this.primaryPackage = primaryPackage;
    }

    @Override
    public void forEach(Consumer<Class<?>> consumer) {
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(primaryPackage.replace(".", "/"));

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Path path;
                URI uri = url.toURI();
                try {
                    path = Path.of(uri);
                } catch (FileSystemNotFoundException e) {
                    PriceCxn.getMod().getLogger().error("Error while loading module with uri: {}", uri);
                    continue;
                }
                try (Stream<Path> pathStream = Files.walk(path)) {
                    pathStream.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".class"))
                            .filter(p -> !p.toString().toLowerCase().contains("mixin"))//mixins cannot be loaded in this phase
                            .map(p -> p.toString()
                                    .replace(".class", "")
                                    .replace("\\", ".")
                                    .replace("/", "."))
                            .forEach(className -> {
                                int index = className.indexOf(primaryPackage);
                                if (index != -1) {
                                    className = className.substring(index);
                                }
                                try {
                                    consumer.accept(Thread.currentThread().getContextClassLoader().loadClass(className));
                                } catch (ClassNotFoundException e) {
                                    PriceCxn.getMod().getLogger().error("Error while loading class", e);
                                } catch (RuntimeException e) {
                                    PriceCxn.getMod().getLogger().info("Got RuntimeException while loading class {}.", className, e);
                                }
                            });

                }
            }
        } catch (Exception e) {
            PriceCxn.getMod().getLogger().error("Error while loading module", e);
        }

    }
}
