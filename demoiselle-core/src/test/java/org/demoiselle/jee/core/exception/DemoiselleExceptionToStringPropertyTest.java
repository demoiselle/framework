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
 * Property-based test for DemoiselleException toString() including errorCode.
 *
 * <p><b>Validates: Requirement 3.6</b></p>
 */
class DemoiselleExceptionToStringPropertyTest {

    // -----------------------------------------------------------------------
    // Property 6: toString() inclui errorCode
    // -----------------------------------------------------------------------

    /**
     * Feature: core-api-enhancements, Property 6: toString() inclui errorCode
     *
     * <p>For any DemoiselleException constructed with a valid, non-null errorCode,
     * the string returned by toString() must contain the errorCode as a substring.</p>
     *
     * <p><b>Validates: Requirement 3.6</b></p>
     */
    @Property(tries = 100)
    // Feature: core-api-enhancements, Property 6: toString() inclui errorCode
    void toStringContainsErrorCode(@ForAll("validErrorCodes") String errorCode) {
        String message = "some error occurred";

        DemoiselleException ex = new DemoiselleException(message, errorCode);
        String result = ex.toString();

        assertTrue(result.contains(errorCode),
                "toString() must contain the errorCode. Got: " + result);
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
