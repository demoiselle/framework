/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.ratelimit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 1: Rate limiting permite exatamente N requisições por janela
/**
 * Property-based test for {@link SlidingWindowCounter}.
 *
 * <p><b>Validates: Requirements 1.2, 1.3, 1.4</b></p>
 */
class SlidingWindowCounterPropertyTest {

    // Feature: security-enhancements, Property 1: Rate limiting permite exatamente N requisições por janela
    /**
     * Property 1: For any key (IP), positive values of N (requests) and W (window in seconds),
     * the SlidingWindowCounter must allow the first N invocations within a window of W seconds
     * and reject all subsequent invocations until old records expire from the window.
     *
     * <p>We make exactly N calls (all should return -1 meaning allowed), then make one more
     * call which should return a positive retry-after value.</p>
     *
     * <p><b>Validates: Requirements 1.2, 1.3, 1.4</b></p>
     */
    @Property(tries = 100)
    void rateLimitAllowsExactlyNRequestsPerWindow(
            @ForAll @IntRange(min = 1, max = 50) int maxRequests,
            @ForAll @IntRange(min = 1, max = 120) int windowSeconds
    ) {
        SlidingWindowCounter counter = new SlidingWindowCounter();
        String key = "192.168.1.1";

        // First N requests should all be allowed (return -1)
        for (int i = 0; i < maxRequests; i++) {
            int result = counter.recordAndCheck(key, maxRequests, windowSeconds);
            assertEquals(-1, result,
                    "Request " + (i + 1) + " of " + maxRequests
                            + " should be allowed (return -1) within window of " + windowSeconds + "s");
        }

        // The (N+1)th request should be rejected with a positive retry-after
        int retryAfter = counter.recordAndCheck(key, maxRequests, windowSeconds);
        assertTrue(retryAfter > 0,
                "Request " + (maxRequests + 1) + " should be rejected with positive retry-after, "
                        + "but got " + retryAfter + " (maxRequests=" + maxRequests
                        + ", windowSeconds=" + windowSeconds + ")");
    }
}
