/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowCounterConcurrencyTest {

    @Test
    void allowsExactlyMaxRequestsUnderConcurrency() throws Exception {
        SlidingWindowCounter counter = new SlidingWindowCounter(
                new org.demoiselle.jee.security.store.LocalSecurityStore());
        int max = 100;
        int attempts = 1_000;
        int threads = 20;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowed = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < attempts / threads; i++) {
                        if (counter.recordAndCheck("shared-key", max, 60) < 0) {
                            allowed.incrementAndGet();
                        } else {
                            rejected.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(max, allowed.get(), "Exactly max requests must be allowed");
        assertEquals(attempts - max, rejected.get(), "All others must be rejected");
    }

    @Test
    void retryAfterWithinBounds() {
        SlidingWindowCounter counter = new SlidingWindowCounter(
                new org.demoiselle.jee.security.store.LocalSecurityStore());
        for (int i = 0; i < 3; i++) {
            assertEquals(-1, counter.recordAndCheck("k", 3, 30));
        }
        int retry = counter.recordAndCheck("k", 3, 30);
        assertTrue(retry >= 1 && retry <= 30, "Retry-After must be within (0, window], was " + retry);
    }
}
