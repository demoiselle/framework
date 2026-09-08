/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;

/**
 * Default in-memory {@link SecurityStore}.
 *
 * <p>Thread-safe and lock-free at the map level: every mutating operation is
 * performed through {@link ConcurrentHashMap#compute} so the read-modify-write
 * sequence is atomic per key. Entries carry an absolute expiry so stale data is
 * reclaimed lazily on access and, opportunistically, when the global cap is
 * reached.</p>
 *
 * <p>A global cap ({@code demoiselle.security.storeMaxEntries}, default
 * {@code 100000}) bounds memory usage. When the cap is hit the store first
 * purges expired entries; if it is still full a new entry is still admitted but
 * the oldest-expiring entry is evicted, so the store degrades gracefully instead
 * of growing without bound (a defence against key-space flooding attacks).</p>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class LocalSecurityStore implements SecurityStore {

    /** Sentinel meaning "no window expiry". */
    private static final long NEVER = Long.MAX_VALUE;

    /**
     * One entry. For counters {@code count}/{@code windowStart} are used and
     * {@code expiresAt} tracks the window end. For locks/markers only
     * {@code expiresAt} matters.
     */
    private static final class Entry {
        final long count;
        final long windowStart;
        final long expiresAt;

        Entry(long count, long windowStart, long expiresAt) {
            this.count = count;
            this.windowStart = windowStart;
            this.expiresAt = expiresAt;
        }
    }

    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();
    private final AtomicLong evictions = new AtomicLong();

    @Inject
    private Instance<DemoiselleSecurityConfig> config;

    private volatile int maxEntries = 100_000;

    /** No-args constructor for non-CDI usage / tests. */
    public LocalSecurityStore() {
    }

    @PostConstruct
    void init() {
        if (config != null && config.isResolvable()) {
            int configured = config.get().getStoreMaxEntries();
            if (configured > 0) {
                this.maxEntries = configured;
            }
        }
    }

    /** Overrides the cap; intended for tests. */
    public void setMaxEntries(int maxEntries) {
        if (maxEntries > 0) {
            this.maxEntries = maxEntries;
        }
    }

    private String composite(String namespace, String key) {
        return namespace + '\u0000' + key;
    }

    private static boolean expired(Entry e, long now) {
        return e == null || now >= e.expiresAt;
    }

    @Override
    public long incrementAndGet(String namespace, String key, long windowMillis) {
        final long now = System.currentTimeMillis();
        final String k = composite(namespace, key);
        ensureCapacity(now);

        Entry updated = map.compute(k, (ignored, existing) -> {
            if (expired(existing, now)) {
                long expiry = (windowMillis > 0) ? now + windowMillis : NEVER;
                return new Entry(1, now, expiry);
            }
            return new Entry(existing.count + 1, existing.windowStart, existing.expiresAt);
        });
        return updated.count;
    }

    @Override
    public long currentCount(String namespace, String key) {
        final long now = System.currentTimeMillis();
        Entry e = map.get(composite(namespace, key));
        return expired(e, now) ? 0 : e.count;
    }

    @Override
    public long windowStartMillis(String namespace, String key) {
        final long now = System.currentTimeMillis();
        Entry e = map.get(composite(namespace, key));
        return expired(e, now) ? -1 : e.windowStart;
    }

    @Override
    public void lock(String namespace, String key, long untilEpochMillis) {
        final long now = System.currentTimeMillis();
        ensureCapacity(now);
        map.put(composite(namespace, key), new Entry(0, now, untilEpochMillis));
    }

    @Override
    public long lockRemainingMillis(String namespace, String key) {
        final long now = System.currentTimeMillis();
        final String k = composite(namespace, key);
        Entry e = map.get(k);
        if (e == null) {
            return -1;
        }
        if (now >= e.expiresAt) {
            // Clear expired lock atomically only if unchanged.
            map.remove(k, e);
            return -1;
        }
        return e.expiresAt - now;
    }

    @Override
    public boolean markIfAbsent(String namespace, String key, long ttlMillis) {
        final long now = System.currentTimeMillis();
        final String k = composite(namespace, key);
        ensureCapacity(now);

        long expiry = (ttlMillis > 0) ? now + ttlMillis : NEVER;
        final boolean[] inserted = {false};
        map.compute(k, (ignored, existing) -> {
            if (expired(existing, now)) {
                inserted[0] = true;
                return new Entry(1, now, expiry);
            }
            // Still live → replay, keep existing.
            return existing;
        });
        return inserted[0];
    }

    @Override
    public void remove(String namespace, String key) {
        map.remove(composite(namespace, key));
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public long size() {
        return map.size();
    }

    /** Number of forced evictions performed under cap pressure (observability). */
    public long evictionCount() {
        return evictions.get();
    }

    /**
     * Keeps the store within {@link #maxEntries}. First purges expired entries;
     * if still at/above the cap, evicts the entry with the nearest expiry so a
     * flood of distinct keys cannot exhaust memory.
     */
    private void ensureCapacity(long now) {
        if (map.size() < maxEntries) {
            return;
        }
        // Cheap first pass: drop expired entries.
        map.entrySet().removeIf(e -> now >= e.getValue().expiresAt);
        if (map.size() < maxEntries) {
            return;
        }
        // Still full: evict the soonest-expiring live entry.
        String victim = null;
        long soonest = Long.MAX_VALUE;
        for (Map.Entry<String, Entry> e : map.entrySet()) {
            long exp = e.getValue().expiresAt;
            if (exp < soonest) {
                soonest = exp;
                victim = e.getKey();
            }
        }
        if (victim != null && map.remove(victim) != null) {
            evictions.incrementAndGet();
        }
    }
}
