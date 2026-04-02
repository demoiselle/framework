/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for errorFormat normalization in DemoiselleRestConfig.
 *
 * // Feature: rest-enhancements, Property 4: Normalização do errorFormat na configuração
 *
 * For any string s, after setting errorFormat: if s equals "rfc9457",
 * getErrorFormat() returns "rfc9457"; otherwise returns "legacy".
 *
 * **Validates: Requirements 3.1, 3.3**
 */
class DemoiselleRestConfigPropertyTest {

    @Provide
    Arbitrary<String> anyStrings() {
        return Arbitraries.oneOf(
                Arbitraries.just("rfc9457"),
                Arbitraries.just("legacy"),
                Arbitraries.just(""),
                Arbitraries.just(null),
                Arbitraries.strings().ofMinLength(0).ofMaxLength(100)
        );
    }

    // Feature: rest-enhancements, Property 4: Normalização do errorFormat na configuração
    @Property(tries = 100)
    void errorFormatNormalizesToLegacyOrRfc9457(@ForAll("anyStrings") String input) {
        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setErrorFormat(input);

        if ("rfc9457".equals(input)) {
            assertEquals("rfc9457", config.getErrorFormat(),
                    "Setting 'rfc9457' should store 'rfc9457'");
            assertTrue(config.isRfc9457(),
                    "isRfc9457() should return true when errorFormat is 'rfc9457'");
        } else {
            assertEquals("legacy", config.getErrorFormat(),
                    "Setting any value other than 'rfc9457' should normalize to 'legacy'. Input was: " + input);
            assertFalse(config.isRfc9457(),
                    "isRfc9457() should return false when errorFormat is not 'rfc9457'");
        }
    }
}
