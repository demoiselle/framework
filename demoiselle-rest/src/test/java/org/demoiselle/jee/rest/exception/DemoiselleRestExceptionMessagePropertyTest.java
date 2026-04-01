/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 3: Round-trip de serialização JSON do DemoiselleRestExceptionMessage
 *
 * For any valid DemoiselleRestExceptionMessage, serializing to JSON and
 * deserializing back should produce an equivalent object.
 *
 * **Validates: Requirements 2.3**
 */
class DemoiselleRestExceptionMessagePropertyTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Provide
    Arbitrary<String> nonNullStrings() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(200)
                .alpha().numeric()
                .withChars(' ', '-', '_', '.', '/', ':', '?', '=', '&', '#');
    }

    @Provide
    Arbitrary<String> nullableStrings() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                nonNullStrings()
        );
    }

    @Provide
    Arbitrary<DemoiselleRestExceptionMessage> validMessages() {
        return Combinators.combine(
                nonNullStrings(),
                nullableStrings(),
                nullableStrings()
        ).as(DemoiselleRestExceptionMessage::new);
    }

    /**
     * P3: For any valid DemoiselleRestExceptionMessage, serializing to JSON
     * and deserializing back must produce an object equal to the original.
     */
    @Property(tries = 200)
    @Tag("Feature_jee-migration-v4_Property-3_json-roundtrip")
    void jsonRoundTripPreservesEquality(
            @ForAll("validMessages") DemoiselleRestExceptionMessage original) throws Exception {

        String json = MAPPER.writeValueAsString(original);
        DemoiselleRestExceptionMessage deserialized =
                MAPPER.readValue(json, DemoiselleRestExceptionMessage.class);

        assertEquals(original, deserialized,
                "Round-trip JSON serialization should preserve equality. JSON was: " + json);
    }
}
