/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

/**
 * Service Provider Interface (SPI) for query-result cache backends.
 *
 * <p>The framework ships a JDK-only, in-process bounded backend
 * ({@link LocalBoundedCacheBackend}). Distributed backends (e.g. Redis,
 * Hazelcast, Infinispan) can be supplied as adapters implementing this
 * interface and registered either as CDI beans or through
 * {@link java.util.ServiceLoader}. No external SDKs are bundled by the
 * framework itself.</p>
 *
 * <h2>Keys</h2>
 * <p>Backends receive {@link CacheKey}s, which carry an entity {@code namespace},
 * an operation {@code discriminator} and a structured parameter signature.
 * Backends must key on the whole structure (via {@link CacheKey#asString()}),
 * not on a bare numeric hash, and must support namespace-scoped invalidation.</p>
 *
 * @author SERPRO
 */
public interface CacheBackend {

    /**
     * A short, stable identifier for this backend (e.g. {@code local}, {@code redis}).
     *
     * @return the backend name
     */
    String name();

    /**
     * Retrieves a live cached value, or {@code null} when absent or expired.
     * Implementations must update hit/miss metrics accordingly.
     *
     * @param key the structured cache key
     * @return the cached value, or {@code null}
     */
    Object get(CacheKey key);

    /**
     * Stores a value with the given time-to-live.
     *
     * @param key        the structured cache key
     * @param value      the value to cache
     * @param ttlSeconds time-to-live in seconds
     */
    void put(CacheKey key, Object value, long ttlSeconds);

    /**
     * Invalidates every entry that belongs to the given namespace (entity).
     *
     * @param namespace the entity namespace to purge
     */
    void invalidateNamespace(String namespace);

    /**
     * Removes all entries from the backend.
     */
    void clear();

    /**
     * @return a snapshot of the backend metrics (hits, misses, evictions, size)
     */
    CacheStats stats();
}
