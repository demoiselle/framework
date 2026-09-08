/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Application-scoped store for query result caching.
 *
 * <p>This class is now a thin façade over a pluggable {@link CacheBackend}
 * (defaulting to the JDK-only {@link LocalBoundedCacheBackend}). It preserves
 * the historical string-key API used by {@link CacheInterceptor} and existing
 * callers while adding structured keys, a size cap, eviction and metrics through
 * the backend.</p>
 *
 * <p>Cache keys follow the convention {@code entityClassName:discriminator:params},
 * enabling bulk invalidation by entity class via {@link #invalidateByEntityClass(Class)}.
 * The entity class name is used as the {@link CacheKey#namespace() namespace}.</p>
 *
 * <p>Validates: Requirements 7.8</p>
 */
@ApplicationScoped
public class QueryCacheStore {

    @Inject
    private CacheBackend backend;

    @PostConstruct
    void init() {
        if (backend == null) {
            backend = new LocalBoundedCacheBackend();
        }
    }

    /** Ensures a backend exists even when instantiated outside CDI (tests). */
    private CacheBackend backend() {
        if (backend == null) {
            backend = new LocalBoundedCacheBackend();
        }
        return backend;
    }

    /**
     * Retrieves a cached value by key, returning {@code null} if the entry
     * does not exist or has expired.
     *
     * @param key the cache key (legacy string form {@code namespace:discriminator:params})
     * @return the cached value, or {@code null} if absent or expired
     */
    public Object get(String key) {
        return backend().get(toCacheKey(key));
    }

    /**
     * Stores a value in the cache with the given TTL.
     *
     * @param key        the cache key
     * @param value      the value to cache
     * @param ttlSeconds time-to-live in seconds
     */
    public void put(String key, Object value, long ttlSeconds) {
        backend().put(toCacheKey(key), value, ttlSeconds);
    }

    /**
     * Retrieves a cached value using a structured {@link CacheKey}.
     *
     * @param key the structured key
     * @return the cached value, or {@code null}
     */
    public Object get(CacheKey key) {
        return backend().get(key);
    }

    /**
     * Stores a value using a structured {@link CacheKey}.
     *
     * @param key        the structured key
     * @param value      the value to cache
     * @param ttlSeconds time-to-live in seconds
     */
    public void put(CacheKey key, Object value, long ttlSeconds) {
        backend().put(key, value, ttlSeconds);
    }

    /**
     * Invalidates all cache entries whose namespace is the fully qualified
     * class name of the given entity class.
     *
     * @param entityClass the entity class whose cached queries should be invalidated
     */
    public void invalidateByEntityClass(Class<?> entityClass) {
        backend().invalidateNamespace(entityClass.getName());
    }

    /**
     * @return a snapshot of the backend metrics (hits, misses, evictions, size)
     */
    public CacheStats stats() {
        return backend().stats();
    }

    /**
     * Adapts a legacy string key of the form {@code namespace:discriminator:params}
     * into a structured {@link CacheKey}. The namespace is the substring before
     * the first {@code ':'}; the remainder becomes the discriminator so that the
     * full key round-trips to a unique {@link CacheKey#asString()}.
     */
    private static CacheKey toCacheKey(String key) {
        if (key == null) {
            return new CacheKey("", "", "");
        }
        int sep = key.indexOf(':');
        if (sep < 0) {
            return new CacheKey(key, "", "");
        }
        String namespace = key.substring(0, sep);
        String remainder = key.substring(sep + 1);
        return new CacheKey(namespace, remainder, "");
    }
}
