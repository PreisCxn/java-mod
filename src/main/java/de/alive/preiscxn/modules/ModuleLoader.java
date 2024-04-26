package de.alive.preiscxn.modules;

import de.alive.api.PriceCxn;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static de.alive.api.LogPrinter.LOGGER;

public class ModuleLoader {

    private final Package defaultPackage;
    private final String remotePath;
    private final Path jarPath;

    public ModuleLoader(Package defaultPackage, String remotePath, Path jarPath) {
        this.defaultPackage = defaultPackage;
        this.remotePath = remotePath;
        this.jarPath = jarPath;
    }

    public ModuleLoader(Package defaultPackage){
        this(defaultPackage, null, null);
    }

    public <I> Mono<List<Class<? extends I>>> loadInterfaces(Class<I> interfaceClass) {
        if(this.defaultPackage != null){
            return Mono.just(getInterfacesFromPackage(interfaceClass));
        }
        return isOutdated()
                .flatMap(outdated -> {
                    LOGGER.info("Module ({}) is outdated: {}", remotePath, outdated);
                    if (outdated) {
                        return download().then(calculateFileHash(jarPath).doOnNext(s -> LOGGER.info("Calculated hash for downloaded {}: {}...", jarPath, s.substring(0, 10))))
                                .then(Mono.fromCallable(() -> getInterfaces(interfaceClass)))
                                .subscribeOn(Schedulers.boundedElastic());
                    } else {
                        return Mono.fromCallable(() -> getInterfaces(interfaceClass))
                                .subscribeOn(Schedulers.boundedElastic());
                    }
                });

    }

    private <I> List<Class<? extends I>> getInterfacesFromPackage(Class<I> interfaceClass) {
        LOGGER.info("Loading interfaces from package {}", defaultPackage);
        List<Class<? extends I>> list = new ArrayList<>();
        try{
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(defaultPackage.getName().replace(".", File.separator));

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
                                int index = className.indexOf("de.alive.preiscxn");
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

    private Mono<Void> download() {
        if(remotePath == null)
            return Mono.empty();

        return PriceCxn.getMod().getCdnFileHandler().getFile(remotePath, null)
                .doOnNext(bytes -> LOGGER.info("Downloaded module from {}", remotePath))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(content -> {
                    try{
                        if (Files.notExists(this.jarPath.getParent()))
                            Files.createDirectory(this.jarPath.getParent());

                        Files.write(jarPath, content);
                    }catch(IOException e){
                        throw new RuntimeException("Could not write file", e);
                    }
                })
                .then();
    }

    private Mono<Boolean> isOutdated() {
        if(remotePath == null)
            return Mono.just(false);

        if (!Files.exists(jarPath))
            return Mono.just(true);

        return Mono
                .zip(calculateFileHash(jarPath)
                                .doOnNext(s -> LOGGER.info("Calculated hash for {}: {}...", jarPath, s.substring(0, 10))),
                        getUrlHash()
                                .doOnNext(s -> LOGGER.info("Got hash from remote: {}...", s.substring(0, 10))))
                .map(tuple -> !tuple.getT1().equals(tuple.getT2()))
                .switchIfEmpty(Mono.just(true));
    }

    private Mono<String> calculateFileHash(Path filePath) {
        return Mono.fromCallable(() -> {
            LOGGER.info("Calculating hash for {}", filePath);
            try{
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                try(DigestInputStream dis = new DigestInputStream(Files.newInputStream(filePath), md)){
                    while (dis.read() != -1) ;
                    md = dis.getMessageDigest();
                }

                StringBuilder result = new StringBuilder();
                for (byte b : md.digest()) {
                    result.append(String.format("%02x", b));
                }
                return result.toString();

            }catch(Exception e){
                throw new RuntimeException("Could not calculate hash", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> getUrlHash() {
        return PriceCxn.getMod().getCdnFileHandler().getHash(remotePath, null);

    }

    public <I> List<Class<? extends I>> getInterfaces(Class<I> interfaceClass) {
        LOGGER.info("Loading interfaces from {}", jarPath);
        List<Class<? extends I>> list = new ArrayList<>();
        try{
            URL url = jarPath.toUri().toURL();
            URL[] urls = new URL[]{url};

            // Verwenden Sie den ClassLoader der aktuellen Klasse
            URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());

            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    if (entryName.endsWith(".class")) {
                        String className = entryName.replace("/", ".").replace(".class", "");
                        // Verwenden Sie den ClassLoader der aktuellen Klasse, um die Klasse zu laden
                        Class<?> clazz = Class.forName(className, true, classLoader);

                        if (interfaceClass.getName().equals(clazz.getSuperclass().getName())) {
                            list.add(clazz.asSubclass(interfaceClass));
                        }
                    }
                }
            }

        }catch(Exception e){
            LOGGER.error("Error while loading module", e);
        }
        return list;
    }
}
