/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TokenBlacklist}.
 * Covers basic operations, concurrency, and cleanup with fixed timestamps.
 */
class TokenBlacklistTest {

    private TokenBlacklist blacklist;

    @BeforeEach
    void setUp() {
        blacklist = new TokenBlacklist();
    }

    @Test
    void blacklistAndCheckShouldReturnTrue() {
        long futureExp = System.currentTimeMillis() + 60_000;
        blacklist.blacklist("jti-1", futureExp);
        assertTrue(blacklist.isBlacklisted("jti-1"));
    }

    @Test
    void nonBlacklistedJtiShouldReturnFalse() {
        assertFalse(blacklist.isBlacklisted("unknown-jti"));
    }

    @Test
    void nullJtiShouldNotBeBlacklisted() {
        assertFalse(blacklist.isBlacklisted(null));
    }

    @Test
    void emptyJtiShouldNotBeBlacklisted() {
        assertFalse(blacklist.isBlacklisted(""));
    }

    @Test
    void blacklistNullJtiShouldBeIgnored() {
        blacklist.blacklist(null, System.currentTimeMillis() + 60_000);
        assertEquals(0, blacklist.size());
    }

    @Test
    void blacklistEmptyJtiShouldBeIgnored() {
        blacklist.blacklist("", System.currentTimeMillis() + 60_000);
        assertEquals(0, blacklist.size());
    }

    @Test
    void cleanupShouldRemoveExpiredEntries() {
        // Already expired
        blacklist.blacklist("expired-jti", System.currentTimeMillis() - 1000);
        // Still valid
        blacklist.blacklist("valid-jti", System.currentTimeMillis() + 60_000);

        blacklist.cleanup();

        assertFalse(blacklist.isBlacklisted("expired-jti"));
        assertTrue(blacklist.isBlacklisted("valid-jti"));
        assertEquals(1, blacklist.size());
    }

    @Test
    void isBlacklistedShouldPerformOpportunisticCleanup() {
        blacklist.blacklist("expired-jti", System.currentTimeMillis() - 1000);
        // isBlacklisted triggers cleanup, so expired entry is removed
        assertFalse(blacklist.isBlacklisted("expired-jti"));
        assertEquals(0, blacklist.size());
    }

    @Test
    void concurrentBlacklistAndCheckShouldBeThreadSafe() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        long futureExp = System.currentTimeMillis() + 60_000;
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final String jti = "jti-" + i;
            executor.submit(() -> {
                try {
                    blacklist.blacklist(jti, futureExp);
                    assertTrue(blacklist.isBlacklisted(jti));
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(errors.isEmpty(), "Concurrent errors: " + errors);
        assertEquals(threadCount, blacklist.size());
    }
}
