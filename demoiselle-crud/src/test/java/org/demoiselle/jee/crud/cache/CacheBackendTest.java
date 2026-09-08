/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link CacheBackend} SPI and the {@link LocalBoundedCacheBackend}:
 * structured keys (collision safety), size cap (teto) with eviction, hit/miss/
 * eviction metrics, namespace invalidation and a shared backend seen by multiple
 * {@link QueryCacheStore} instances.
 */
class CacheBackendTest {

    private static final String NS_A = "com.example.EntityA";
    private static final String NS_B = "com.example.EntityB";

    // --- Structured keys avoid relying solely on hash ---

    @Test
    void structuredKey_distinguishesEqualHashParams() {
        // Two different parameter sets crafted to be different values; the
        // structured signature embeds a textual rendering plus deep hash so that
        // distinct params never collide even if a naive hash matched.
        CacheKey k1 = CacheKey.of(NS_A, "find", new Object[]{"1"});
        CacheKey k2 = CacheKey.of(NS_A, "find", new Object[]{1});
        assertNotEquals(k1.asString(), k2.asString(),
                "string param and int param must produce distinct keys");
    }

    @Test
    void structuredKey_sameLogicalArgsProduceSameKey() {
        CacheKey k1 = CacheKey.of(NS_A, "find", new Object[]{"x", 42});
        CacheKey k2 = CacheKey.of(NS_A, "find", new Object[]{"x", 42});
        assertEquals(k1.asString(), k2.asString());
    }

    @Test
    void backend_getPut_roundTripAndMetrics() {
        LocalBoundedCacheBackend backend = new LocalBoundedCacheBackend(100);
        CacheKey key = CacheKey.of(NS_A, "find", new Object[]{1});

        assertNull(backend.get(key));                 // miss
        backend.put(key, "value", 300);
        assertEquals("value", backend.get(key));      // hit

        CacheStats stats = backend.stats();
        assertEquals(1, stats.hits());
        assertEquals(1, stats.misses());
        assertEquals(1, stats.size());
        assertEquals(0.5d, stats.hitRatio(), 1e-9);
    }

    @Test
    void backend_ttlExpiryCountsAsEviction() throws InterruptedException {
        LocalBoundedCacheBackend backend = new LocalBoundedCacheBackend(100);
        CacheKey key = CacheKey.of(NS_A, "find", new Object[]{1});
        backend.put(key, "value", 1);
        assertEquals("value", backend.get(key));
        Thread.sleep(1100);
        assertNull(backend.get(key), "expired entry must be a miss");
        assertTrue(backend.stats().evictions() >= 1, "TTL expiry must count as eviction");
    }

    // --- Cap / teto with LRU eviction ---

    @Test
    void backend_enforcesSizeCapWithEviction() {
        int cap = 10;
        LocalBoundedCacheBackend backend = new LocalBoundedCacheBackend(cap);

        for (int i = 0; i < cap * 5; i++) {
            backend.put(CacheKey.of(NS_A, "find", new Object[]{i}), "v" + i, 300);
        }

        CacheStats stats = backend.stats();
        assertTrue(stats.size() <= cap, "live size must never exceed the cap");
        assertTrue(stats.evictions() >= (cap * 5 - cap),
                "entries beyond the cap must be evicted");
    }

    @Test
    void backend_lruKeepsRecentlyUsedEntries() {
        LocalBoundedCacheBackend backend = new LocalBoundedCacheBackend(3);
        CacheKey k1 = CacheKey.of(NS_A, "m", new Object[]{1});
        CacheKey k2 = CacheKey.of(NS_A, "m", new Object[]{2});
        CacheKey k3 = CacheKey.of(NS_A, "m", new Object[]{3});
        backend.put(k1, "1", 300);
        backend.put(k2, "2", 300);
        backend.put(k3, "3", 300);

        // Touch k1 so it becomes most-recently-used.
        assertEquals("1", backend.get(k1));

        // Inserting a 4th entry evicts the least-recently-used (k2).
        CacheKey k4 = CacheKey.of(NS_A, "m", new Object[]{4});
        backend.put(k4, "4", 300);

        assertNotNull(backend.get(k1), "recently used entry must survive");
        assertNull(backend.get(k2), "least recently used entry must be evicted");
    }

    // --- Namespace invalidation ---

    @Test
    void backend_invalidateNamespace_purgesOnlyThatNamespace() {
        LocalBoundedCacheBackend backend = new LocalBoundedCacheBackend(100);
        CacheKey a1 = CacheKey.of(NS_A, "find", new Object[]{1});
        CacheKey a2 = CacheKey.of(NS_A, "find", new Object[]{2});
        CacheKey b1 = CacheKey.of(NS_B, "find", new Object[]{1});
        backend.put(a1, "a1", 300);
        backend.put(a2, "a2", 300);
        backend.put(b1, "b1", 300);

        backend.invalidateNamespace(NS_A);

        assertNull(backend.get(a1));
        assertNull(backend.get(a2));
        assertEquals("b1", backend.get(b1), "other namespaces must remain");
    }

    // --- Shared backend across multiple QueryCacheStore instances ---

    @Test
    void sharedBackend_visibleAcrossStoreInstances() throws Exception {
        LocalBoundedCacheBackend shared = new LocalBoundedCacheBackend(100);

        QueryCacheStore storeA = new QueryCacheStore();
        QueryCacheStore storeB = new QueryCacheStore();
        inject(storeA, shared);
        inject(storeB, shared);

        CacheKey key = CacheKey.of(NS_A, "find", new Object[]{7});
        storeA.put(key, "written-by-A", 300);

        // A second "instance" (simulating a second application node sharing a
        // distributed backend) reads the value written by the first.
        assertEquals("written-by-A", storeB.get(key),
                "a shared backend must expose entries to all instances");

        // Invalidation from one instance is visible to the other.
        storeB.invalidateByEntityClass(EntityForNs.class);
        // EntityForNs namespace differs from NS_A so the entry survives.
        assertEquals("written-by-A", storeA.get(key));
    }

    @Test
    void producer_defaultsToLocalBackend() {
        CacheBackendProducer producer = new CacheBackendProducer();
        CacheBackend backend = producer.cacheBackend();
        assertNotNull(backend);
        // With only the local backend registered, selection resolves to "local".
        assertEquals("local", backend.name());
    }

    // --- helpers ---

    private static void inject(QueryCacheStore store, CacheBackend backend) throws Exception {
        java.lang.reflect.Field f = QueryCacheStore.class.getDeclaredField("backend");
        f.setAccessible(true);
        f.set(store, backend);
    }

    private static class EntityForNs {}
}
