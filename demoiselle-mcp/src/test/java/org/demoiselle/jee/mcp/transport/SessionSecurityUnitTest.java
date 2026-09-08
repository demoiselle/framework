/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SessionSecurityUnitTest {

    // --- SessionFingerprint ---

    @Test
    void fingerprintIsDeterministicAndNonReversible() {
        String fp1 = SessionFingerprint.of("alice");
        String fp2 = SessionFingerprint.of("alice");
        assertEquals(fp1, fp2, "same input → same fingerprint");
        assertEquals(64, fp1.length(), "SHA-256 hex is 64 chars");
        assertFalse(fp1.contains("alice"), "fingerprint must not embed the input");
        assertNotEquals(SessionFingerprint.of("alice"), SessionFingerprint.of("bob"));
    }

    @Test
    void fingerprintOfNullIsNull() {
        assertNull(SessionFingerprint.of(null));
    }

    // --- McpToolRateLimiter ---

    @Test
    void allowsUpToLimitThenRejects() {
        McpToolRateLimiter rl = new McpToolRateLimiter(1000);
        for (int i = 0; i < 5; i++) {
            assertEquals(-1, rl.recordAndCheck("s1", 5, 60));
        }
        assertTrue(rl.recordAndCheck("s1", 5, 60) > 0);
    }

    @Test
    void keysAreIndependent() {
        McpToolRateLimiter rl = new McpToolRateLimiter(1000);
        for (int i = 0; i < 2; i++) rl.recordAndCheck("a", 2, 60);
        assertTrue(rl.recordAndCheck("a", 2, 60) > 0);
        assertEquals(-1, rl.recordAndCheck("b", 2, 60));
    }

    @Test
    void countingIsExactUnderConcurrency() throws Exception {
        McpToolRateLimiter rl = new McpToolRateLimiter(1000);
        int max = 100;
        int total = 1000;
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowed = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < total / threads; i++) {
                        if (rl.recordAndCheck("shared", max, 60) < 0) {
                            allowed.incrementAndGet();
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
        assertEquals(max, allowed.get());
    }

    // --- McpSession expiry ---

    @Test
    void sessionExpiryHonoursTtlAndIdle() {
        java.time.Instant base = java.time.Instant.ofEpochMilli(1_000_000);
        McpSession s = new McpSession("id", null, null, true, base, base, null);
        // No expiry configured
        assertFalse(s.isExpired(base.plusSeconds(10), 0, 0));
        // TTL expired
        assertTrue(s.isExpired(base.plusSeconds(10), 5_000, 0));
        // Idle expired
        assertTrue(s.isExpired(base.plusSeconds(10), 0, 5_000));
        // Touch refreshes idle
        McpSession touched = s.touch(base.plusSeconds(9));
        assertFalse(touched.isExpired(base.plusSeconds(10), 0, 5_000));
    }
}
