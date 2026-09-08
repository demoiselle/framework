/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.store;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LocalSecurityStoreTest {

    @Test
    void incrementsAreExactUnderConcurrency() throws Exception {
        LocalSecurityStore store = new LocalSecurityStore();
        int threads = 16;
        int perThread = 5_000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        store.incrementAndGet("ns", "k", 60_000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals((long) threads * perThread, store.currentCount("ns", "k"));
    }

    @Test
    void windowExpiryResetsCounter() throws Exception {
        LocalSecurityStore store = new LocalSecurityStore();
        store.incrementAndGet("ns", "k", 50);
        store.incrementAndGet("ns", "k", 50);
        assertEquals(2, store.currentCount("ns", "k"));
        Thread.sleep(70);
        // Expired → next increment starts a fresh window at 1
        assertEquals(0, store.currentCount("ns", "k"));
        assertEquals(1, store.incrementAndGet("ns", "k", 50));
    }

    @Test
    void lockReportsRemainingAndExpires() throws Exception {
        LocalSecurityStore store = new LocalSecurityStore();
        store.lock("lock", "ip", System.currentTimeMillis() + 60);
        assertTrue(store.lockRemainingMillis("lock", "ip") > 0);
        Thread.sleep(80);
        assertEquals(-1, store.lockRemainingMillis("lock", "ip"));
    }

    @Test
    void markIfAbsentIsSingleUseUnderConcurrency() throws Exception {
        LocalSecurityStore store = new LocalSecurityStore();
        int threads = 32;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger winners = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    if (store.markIfAbsent("replay", "jti-1", 60_000)) {
                        winners.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(1, winners.get(), "Exactly one caller may insert the marker");
    }

    @Test
    void markIfAbsentExpires() throws Exception {
        LocalSecurityStore store = new LocalSecurityStore();
        assertTrue(store.markIfAbsent("replay", "jti", 40));
        assertFalse(store.markIfAbsent("replay", "jti", 40));
        Thread.sleep(60);
        assertTrue(store.markIfAbsent("replay", "jti", 40), "Marker should be reusable after TTL");
    }

    @Test
    void capBoundsSize() {
        LocalSecurityStore store = new LocalSecurityStore();
        store.setMaxEntries(100);
        for (int i = 0; i < 5_000; i++) {
            store.incrementAndGet("ns", "key-" + i, 60_000);
        }
        assertTrue(store.size() <= 100, "Store must not exceed the cap, was " + store.size());
        assertTrue(store.evictionCount() > 0, "Evictions should have occurred");
    }

    @Test
    void namespacesAreIsolated() {
        LocalSecurityStore store = new LocalSecurityStore();
        store.incrementAndGet("a", "k", 60_000);
        store.incrementAndGet("a", "k", 60_000);
        store.incrementAndGet("b", "k", 60_000);
        assertEquals(2, store.currentCount("a", "k"));
        assertEquals(1, store.currentCount("b", "k"));
    }
}
