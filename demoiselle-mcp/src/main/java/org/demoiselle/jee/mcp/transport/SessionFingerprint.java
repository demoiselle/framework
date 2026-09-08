/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Computes a non-reversible SHA-256 fingerprint used to bind an SSE session to
 * the principal/token that established it, without ever storing the raw token
 * or principal.
 *
 * <p>The fingerprint is a lower-case hex-encoded SHA-256 digest. Binding to a
 * fingerprint (rather than the token itself) means a leaked session store never
 * exposes the bearer token or the principal identifier, while still allowing the
 * transport to detect when a POST arrives carrying a token for a different
 * principal.</p>
 *
 * @author SERPRO
 */
final class SessionFingerprint {

    private SessionFingerprint() {
    }

    /**
     * Returns the SHA-256 fingerprint of the given value.
     *
     * @param value the value to fingerprint (token or principal); may be
     *              {@code null}
     * @return a 64-char lower-case hex digest, or {@code null} when {@code value}
     *         is {@code null}
     */
    static String of(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JRE; treat as fatal.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
