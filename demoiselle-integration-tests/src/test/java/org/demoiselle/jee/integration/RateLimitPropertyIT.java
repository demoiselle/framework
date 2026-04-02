/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.integration;

// Feature: cross-cutting-improvements, Property 9: Invariante do rate limiter

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.demoiselle.jee.security.ratelimit.SlidingWindowCounter;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based integration test for the rate limiter invariant.
 *
 * <p>Property 9: For any rate limit configuration (requests=N, window=W)
 * with N &ge; 1 and W &gt; 0, after exactly N requests within window W,
 * the (N+1)-th request must be rejected by {@link SlidingWindowCounter}.</p>
 *
 * <p><b>Validates: Requirement 11.3</b></p>
 */
@EnabledIf("isRateLimiterAvailable")
class RateLimitPropertyIT {

    static boolean isRateLimiterAvailable() {
        try {
            Class.forName("org.demoiselle.jee.security.ratelimit.SlidingWindowCounter");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // Feature: cross-cutting-improvements, Property 9: Invariante do rate limiter
    /**
     * Property 9: For any configuration (N &ge; 1, W &gt; 0), after exactly N
     * requests within window W, the (N+1)-th request is rejected.
     *
     * <p>We create a fresh {@link SlidingWindowCounter}, make N calls that must
     * all be allowed (return -1), then verify the (N+1)-th call returns a
     * positive retry-after value indicating rejection.</p>
     *
     * <p><b>Validates: Requirements 11.3</b></p>
     */
    @Property(tries = 100)
    void rateLimiterRejectsAfterNRequests(
            @ForAll @IntRange(min = 1, max = 100) int maxRequests,
            @ForAll @IntRange(min = 1, max = 60) int windowSeconds
    ) {
        SlidingWindowCounter counter = new SlidingWindowCounter();
        String key = "test-client-" + maxRequests + "-" + windowSeconds;

        // First N requests must all be allowed (return -1)
        for (int i = 0; i < maxRequests; i++) {
            int result = counter.recordAndCheck(key, maxRequests, windowSeconds);
            assertEquals(-1, result,
                    "Request " + (i + 1) + " of " + maxRequests
                            + " should be allowed within window of " + windowSeconds + "s");
        }

        // The (N+1)-th request must be rejected with a positive retry-after
        int retryAfter = counter.recordAndCheck(key, maxRequests, windowSeconds);
        assertTrue(retryAfter > 0,
                "Request " + (maxRequests + 1) + " should be rejected with positive retry-after, "
                        + "but got " + retryAfter + " (maxRequests=" + maxRequests
                        + ", windowSeconds=" + windowSeconds + ")");
    }
}
