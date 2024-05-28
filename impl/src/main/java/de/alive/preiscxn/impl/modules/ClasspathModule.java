package de.alive.preiscxn.impl.modules;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.module.Module;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
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
                    try {
                        Tuple2<FileSystem, Boolean> fs = getFileSystem(URI.create(array[0]), env);
                        path = fs.getT1().getPath(array[1]);
                        processPath(path, consumer);
                        if (fs.getT2()) {
                            fs.getT1().close();
                        }
                    } catch (FileSystemNotFoundException | FileSystemAlreadyExistsException e) {
                        try {
                            StringBuilder jarUri = new StringBuilder(array[0].replace("jar:file:", ""));
                            if (!jarUri.toString().contains("!/")) {
                                jarUri.append("!/");
                            }

                            Tuple2<FileSystem, Boolean> fs = getFileSystem(URI.create("jar:file:" + jarUri), env);
                            path = fs.getT1().getPath(array[1]);
                            processPath(path, consumer);
                            if (fs.getT2()) {
                                fs.getT1().close();
                            }
                        } catch (Exception ex) {
                            PriceCxn.getMod().getLogger().error("Error while loading module", ex);
                        }
                    }catch (Exception ex){
                        PriceCxn.getMod().getLogger().error("Error while loading module", ex);
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

    private Tuple2<FileSystem, Boolean> getFileSystem(URI uri, Map<String, String> env) throws IOException {
        try {
            return Tuples.of(FileSystems.newFileSystem(uri, env), true);
        } catch (FileSystemAlreadyExistsException e) {
            return Tuples.of(FileSystems.getFileSystem(uri), false);
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