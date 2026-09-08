/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.demoiselle.jee.core.api.idempotency.IdempotencyRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit and concurrency tests for {@link LocalIdempotencyStore}.
 *
 * <p>Validates Requirement (3): atomic slot acquisition, in-progress conflict,
 * replay after completion, TTL expiry.</p>
 */
class LocalIdempotencyStoreTest {

    /** Mutable clock for deterministic TTL tests. */
    private static final class MutableClock extends Clock {
        private Instant now;
        MutableClock(Instant start) { this.now = start; }
        void advance(Duration d) { now = now.plus(d); }
        @Override public Instant instant() { return now; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    @Test
    @DisplayName("First begin wins; second begin sees the in-progress record")
    void beginIsAtomic() {
        LocalIdempotencyStore store = new LocalIdempotencyStore();
        assertTrue(store.begin("fp", 60).isEmpty(), "First caller should win the slot");

        var second = store.begin("fp", 60);
        assertTrue(second.isPresent(), "Second caller must see the existing record");
        assertEquals(IdempotencyRecord.Status.IN_PROGRESS, second.get().status());
    }

    @Test
    @DisplayName("complete transitions the record and enables replay")
    void completeEnablesReplay() {
        LocalIdempotencyStore store = new LocalIdempotencyStore();
        store.begin("fp", 60);
        store.complete("fp", 201, "created".getBytes(), Map.of("X-Test", List.of("1")));

        var record = store.find("fp").orElseThrow();
        assertEquals(IdempotencyRecord.Status.COMPLETED, record.status());
        assertEquals(201, record.httpStatus());
        assertEquals("created", new String(record.body()));
        assertEquals(List.of("1"), record.headers().get("X-Test"));
    }

    @Test
    @DisplayName("abort releases the slot for retry")
    void abortReleasesSlot() {
        LocalIdempotencyStore store = new LocalIdempotencyStore();
        store.begin("fp", 60);
        store.abort("fp");
        assertTrue(store.find("fp").isEmpty());
        assertTrue(store.begin("fp", 60).isEmpty(), "Slot should be free again after abort");
    }

    @Test
    @DisplayName("Expired records are treated as absent (TTL)")
    void ttlExpiry() {
        MutableClock clock = new MutableClock(Instant.parse("2025-01-01T00:00:00Z"));
        LocalIdempotencyStore store = new LocalIdempotencyStore(clock);

        store.begin("fp", 10); // TTL 10s
        assertTrue(store.find("fp").isPresent());

        clock.advance(Duration.ofSeconds(11));
        assertTrue(store.find("fp").isEmpty(), "Record should have expired");
        assertTrue(store.begin("fp", 10).isEmpty(), "Expired slot should be reclaimable");
    }

    @Test
    @DisplayName("Under contention exactly one thread wins begin()")
    void onlyOneWinnerUnderContention() throws Exception {
        LocalIdempotencyStore store = new LocalIdempotencyStore();
        final int threads = 32;
        CyclicBarrier barrier = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger winners = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        try {
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    try {
                        barrier.await(10, TimeUnit.SECONDS);
                        if (store.begin("hot", 60).isEmpty()) {
                            winners.incrementAndGet();
                        } else {
                            conflicts.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // counted as neither → test will fail the equality check
                    } finally {
                        done.countDown();
                    }
                });
            }
            assertTrue(done.await(10, TimeUnit.SECONDS));
            assertEquals(1, winners.get(), "Exactly one thread must acquire the slot");
            assertEquals(threads - 1, conflicts.get(), "All others must see the existing record");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("Fingerprint binds key to principal/method/path/payload")
    void fingerprintDistinguishesInputs() {
        String base = IdempotencyFingerprint.compute("k", "alice", "POST", "/orders", "{}".getBytes(), true);
        assertFalse(base.isEmpty());
        // Same inputs → same fingerprint
        assertEquals(base, IdempotencyFingerprint.compute("k", "alice", "POST", "/orders", "{}".getBytes(), true));
        // Different principal → different
        assertFalse(base.equals(IdempotencyFingerprint.compute("k", "bob", "POST", "/orders", "{}".getBytes(), true)));
        // Different payload → different (when included)
        assertFalse(base.equals(IdempotencyFingerprint.compute("k", "alice", "POST", "/orders", "{\"x\":1}".getBytes(), true)));
        // Payload ignored → payload change does not matter
        String noPayload = IdempotencyFingerprint.compute("k", "alice", "POST", "/orders", "{}".getBytes(), false);
        assertEquals(noPayload, IdempotencyFingerprint.compute("k", "alice", "POST", "/orders", "{\"x\":1}".getBytes(), false));
    }
}
