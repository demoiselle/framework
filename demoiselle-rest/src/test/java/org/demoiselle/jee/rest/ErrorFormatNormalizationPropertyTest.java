/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for unknown errorFormat normalization in DemoiselleRestConfig.
 *
 * // Feature: rfc-standards-compliance, Property 15: Formato desconhecido de errorFormat normalizado para legacy
 *
 * For any string that is NOT "rfc9457", getErrorFormat() must return "legacy"
 * and isRfc9457() must return false.
 *
 * **Validates: Requirements 7.1, 7.4**
 */
class ErrorFormatNormalizationPropertyTest {

    /**
     * Generates arbitrary strings that are NOT "rfc9457", including null, empty,
     * and random strings of various lengths.
     */
    @Provide
    Arbitrary<String> nonRfc9457Strings() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("legacy"),
                Arbitraries.just("LEGACY"),
                Arbitraries.just("RFC9457"),
                Arbitraries.just("Rfc9457"),
                Arbitraries.just("rfc-9457"),
                Arbitraries.just("rfc 9457"),
                Arbitraries.just("unknown"),
                Arbitraries.just("json"),
                Arbitraries.just("xml"),
                Arbitraries.strings().ofMinLength(0).ofMaxLength(200)
                        .filter(s -> !"rfc9457".equals(s))
        );
    }

    // Feature: rfc-standards-compliance, Property 15: Formato desconhecido de errorFormat normalizado para legacy
    @Property(tries = 100)
    void unknownErrorFormatNormalizedToLegacy(@ForAll("nonRfc9457Strings") String input) {
        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setErrorFormat(input);

        assertEquals("legacy", config.getErrorFormat(),
                "Any errorFormat value other than 'rfc9457' must be normalized to 'legacy'. Input was: " + input);
        assertFalse(config.isRfc9457(),
                "isRfc9457() must return false for any errorFormat value other than 'rfc9457'. Input was: " + input);
    }
}
