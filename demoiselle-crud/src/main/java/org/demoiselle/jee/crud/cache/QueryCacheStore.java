/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application-scoped store for query result caching with lazy expiration.
 *
 * <p>Uses a {@link ConcurrentHashMap} internally with per-entry TTL tracking.
 * Expired entries are removed lazily on access (no background eviction thread).</p>
 *
 * <p>Cache keys follow the convention {@code entityClassName:methodName:paramsHash},
 * enabling bulk invalidation by entity class via {@link #invalidateByEntityClass(Class)}.</p>
 *
 * <p>Validates: Requirements 7.8</p>
 */
@ApplicationScoped
public class QueryCacheStore {

    private record CacheEntry(Object value, long expiresAt) {}

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached value by key, returning {@code null} if the entry
     * does not exist or has expired (lazy expiration).
     *
     * @param key the cache key
     * @return the cached value, or {@code null} if absent or expired
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() > entry.expiresAt()) {
            cache.remove(key);
            return null;
        }
        return entry.value();
    }

    /**
     * Stores a value in the cache with the given TTL.
     *
     * @param key        the cache key
     * @param value      the value to cache
     * @param ttlSeconds time-to-live in seconds
     */
    public void put(String key, Object value, long ttlSeconds) {
        long expiresAt = System.currentTimeMillis() + (ttlSeconds * 1000);
        cache.put(key, new CacheEntry(value, expiresAt));
    }

    /**
     * Invalidates all cache entries whose key starts with the fully qualified
     * class name of the given entity class followed by {@code ":"}.
     *
     * @param entityClass the entity class whose cached queries should be invalidated
     */
    public void invalidateByEntityClass(Class<?> entityClass) {
        String prefix = entityClass.getName() + ":";
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }
}
