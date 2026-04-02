/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl.pbt;

import net.jqwik.api.*;

import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 6: Blacklist membership invariant
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 9.1, 9.2**
 *
 * For all JTIs added with future expiration, isBlacklisted returns true.
 * For all JTIs never added, isBlacklisted returns false.
 */
class BlacklistMembershipPropertyTest {

    @Provide
    Arbitrary<String> validJtis() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(64);
    }

    @Provide
    Arbitrary<Long> futureExpirations() {
        // Timestamps at least 10 seconds in the future, up to ~1 hour
        return Arbitraries.longs().between(
                System.currentTimeMillis() + 10_000L,
                System.currentTimeMillis() + 3_600_000L
        );
    }

    /**
     * P6a: For all JTIs added with a future expiration, isBlacklisted must return true.
     */
    @Property(tries = 100)
    void blacklistedJtiWithFutureExpirationShouldBeDetected(
            @ForAll("validJtis") String jti,
            @ForAll("futureExpirations") long expiration) {

        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.blacklist(jti, expiration);

        assertTrue(blacklist.isBlacklisted(jti),
                "JTI '" + jti + "' with future expiration should be blacklisted");
    }

    /**
     * P6b: For all JTIs never added, isBlacklisted must return false.
     */
    @Property(tries = 100)
    void nonBlacklistedJtiShouldNotBeDetected(
            @ForAll("validJtis") String jti) {

        TokenBlacklist blacklist = new TokenBlacklist();

        assertFalse(blacklist.isBlacklisted(jti),
                "JTI '" + jti + "' that was never added should not be blacklisted");
    }
}
