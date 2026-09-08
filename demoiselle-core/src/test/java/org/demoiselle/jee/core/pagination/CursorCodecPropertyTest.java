/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.demoiselle.jee.core.api.pagination.Cursor;
import org.demoiselle.jee.core.pagination.CursorCodec.InvalidCursorException;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

/**
 * Property-based tests for {@link CursorCodec}.
 *
 * <p>Validates Requirement (5): round-trip stability and universal tamper
 * rejection.</p>
 */
class CursorCodecPropertyTest {

    private final CursorCodec codec = new CursorCodec("property-test-secret");

    @Provide
    Arbitrary<Cursor> cursors() {
        Arbitrary<Map<String, String>> keys = Arbitraries.maps(
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                        Arbitraries.strings().ofMaxLength(12))
                .ofMinSize(1).ofMaxSize(5)
                .map(LinkedHashMap::new);
        Arbitrary<Cursor.Direction> dir = Arbitraries.of(Cursor.Direction.values());
        Arbitrary<Long> expiry = Arbitraries.longs().between(0, 4_000_000_000L);
        return net.jqwik.api.Combinators.combine(keys, dir, expiry)
                .as((k, d, e) -> new Cursor(k, d, e));
    }

    @Property(tries = 200)
    void roundTripIsStable(@ForAll("cursors") Cursor cursor) {
        String token = codec.encode(cursor);
        // Decode "before" any expiry so the expiry check never fires here.
        Cursor decoded = codec.decode(token, 0);
        assertEquals(cursor, decoded);
    }

    @Property(tries = 300)
    void tamperNeverForgesADifferentCursor(@ForAll("cursors") Cursor cursor,
                                           @ForAll @IntRange(min = 0, max = 500) int position) {
        String token = codec.encode(cursor);
        int idx = position % token.length();
        char c = token.charAt(idx);
        if (c == '.') {
            return; // separator flip is a structural error, covered elsewhere
        }
        char replacement = (c == 'A') ? 'B' : 'A';
        String tampered = token.substring(0, idx) + replacement + token.substring(idx + 1);
        if (tampered.equals(token)) {
            return;
        }
        // Security invariant: a tampered token must EITHER be rejected OR — because
        // unpadded base64url has redundant trailing-bit encodings — decode to the
        // *exact same* cursor. It must never yield a different, forged cursor.
        try {
            Cursor decoded = codec.decode(tampered, 0);
            assertEquals(cursor, decoded,
                    "an accepted variant token must decode to the identical cursor (no forgery)");
        } catch (RuntimeException expected) {
            // rejected — the primary and expected outcome
        }
    }

    @Property(tries = 100)
    void unrelatedSecretsDoNotVerify(@ForAll("cursors") Cursor cursor,
                                     @ForAll @Size(min = 1, max = 16) @net.jqwik.api.constraints.CharRange(from = 'a', to = 'z') char[] otherSecret) {
        String token = new CursorCodec(new String(otherSecret)).encode(cursor);
        if (new String(otherSecret).equals("property-test-secret")) {
            return;
        }
        assertThrows(InvalidCursorException.class, () -> codec.decode(token, 0));
    }
}
