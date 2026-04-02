/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl.pbt;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.jqwik.api.*;

import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 7: Blacklist cleanup idempotency
 *
 * **Validates: Requirements 3.5, 9.3**
 *
 * After cleanup(), only entries with future expiration remain.
 * Running cleanup() twice doesn't change state.
 */
class BlacklistCleanupPropertyTest {

    @Provide
    Arbitrary<Map<String, Long>> blacklistEntries() {
        long now = System.currentTimeMillis();
        Arbitrary<String> jtis = Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(32);
        // Mix of past (expired) and future timestamps
        Arbitrary<Long> timestamps = Arbitraries.oneOf(
                // Past: already expired (1 second to 1 hour ago)
                Arbitraries.longs().between(now - 3_600_000L, now - 1_000L),
                // Future: still valid (10 seconds to 1 hour from now)
                Arbitraries.longs().between(now + 10_000L, now + 3_600_000L)
        );
        return Arbitraries.maps(jtis, timestamps).ofMinSize(1).ofMaxSize(20);
    }

    /**
     * P7: After cleanup(), only entries with future expiration remain.
     * Running cleanup() a second time does not change state.
     */
    @Property(tries = 100)
    void cleanupShouldBeIdempotentAndRemoveOnlyExpired(
            @ForAll("blacklistEntries") Map<String, Long> entries) {

        long now = System.currentTimeMillis();

        TokenBlacklist blacklist = new TokenBlacklist();
        entries.forEach(blacklist::blacklist);

        // Determine which JTIs should survive cleanup (future expiration)
        Set<String> expectedSurvivors = entries.entrySet().stream()
                .filter(e -> e.getValue() > now)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // First cleanup
        blacklist.cleanup();

        // Verify only future entries remain, expired ones are gone
        for (Map.Entry<String, Long> entry : entries.entrySet()) {
            if (expectedSurvivors.contains(entry.getKey())) {
                assertTrue(blacklist.isBlacklisted(entry.getKey()),
                        "JTI '" + entry.getKey() + "' with future expiration should survive cleanup");
            } else {
                assertFalse(blacklist.isBlacklisted(entry.getKey()),
                        "JTI '" + entry.getKey() + "' with past expiration should be removed by cleanup");
            }
        }

        // Capture membership state after first cleanup
        Map<String, Boolean> stateAfterFirstCleanup = entries.keySet().stream()
                .collect(Collectors.toMap(jti -> jti, blacklist::isBlacklisted));

        // Second cleanup — should be idempotent
        blacklist.cleanup();

        // Verify state is unchanged after second cleanup
        for (String jti : entries.keySet()) {
            assertEquals(stateAfterFirstCleanup.get(jti), blacklist.isBlacklisted(jti),
                    "Cleanup should be idempotent — membership of '" + jti + "' should not change on second run");
        }
    }
}
