package de.alive.pricecxn.modules;

import de.alive.pricecxn.networking.cdn.CdnDeliveryType;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

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

import static de.alive.pricecxn.LogPrinter.LOGGER;

public class ModuleLoader {

    private final Tuple2<ClassLoader, Package> defaultPackage;
    private final String remotePath;
    private final Path jarPath;

    public ModuleLoader(Tuple2<ClassLoader, Package> defaultPackage, String remotePath, Path jarPath) {
        this.defaultPackage = defaultPackage;
        this.remotePath = remotePath;
        this.jarPath = jarPath;
        try{
            if (Files.notExists(this.jarPath.getParent()))
                Files.createDirectory(this.jarPath.getParent());
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public <I> Mono<List<Class<? extends I>>> loadInterfaces(Class<I> interfaceClass) {
        if(this.defaultPackage != null){
            return Mono.just(getInterfacesFromPackage(interfaceClass));
        }
        return isOutdated()
                .flatMap(outdated -> {
                    LOGGER.info("Module ({}) is outdated: {}", remotePath, outdated);
                    if (outdated) {
                        return download()
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
            Enumeration<URL> resources = defaultPackage.getT1().getResources(defaultPackage.getT2().getName().replace(".", "/"));

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Path path = Path.of(url.toURI());
                Files.walk(path)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".class"))
                        .forEach(p -> {
                            String className = p.toString()
                                    .replace(path.toString(), "")
                                    .replace(".class", "")
                                    .replace("/", ".")
                                    .substring(1);
                            try{
                                Class<?> clazz = defaultPackage.getT1().loadClass(defaultPackage.getT2().getName() + "." + className);
                                if (interfaceClass.isAssignableFrom(clazz)) {
                                    list.add(clazz.asSubclass(interfaceClass));
                                }
                            }catch(ClassNotFoundException e){
                                LOGGER.error("Error while loading module", e);
                            }
                        });
            }
        }catch(Exception e){
            LOGGER.error("Error while loading module", e);
        }
        return list;
    }

    private Mono<Void> download() {
        return CdnDeliveryType.FILE.generateResponseAsBytes(remotePath)
                .doOnNext(bytes -> LOGGER.info("Downloaded module from {}", remotePath))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(content -> {
                    try{
                        Files.write(jarPath, content);
                    }catch(IOException e){
                        throw new RuntimeException("Could not write file", e);
                    }
                })
                .then();
    }

    private Mono<Boolean> isOutdated() {
        if (!Files.exists(jarPath))
            return Mono.just(true);

        return Mono
                .zip(calculateFileHash(jarPath), getUrlHash())
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
        return CdnDeliveryType.HASH.generateResponse(remotePath)
                .doOnNext(hash -> LOGGER.info("Got hash from remote: /{}", remotePath));

    }

    @SuppressWarnings("unchecked")
    public <I> List<Class<? extends I>> getInterfaces(Class<I> interfaceClass) {
        LOGGER.info("Loading interfaces from {}", jarPath);
        List<Class<? extends I>> list = new ArrayList<>();
        try{
            URL url = jarPath.toUri().toURL();
            URL[] urls = new URL[]{url};

            try(URLClassLoader classLoader = new URLClassLoader(urls)){

                try(JarFile jarFile = new JarFile(jarPath.toFile())){

                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();

                        if (entryName.endsWith(".class")) {
                            String className = entryName.replace("/", ".").replace(".class", "");

                            Class<?> clazz = classLoader.loadClass(className);

                            if (interfaceClass.getName().equals(clazz.getSuperclass().getName())) {
                                list.add((Class<? extends I>) clazz);
                            }
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
