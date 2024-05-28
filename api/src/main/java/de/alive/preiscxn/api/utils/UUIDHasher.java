package de.alive.preiscxn.api.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.UUID;

public class UUIDHasher {
    private static final Cache<UUID, UUID> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build();

    public static UUID hash(UUID uuid) {
        if(cache.getIfPresent(uuid) != null) {
            return cache.getIfPresent(uuid);
        }

        UUID uuid1 = UUID.nameUUIDFromBytes(uuid.toString().getBytes());
        cache.put(uuid, uuid1);
        return uuid1;
    }

}
