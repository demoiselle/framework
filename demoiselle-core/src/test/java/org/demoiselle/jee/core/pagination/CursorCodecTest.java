/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.demoiselle.jee.core.api.pagination.Cursor;
import org.demoiselle.jee.core.pagination.CursorCodec.InvalidCursorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CursorCodec}: round-trip, tamper rejection, expiry,
 * URL-safety.
 *
 * <p>Validates Requirement (5): HMAC base64url signed codec; expiration; tamper
 * rejection; structured cursor.</p>
 */
class CursorCodecTest {

    private final CursorCodec codec = new CursorCodec("super-secret-key");

    private static Cursor sample(long expiresAt) {
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put("createdAt", "2025-01-01T00:00:00Z");
        keys.put("id", "42");
        return new Cursor(keys, Cursor.Direction.AFTER, expiresAt);
    }

    @Test
    @DisplayName("encode/decode round-trips a cursor and preserves key order")
    void roundTrip() {
        Cursor original = sample(0);
        String token = codec.encode(original);
        Cursor decoded = codec.decode(token, 1_000);

        assertEquals(original, decoded);
        assertEquals(java.util.List.of("createdAt", "id"),
                new java.util.ArrayList<>(decoded.orderedKeys().keySet()),
                "key order must be preserved for a deterministic keyset");
    }

    @Test
    @DisplayName("token is URL-safe (no '+', '/', '=')")
    void tokenIsUrlSafe() {
        String token = codec.encode(sample(0));
        assertFalse(token.contains("+"));
        assertFalse(token.contains("/"));
        assertFalse(token.contains("="));
    }

    @Test
    @DisplayName("tampering with the payload is rejected")
    void tamperRejected() {
        String token = codec.encode(sample(0));
        int dot = token.indexOf('.');
        // Flip a character in the payload segment.
        char[] chars = token.toCharArray();
        chars[0] = chars[0] == 'A' ? 'B' : 'A';
        String tampered = new String(chars);

        assertThrows(InvalidCursorException.class, () -> codec.decode(tampered, 1_000));
        assertTrue(dot > 0);
    }

    @Test
    @DisplayName("signature from a different secret is rejected")
    void wrongSecretRejected() {
        String token = new CursorCodec("attacker-key").encode(sample(0));
        assertThrows(InvalidCursorException.class, () -> codec.decode(token, 1_000));
    }

    @Test
    @DisplayName("expired cursor is rejected")
    void expiryRejected() {
        String token = codec.encode(sample(500)); // expires at epoch 500
        // now = 501 → expired
        assertThrows(InvalidCursorException.class, () -> codec.decode(token, 501));
        // now = 499 → still valid
        assertEquals(sample(500), codec.decode(token, 499));
    }

    @Test
    @DisplayName("malformed tokens are rejected, not returned")
    void malformedRejected() {
        assertThrows(InvalidCursorException.class, () -> codec.decode(null, 0));
        assertThrows(InvalidCursorException.class, () -> codec.decode("", 0));
        assertThrows(InvalidCursorException.class, () -> codec.decode("no-dot", 0));
        assertThrows(InvalidCursorException.class, () -> codec.decode(".", 0));
        assertThrows(InvalidCursorException.class, () -> codec.decode("!!!.###", 0));
    }

    @Test
    @DisplayName("empty secret is rejected")
    void emptySecretRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CursorCodec(new byte[0]));
    }
}
