package de.alive.preiscxn.modules;

import de.alive.api.PriceCxn;
import de.alive.api.module.Module;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import static de.alive.api.LogPrinter.LOGGER;

public class RemoteModule implements Module {
    private final String remotePath;
    private final Path jarPath;
    private final String primaryPackage;

    private RemoteModule(String remotePath, Path jarPath, String primaryPackage) {
        this.remotePath = remotePath;
        this.jarPath = jarPath;
        this.primaryPackage = primaryPackage;
    }

    public static Mono<Module> create(String remotePath, Path jarPath, String primaryPackage) {
        RemoteModule remoteModule = new RemoteModule(remotePath, jarPath, primaryPackage);

        if(primaryPackage != null)
            return Mono.just(remoteModule);

        return remoteModule
                .isOutdated()
                .filter(aBoolean -> aBoolean)
                .flatMap(outdated -> remoteModule.download())
                .then(Mono.just(remoteModule));
    }

    @Override
    public String getPrimaryPackage() {
        return primaryPackage;
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
