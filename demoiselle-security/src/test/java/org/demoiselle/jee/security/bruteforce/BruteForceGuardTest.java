/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.bruteforce;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class BruteForceGuardTest {

    private BruteForceGuard guard;

    @BeforeEach
    void setUp() throws Exception {
        guard = new BruteForceGuard();
        DemoiselleSecurityConfig config = new DemoiselleSecurityConfig();
        // Inject config via reflection (no CDI in unit tests)
        Field configField = BruteForceGuard.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(guard, config);
    }

    @Test
    void shouldReturnNotBlockedForUnknownIp() {
        assertEquals(-1, guard.isBlocked("10.0.0.1"));
    }

    @Test
    void shouldNotBlockBeforeMaxAttempts() {
        String ip = "10.0.0.2";
        // Default maxAttempts is 5, record 4 failures
        for (int i = 0; i < 4; i++) {
            guard.recordFailedAttempt(ip);
        }
        assertEquals(-1, guard.isBlocked(ip));
    }

    @Test
    void shouldBlockAfterMaxAttempts() {
        String ip = "10.0.0.3";
        for (int i = 0; i < 5; i++) {
            guard.recordFailedAttempt(ip);
        }
        int retryAfter = guard.isBlocked(ip);
        assertTrue(retryAfter > 0, "IP should be blocked after maxAttempts");
    }

    @Test
    void shouldResetAttemptsForIp() {
        String ip = "10.0.0.4";
        for (int i = 0; i < 3; i++) {
            guard.recordFailedAttempt(ip);
        }
        guard.resetAttempts(ip);
        assertEquals(-1, guard.isBlocked(ip));
    }

    @Test
    void shouldTrackDifferentIpsIndependently() {
        String ip1 = "10.0.0.5";
        String ip2 = "10.0.0.6";

        // Block ip1
        for (int i = 0; i < 5; i++) {
            guard.recordFailedAttempt(ip1);
        }
        assertTrue(guard.isBlocked(ip1) > 0, "ip1 should be blocked");
        assertEquals(-1, guard.isBlocked(ip2), "ip2 should not be affected");
    }

    @Test
    void shouldResetOnlyTargetIp() {
        String ip1 = "10.0.0.7";
        String ip2 = "10.0.0.8";

        for (int i = 0; i < 5; i++) {
            guard.recordFailedAttempt(ip1);
            guard.recordFailedAttempt(ip2);
        }

        guard.resetAttempts(ip1);
        assertEquals(-1, guard.isBlocked(ip1), "ip1 should be unblocked after reset");
        assertTrue(guard.isBlocked(ip2) > 0, "ip2 should still be blocked");
    }
}
