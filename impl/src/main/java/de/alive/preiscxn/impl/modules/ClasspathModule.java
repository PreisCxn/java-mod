package de.alive.preiscxn.impl.modules;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.module.Module;

import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ClasspathModule implements Module {

    private final String primaryPackage;
    private final ClassLoader loader;

    public ClasspathModule(String primaryPackage, ClassLoader loader) {
        this.primaryPackage = primaryPackage;
        this.loader = loader;
    }

    @Override
    public void forEach(Consumer<Class<?>> consumer) {
        try {
            Enumeration<URL> resources = loader.getResources(primaryPackage.replace(".", "/"));
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                URI uri = url.toURI();
                Map<String, String> env = new HashMap<>();

                Path path;
                if ("jar".equals(uri.getScheme())) {
                    String[] array = uri.toString().split("!");
                    try (FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), env)) {
                        path = fs.getPath(array[1]);
                        processPath(path, consumer);
                    } catch (FileSystemNotFoundException | FileSystemAlreadyExistsException e) {
                        try {
                            path = Paths.get(URI.create(array[0])); // Handle jar within a nested jar
                            processPath(path, consumer);
                        }catch (Exception ex){
                            //this is not supposed to be, but if it works, its fine.
                        }
                    }
                } else {
                    path = Paths.get(uri);
                    processPath(path, consumer);
                }
            }
        } catch (Exception e) {
            PriceCxn.getMod().getLogger().error("Error while loading module", e);
        }
    }

    private void processPath(Path path, Consumer<Class<?>> consumer) throws Exception {
        try (Stream<Path> pathStream = Files.walk(path)) {
            pathStream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .filter(p -> !p.toString().toLowerCase().contains("mixin"))
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
                            consumer.accept(loader.loadClass(className));
                        } catch (ClassNotFoundException e) {
                            PriceCxn.getMod().getLogger().error("Error while loading class", e);
                        } catch (RuntimeException e) {
                            PriceCxn.getMod().getLogger().info("Got RuntimeException while loading class {}.", className, e);
                        }
                    });
        }
    }
}