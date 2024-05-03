package de.alive.preiscxn.modules;

import de.alive.api.module.Module;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static de.alive.api.LogPrinter.LOGGER;

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
                Path path = Path.of(url.toURI());
                try (Stream<Path> pathStream = Files.walk(path)) {
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
                                try {
                                    consumer.accept(Thread.currentThread().getContextClassLoader().loadClass(className));
                                } catch (ClassNotFoundException e) {
                                    LOGGER.error("Error while loading class", e);
                                } catch (RuntimeException e) {
                                    LOGGER.info("Got RuntimeException while loading class {}.", className, e);
                                }
                            });

                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while loading module", e);
        }

    }
}
