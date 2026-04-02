package org.demoiselle.jee.security.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowCounterTest {

    private SlidingWindowCounter counter;

    @BeforeEach
    void setUp() {
        counter = new SlidingWindowCounter();
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        int result = counter.recordAndCheck("192.168.1.1", 5, 60);
        assertEquals(-1, result, "First request should be allowed");
    }

    @Test
    void shouldAllowExactlyMaxRequests() {
        String key = "192.168.1.2";
        for (int i = 0; i < 4; i++) {
            assertEquals(-1, counter.recordAndCheck(key, 5, 60));
        }
        // 5th request should still be allowed
        assertEquals(-1, counter.recordAndCheck(key, 5, 60));
    }

    @Test
    void shouldRejectWhenLimitExceeded() {
        String key = "192.168.1.3";
        for (int i = 0; i < 5; i++) {
            assertEquals(-1, counter.recordAndCheck(key, 5, 60));
        }
        // 6th request should be rejected with positive retry-after
        int retryAfter = counter.recordAndCheck(key, 5, 60);
        assertTrue(retryAfter > 0, "Should return positive retry-after seconds");
    }

    @Test
    void shouldReturnAtLeastOneSecondRetryAfter() {
        String key = "192.168.1.4";
        for (int i = 0; i < 3; i++) {
            counter.recordAndCheck(key, 3, 60);
        }
        int retryAfter = counter.recordAndCheck(key, 3, 60);
        assertTrue(retryAfter >= 1, "Retry-after should be at least 1 second");
    }

    @Test
    void shouldTrackDifferentKeysIndependently() {
        String key1 = "192.168.1.5";
        String key2 = "192.168.1.6";

        // Exhaust limit for key1
        for (int i = 0; i < 2; i++) {
            counter.recordAndCheck(key1, 2, 60);
        }
        assertTrue(counter.recordAndCheck(key1, 2, 60) > 0, "key1 should be rate limited");

        // key2 should still be allowed
        assertEquals(-1, counter.recordAndCheck(key2, 2, 60), "key2 should not be affected");
    }
}
