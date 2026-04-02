/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.ratelimit;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Feature: rfc-standards-compliance, Property 13: Rate limiter rejeita (N+1)-ésima requisição com Retry-After positivo
/**
 * Property-based test for {@link SlidingWindowCounter} rejection behavior.
 *
 * <p><b>Validates: Requirements 6.6, 6.7</b></p>
 */
class RateLimitRejectionPropertyTest {

    // Feature: rfc-standards-compliance, Property 13: Rate limiter rejeita (N+1)-ésima requisição com Retry-After positivo
    /**
     * Property 13: For any rate limit configuration with {@code requests >= 1} and
     * {@code window > 0}, after exactly N calls to {@code recordAndCheck}, the (N+1)th
     * call must return a positive Retry-After value ({@code >= 1}) that is also
     * {@code <= windowSeconds}.
     *
     * <p>Uses a unique key per iteration to avoid interference between test runs.</p>
     *
     * <p><b>Validates: Requirements 6.6, 6.7</b></p>
     */
    @Property(tries = 100)
    void rejectsNPlusOneRequestWithPositiveRetryAfterWithinWindow(
            @ForAll @IntRange(min = 1, max = 50) int maxRequests,
            @ForAll @IntRange(min = 1, max = 60) int windowSeconds
    ) {
        SlidingWindowCounter counter = new SlidingWindowCounter();
        String key = "test-" + UUID.randomUUID();

        // First N requests should all be allowed (return -1)
        for (int i = 0; i < maxRequests; i++) {
            int result = counter.recordAndCheck(key, maxRequests, windowSeconds);
            assertEquals(-1, result,
                    "Request " + (i + 1) + " of " + maxRequests
                            + " should be allowed (return -1) within window of " + windowSeconds + "s");
        }

        // The (N+1)th request should be rejected
        int retryAfter = counter.recordAndCheck(key, maxRequests, windowSeconds);

        // Retry-After must be positive (>= 1)
        assertTrue(retryAfter >= 1,
                "(N+1)th request should return Retry-After >= 1, but got " + retryAfter
                        + " (maxRequests=" + maxRequests + ", windowSeconds=" + windowSeconds + ")");

        // Retry-After must be <= windowSeconds
        assertTrue(retryAfter <= windowSeconds,
                "(N+1)th request Retry-After should be <= windowSeconds (" + windowSeconds
                        + "), but got " + retryAfter
                        + " (maxRequests=" + maxRequests + ", windowSeconds=" + windowSeconds + ")");
    }
}
