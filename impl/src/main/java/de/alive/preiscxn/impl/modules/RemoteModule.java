package de.alive.preiscxn.impl.modules;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.module.Module;
import de.alive.preiscxn.impl.Version;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;



public final class RemoteModule implements Module {
    private final String remotePath;
    private final Path jarPath;
    private final String primaryPackage;
    private final ClassLoader classLoader;
    private boolean isDownloaded = false;

    private RemoteModule(String remotePath, Path jarPath, String primaryPackage, ClassLoader classLoader) {
        this.remotePath = remotePath;
        this.jarPath = jarPath;
        this.primaryPackage = primaryPackage;
        this.classLoader = classLoader;
    }

    public static Mono<Module> create(String remotePath, Path jarPath, String primaryPackage, boolean useRemote, ClassLoader classLoader) {

        if (!useRemote)
            return Mono.just(new ClasspathModule(primaryPackage));

        PriceCxn.getMod().getLogger().info("Creating remote module with remotePath: {}, jarPath: {}, primaryPackage: {}", remotePath, jarPath, primaryPackage);
        RemoteModule remoteModule = new RemoteModule(remotePath, jarPath, primaryPackage, classLoader);

        return remoteModule
                .isOutdated()
                .flatMap(outdated -> {
                    if (outdated)
                        return remoteModule.download();
                    else {
                        remoteModule.isDownloaded = true;
                        return Mono.empty();
                    }
                })
                .then(Mono.just(remoteModule));
    }

    private Mono<Void> download() {
        if (remotePath == null)
            return Mono.empty();

        return PriceCxn.getMod().getCdnFileHandler().getFile(remotePath, null)
                .switchIfEmpty(Mono.error(new RuntimeException("Could not download module from " + remotePath)))
                .doOnNext(bytes -> PriceCxn.getMod().getLogger().info("Downloaded module from {} with length: {}", remotePath, bytes.length))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(content -> {
                    try {
                        if (Files.notExists(this.jarPath.getParent()))
                            Files.createDirectory(this.jarPath.getParent());

                        Files.write(jarPath, content);

                        this.isDownloaded = true;
                    } catch (IOException e) {
                        throw new RuntimeException("Could not write file", e);
                    }
                })
                .doOnError(throwable -> PriceCxn.getMod().getLogger().error("Error while downloading module", throwable))
                .then();
    }

    private Mono<Boolean> isOutdated() {
        if (remotePath == null)
            return Mono.just(false);

        if (!Files.exists(jarPath))
            return Mono.just(true);

        return Mono
                .zip(calculateFileHash(jarPath)
                                .doOnNext(s -> PriceCxn.getMod().getLogger().info("Calculated hash for {}: {}...", jarPath, s.substring(0, 10))),
                        getUrlHash()
                                .doOnNext(s -> PriceCxn.getMod().getLogger().info("Got hash from remote: {}...", s.substring(0, 10))))
                .map(tuple -> !tuple.getT1().equals(tuple.getT2()))
                .switchIfEmpty(Mono.just(true));
    }

    private Mono<String> calculateFileHash(Path filePath) {
        return Mono.fromCallable(() -> {
            PriceCxn.getMod().getLogger().info("Calculating hash for {}", filePath);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(Files.readAllBytes(filePath));
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
        return PriceCxn.getMod().getCdnFileHandler().getHash(remotePath, Version.MOD_VERSION);

    }

    @Override
    public String toString() {
        return "RemoteModule{"
                + "remotePath='" + remotePath + '\''
                + ", jarPath=" + jarPath
                + ", primaryPackage='" + primaryPackage + '\''
                + '}';
    }

    @Override
    public void forEach(Consumer<Class<?>> consumer) {
        if (!isDownloaded) {
            PriceCxn.getMod().getLogger().error("Module not downloaded");
            return;
        }
        PriceCxn.getMod().getLogger().info("Loading module from {}", jarPath);
        try (JarFile jarFile = new JarFile(jarPath.toFile());
             URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, Thread.currentThread().getContextClassLoader())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace(".class", "")
                            .replace("/", ".");
                    if (className.startsWith(primaryPackage)) {
                        try {
                            consumer.accept(urlClassLoader.loadClass(className));
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            PriceCxn.getMod().getLogger().error("Error while loading class {}", className);
                        }
                    }
                }
            }
        } catch (IOException e) {
            PriceCxn.getMod().getLogger().error("Error while loading module", e);
        }
    }
}
