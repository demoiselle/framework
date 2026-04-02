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
 * Property-based test: valid errorCode format is accepted without exception.
 *
 * <p><b>Validates: Requirement 3.8</b></p>
 */
class DemoiselleExceptionValidFormatPropertyTest {

    // -----------------------------------------------------------------------
    // Property 7: Formato válido de errorCode é aceito
    // -----------------------------------------------------------------------

    /**
     * Feature: core-api-enhancements, Property 7: Formato válido de errorCode é aceito
     *
     * <p>For any string matching ^DEMOISELLE-[A-Z]{2,4}-\d{3}$, constructing a
     * DemoiselleException with that code must succeed (no exception thrown).</p>
     *
     * <p><b>Validates: Requirement 3.8</b></p>
     */
    @Property(tries = 100)
    // Feature: core-api-enhancements, Property 7: Formato válido de errorCode é aceito
    void validErrorCodeFormatIsAccepted(@ForAll("validErrorCodes") String errorCode) {
        assertDoesNotThrow(
                () -> new DemoiselleException("test", errorCode),
                "Valid errorCode '" + errorCode + "' should not throw any exception");
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
