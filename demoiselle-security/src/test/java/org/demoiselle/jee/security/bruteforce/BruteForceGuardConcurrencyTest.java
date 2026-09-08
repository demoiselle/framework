/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.bruteforce;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;
import org.demoiselle.jee.security.store.LocalSecurityStore;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BruteForceGuardConcurrencyTest {

    @Test
    void concurrentFailuresLockDeterministically() throws Exception {
        DemoiselleSecurityConfig cfg = new DemoiselleSecurityConfig(); // maxAttempts=5
        BruteForceGuard guard = new BruteForceGuard(cfg, new LocalSecurityStore());

        String key = "ip:10.0.0.99";
        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    guard.recordFailedAttempt(key);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        // After well over maxAttempts concurrent failures, the key must be locked.
        assertTrue(guard.isBlocked(key) > 0, "Key should be locked after concurrent failures");
    }

    @Test
    void resetClearsCountAndLock() {
        DemoiselleSecurityConfig cfg = new DemoiselleSecurityConfig();
        BruteForceGuard guard = new BruteForceGuard(cfg, new LocalSecurityStore());
        String key = "ip:10.0.0.100";
        for (int i = 0; i < 5; i++) {
            guard.recordFailedAttempt(key);
        }
        assertTrue(guard.isBlocked(key) > 0);
        guard.resetAttempts(key);
        assertEquals(-1, guard.isBlocked(key));
    }

    @Test
    void keysAreIndependent() {
        DemoiselleSecurityConfig cfg = new DemoiselleSecurityConfig();
        BruteForceGuard guard = new BruteForceGuard(cfg, new LocalSecurityStore());
        String a = "user:alice";
        String b = "ip:1.2.3.4";
        for (int i = 0; i < 5; i++) {
            guard.recordFailedAttempt(a);
        }
        assertTrue(guard.isBlocked(a) > 0);
        assertEquals(-1, guard.isBlocked(b));
    }
}
