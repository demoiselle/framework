/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.bruteforce;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 14: Brute force bloqueia IP após maxAttempts tentativas falhas
/**
 * Property-based tests for {@link BruteForceGuard} brute force lockout behavior.
 *
 * <p><b>Validates: Requirements 7.1, 7.2, 7.3</b></p>
 */
class BruteForceGuardPropertyTest {

    // ---- Helper ----

    /**
     * Creates a BruteForceGuard with a DemoiselleSecurityConfig injected via reflection,
     * configured with the given maxAttempts and a default lockout duration of 300 seconds.
     */
    private BruteForceGuard createGuard(int maxAttempts) throws Exception {
        BruteForceGuard guard = new BruteForceGuard();

        DemoiselleSecurityConfig config = new DemoiselleSecurityConfig();

        Field maxAttemptsField = DemoiselleSecurityConfig.class.getDeclaredField("bruteForceMaxAttempts");
        maxAttemptsField.setAccessible(true);
        maxAttemptsField.setInt(config, maxAttempts);

        Field lockoutField = DemoiselleSecurityConfig.class.getDeclaredField("bruteForceLockoutDuration");
        lockoutField.setAccessible(true);
        lockoutField.setInt(config, 300);

        Field configField = BruteForceGuard.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(guard, config);

        return guard;
    }

    // ---- Property Test ----

    // Feature: security-enhancements, Property 14: Brute force bloqueia IP após maxAttempts tentativas falhas
    /**
     * Property 14: For any IP address and any positive value of maxAttempts, after recording
     * exactly maxAttempts consecutive failed attempts, the BruteForceGuard must report the IP
     * as blocked and return a positive retryAfter value.
     *
     * <p><b>Validates: Requirements 7.1, 7.2, 7.3</b></p>
     */
    @Property(tries = 100)
    void blocksAfterMaxAttempts(
            @ForAll("ipAddresses") String ip,
            @ForAll @IntRange(min = 1, max = 20) int maxAttempts
    ) throws Exception {
        BruteForceGuard guard = createGuard(maxAttempts);

        // Before any attempts, IP should not be blocked
        assertEquals(-1, guard.isBlocked(ip),
                "IP should not be blocked before any failed attempts");

        // Record exactly maxAttempts - 1 failed attempts; IP should NOT be blocked yet
        for (int i = 1; i < maxAttempts; i++) {
            guard.recordFailedAttempt(ip);
            assertEquals(-1, guard.isBlocked(ip),
                    "IP should not be blocked after " + i + " of " + maxAttempts + " attempts");
        }

        // Record the final attempt (the maxAttempts-th one); IP should now be blocked
        guard.recordFailedAttempt(ip);

        int retryAfter = guard.isBlocked(ip);
        assertTrue(retryAfter > 0,
                "IP must be blocked after exactly " + maxAttempts + " failed attempts. " +
                "IP: " + ip + ", maxAttempts: " + maxAttempts + ", retryAfter: " + retryAfter);
    }

    // Feature: security-enhancements, Property 15: Brute force reseta contador após token válido
    /**
     * Property 15: For any IP address that has failed attempts registered (but is not blocked),
     * when resetAttempts(ip) is invoked, the BruteForceGuard must remove all registered attempts
     * for that IP, and isBlocked(ip) must return -1 (not blocked).
     *
     * <p><b>Validates: Requirements 7.5</b></p>
     */
    @Property(tries = 100)
    void resetsAfterValidToken(
            @ForAll("ipAddresses") String ip,
            @ForAll @IntRange(min = 2, max = 20) int maxAttempts,
            @ForAll @IntRange(min = 1, max = 19) int failedAttemptsBound
    ) throws Exception {
        // Ensure failedAttempts is strictly less than maxAttempts (IP not blocked)
        int failedAttempts = Math.min(failedAttemptsBound, maxAttempts - 1);

        BruteForceGuard guard = createGuard(maxAttempts);

        // Record some failed attempts (less than maxAttempts so IP is NOT blocked)
        for (int i = 0; i < failedAttempts; i++) {
            guard.recordFailedAttempt(ip);
        }

        // Verify IP is not blocked before reset
        assertEquals(-1, guard.isBlocked(ip),
                "IP should not be blocked with " + failedAttempts + " of " + maxAttempts + " max attempts");

        // Reset attempts (simulates a valid token received)
        guard.resetAttempts(ip);

        // After reset, IP must not be blocked
        assertEquals(-1, guard.isBlocked(ip),
                "IP must not be blocked after resetAttempts(). IP: " + ip);

        // Verify the counter was truly reset: recording maxAttempts-1 attempts should NOT block
        for (int i = 1; i < maxAttempts; i++) {
            guard.recordFailedAttempt(ip);
            assertEquals(-1, guard.isBlocked(ip),
                    "After reset, IP should not be blocked after " + i + " of " + maxAttempts + " attempts");
        }

        // Only the maxAttempts-th attempt should trigger a block again
        guard.recordFailedAttempt(ip);
        assertTrue(guard.isBlocked(ip) > 0,
                "After reset, IP should be blocked again after " + maxAttempts + " new failed attempts");
    }

    // ---- Providers ----

    /**
     * Generates random IPv4 addresses.
     */
    @Provide
    Arbitrary<String> ipAddresses() {
        Arbitrary<Integer> octet = Arbitraries.integers().between(0, 255);
        return Combinators.combine(octet, octet, octet, octet)
                .as((a, b, c, d) -> a + "." + b + "." + c + "." + d);
    }
}
