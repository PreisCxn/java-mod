package de.alive.preiscxn.modules;

import de.alive.api.PriceCxn;
import de.alive.api.module.Module;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import static de.alive.api.LogPrinter.LOGGER;

public class RemoteModule implements Module {
    private final String remotePath;
    private final Path jarPath;
    private final Package defaultPackage;
    private ClassLoader parentClassLoader = null;
    private ClassLoader moduleClassLoader = null;

    private RemoteModule(String remotePath, Path jarPath, Package defaultPackage) {
        this.remotePath = remotePath;
        this.jarPath = jarPath;
        this.defaultPackage = defaultPackage;
    }

    public static Mono<Module> create(String remotePath, Path jarPath, Package defaultPackage) {
        RemoteModule remoteModule = new RemoteModule(remotePath, jarPath, defaultPackage);

        if(defaultPackage != null)
            return Mono.just(remoteModule);

        return remoteModule
                .isOutdated()
                .filter(aBoolean -> aBoolean)
                .flatMap(outdated -> remoteModule.download())
                .then(Mono.just(remoteModule));
    }

    @Override
    public void load(ClassLoader parentClassloader) {
        if(parentClassloader == null)
            throw new IllegalArgumentException("Parent classloader must not be null");

        this.parentClassLoader = parentClassloader;
    }

    @Override
    public ClassLoader getModuleClassLoader() {
        if(moduleClassLoader == null)
            moduleClassLoader = createClassLoader();

        return moduleClassLoader;
    }

    private ClassLoader createClassLoader() {
        if(this.parentClassLoader == null)
            throw new IllegalStateException("Parent classloader must not be null (call load first)");

        if(defaultPackage != null)
            return this.parentClassLoader;

        LOGGER.info("Loading interfaces from {}", jarPath);
        try {
            URL url = jarPath.toUri().toURL();
            URL[] urls = new URL[]{url};

            return new URLClassLoader(urls, this.parentClassLoader == null ? this.getClass().getClassLoader() : this.parentClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<Void> download() {
        if (remotePath == null)
            return Mono.empty();

        return PriceCxn.getMod().getCdnFileHandler().getFile(remotePath, null)
                .doOnNext(bytes -> LOGGER.info("Downloaded module from {}", remotePath))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(content -> {
                    try {
                        if (Files.notExists(this.jarPath.getParent()))
                            Files.createDirectory(this.jarPath.getParent());

                        Files.write(jarPath, content);
                    } catch (IOException e) {
                        throw new RuntimeException("Could not write file", e);
                    }
                })
                .then();
    }

    private Mono<Boolean> isOutdated() {
        if (remotePath == null)
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
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                try (DigestInputStream dis = new DigestInputStream(Files.newInputStream(filePath), md)) {
                    while (dis.read() != -1) ;
                    md = dis.getMessageDigest();
                }

                StringBuilder result = new StringBuilder();
                for (byte b : md.digest()) {
                    result.append(String.format("%02x", b));
                }
                return result.toString();

            } catch (Exception e) {
                throw new RuntimeException("Could not calculate hash", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> getUrlHash() {
        return PriceCxn.getMod().getCdnFileHandler().getHash(remotePath, null);

    }

}
