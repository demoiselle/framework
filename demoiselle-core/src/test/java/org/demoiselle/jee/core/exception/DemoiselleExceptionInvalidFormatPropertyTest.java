/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.exception;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test: invalid errorCode format is rejected with IllegalArgumentException.
 *
 * <p><b>Validates: Requirement 3.9</b></p>
 */
class DemoiselleExceptionInvalidFormatPropertyTest {

    private static final String VALID_PATTERN = "^DEMOISELLE-[A-Z]{2,4}-\\d{3}$";

    // -----------------------------------------------------------------------
    // Property 8: Formato inválido de errorCode é rejeitado
    // -----------------------------------------------------------------------

    /**
     * Feature: core-api-enhancements, Property 8: Formato inválido de errorCode é rejeitado
     *
     * <p>For any non-null string that does NOT match ^DEMOISELLE-[A-Z]{2,4}-\d{3}$,
     * constructing a DemoiselleException with that code must throw IllegalArgumentException.</p>
     *
     * <p><b>Validates: Requirement 3.9</b></p>
     */
    @Property(tries = 100)
    // Feature: core-api-enhancements, Property 8: Formato inválido de errorCode é rejeitado
    void invalidErrorCodeFormatIsRejected(@ForAll("invalidErrorCodes") String errorCode) {
        assertThrows(IllegalArgumentException.class,
                () -> new DemoiselleException("test", errorCode),
                "Invalid errorCode '" + errorCode + "' should throw IllegalArgumentException");
    }

    @Provide
    Arbitrary<String> invalidErrorCodes() {
        return Arbitraries.oneOf(
                // Empty string
                Arbitraries.just(""),
                // Random strings without DEMOISELLE prefix
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
                // Wrong prefix
                Arbitraries.strings()
                        .withCharRange('A', 'Z')
                        .ofMinLength(2).ofMaxLength(4)
                        .map(mod -> "INVALID-" + mod + "-001"),
                // Module too short (1 char)
                Arbitraries.strings()
                        .withCharRange('A', 'Z')
                        .ofLength(1)
                        .map(mod -> "DEMOISELLE-" + mod + "-001"),
                // Module too long (5+ chars)
                Arbitraries.strings()
                        .withCharRange('A', 'Z')
                        .ofMinLength(5).ofMaxLength(8)
                        .map(mod -> "DEMOISELLE-" + mod + "-001"),
                // Wrong number of digits (1-2 digits)
                Arbitraries.integers().between(0, 99)
                        .map(n -> "DEMOISELLE-SEC-" + n),
                // Wrong number of digits (4+ digits)
                Arbitraries.integers().between(1000, 9999)
                        .map(n -> "DEMOISELLE-SEC-" + n),
                // Lowercase module
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .ofMinLength(2).ofMaxLength(4)
                        .map(mod -> "DEMOISELLE-" + mod + "-001"),
                // Missing separators
                Arbitraries.just("DEMOISELLESEC001"),
                // Extra segments
                Arbitraries.just("DEMOISELLE-SEC-001-EXTRA")
        ).filter(s -> !s.matches(VALID_PATTERN));
    }
}
