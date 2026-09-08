/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JDK-only, in-process {@link CacheBackend} with:
 * <ul>
 *   <li>a hard size cap (<em>teto</em>) enforced with LRU eviction;</li>
 *   <li>per-entry TTL with lazy expiry on access plus eviction accounting;</li>
 *   <li>hit / miss / eviction metrics via {@link #stats()};</li>
 *   <li>namespace-scoped invalidation keyed on {@link CacheKey#namespace()}.</li>
 * </ul>
 *
 * <p>Thread-safety is provided by a {@link ReentrantReadWriteLock}; the backing
 * {@link LinkedHashMap} is used in access-order mode to implement LRU. The cap
 * defaults to 10&nbsp;000 entries and can be tuned through the system property
 * {@code demoiselle.crud.cache.maxEntries}.</p>
 *
 * @author SERPRO
 */
public class LocalBoundedCacheBackend implements CacheBackend {

    /** System property controlling the maximum number of live entries. */
    public static final String MAX_ENTRIES_PROPERTY = "demoiselle.crud.cache.maxEntries";
    /** Default cap when the system property is absent or invalid. */
    public static final int DEFAULT_MAX_ENTRIES = 10_000;

    private final int maxEntries;

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong evictions = new AtomicLong();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private record CacheEntry(Object value, String namespace, long expiresAt) {}

    private final LinkedHashMap<String, CacheEntry> map;

    public LocalBoundedCacheBackend() {
        this(resolveMaxEntries());
    }

    public LocalBoundedCacheBackend(int maxEntries) {
        this.maxEntries = Math.max(1, maxEntries);
        // Access-order LinkedHashMap; removeEldestEntry enforces the cap.
        this.map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                boolean remove = size() > LocalBoundedCacheBackend.this.maxEntries;
                if (remove) {
                    evictions.incrementAndGet();
                }
                return remove;
            }
        };
    }

    private static int resolveMaxEntries() {
        String raw = System.getProperty(MAX_ENTRIES_PROPERTY);
        if (raw != null && !raw.isBlank()) {
            try {
                int parsed = Integer.parseInt(raw.trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return DEFAULT_MAX_ENTRIES;
    }

    @Override
    public String name() {
        return "local";
    }

    @Override
    public Object get(CacheKey key) {
        String k = key.asString();
        lock.writeLock().lock(); // write lock: access-order mutates the map
        try {
            CacheEntry entry = map.get(k);
            if (entry == null) {
                misses.incrementAndGet();
                return null;
            }
            if (System.currentTimeMillis() > entry.expiresAt()) {
                map.remove(k);
                evictions.incrementAndGet();
                misses.incrementAndGet();
                return null;
            }
            hits.incrementAndGet();
            return entry.value();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(CacheKey key, Object value, long ttlSeconds) {
        long expiresAt = System.currentTimeMillis() + Math.max(0L, ttlSeconds) * 1000L;
        lock.writeLock().lock();
        try {
            map.put(key.asString(), new CacheEntry(value, key.namespace(), expiresAt));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void invalidateNamespace(String namespace) {
        if (namespace == null) {
            return;
        }
        lock.writeLock().lock();
        try {
            Iterator<Map.Entry<String, CacheEntry>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, CacheEntry> e = it.next();
                if (namespace.equals(e.getValue().namespace())) {
                    it.remove();
                    evictions.incrementAndGet();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            map.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public CacheStats stats() {
        lock.readLock().lock();
        try {
            return new CacheStats(hits.get(), misses.get(), evictions.get(), map.size());
        } finally {
            lock.readLock().unlock();
        }
    }

    /** @return the configured maximum number of live entries (the cap/teto). */
    public int getMaxEntries() {
        return maxEntries;
    }
}
