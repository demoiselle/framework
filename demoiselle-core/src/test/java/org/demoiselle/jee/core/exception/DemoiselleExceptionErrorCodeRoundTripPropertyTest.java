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
 * Property-based test for DemoiselleException errorCode round-trip.
 *
 * <p><b>Validates: Requirements 3.2, 3.4, 3.7</b></p>
 */
class DemoiselleExceptionErrorCodeRoundTripPropertyTest {

    // -----------------------------------------------------------------------
    // Property 5: Round-trip de errorCode na DemoiselleException
    // -----------------------------------------------------------------------

    /**
     * Feature: core-api-enhancements, Property 5: Round-trip de errorCode
     *
     * <p>For any valid errorCode string (matching DEMOISELLE-[A-Z]{2,4}-\d{3}),
     * constructing a DemoiselleException with that code and calling getErrorCode()
     * must return the identical code.</p>
     *
     * <p><b>Validates: Requirements 3.2, 3.4, 3.7</b></p>
     */
    @Property(tries = 100)
    // Feature: core-api-enhancements, Property 5: Round-trip de errorCode
    void errorCodeRoundTrip(@ForAll("validErrorCodes") String errorCode) {
        String message = "test message";

        DemoiselleException ex = new DemoiselleException(message, errorCode);

        assertEquals(errorCode, ex.getErrorCode(),
                "getErrorCode() must return the exact errorCode provided to the constructor");
    }

    @Provide
    Arbitrary<String> validErrorCodes() {
        Arbitrary<String> module = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(2)
                .ofMaxLength(4);
        Arbitrary<String> number = Arbitraries.integers()
                .between(0, 999)
                .map(n -> String.format("%03d", n));
        return Combinators.combine(module, number)
                .as((mod, num) -> "DEMOISELLE-" + mod + "-" + num);
    }
}
