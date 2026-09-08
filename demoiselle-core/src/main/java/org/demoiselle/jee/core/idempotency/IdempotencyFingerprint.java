/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Computes the deterministic fingerprint that identifies a logical idempotent
 * operation.
 *
 * <p>
 * The fingerprint combines the client-supplied {@code Idempotency-Key} with the
 * authenticated principal, HTTP method, request path and a hash of the request
 * payload. Binding the key to principal/method/path prevents a key issued for
 * one operation from replaying another, and including the payload hash detects
 * accidental key reuse with a different body.
 * </p>
 *
 * @author SERPRO
 */
public final class IdempotencyFingerprint {

    private static final String NONE = "-";

    private IdempotencyFingerprint() {
    }

    /**
     * Builds the fingerprint.
     *
     * @param idempotencyKey the client {@code Idempotency-Key} header value
     * @param principal      the authenticated principal name, or {@code null} for
     *                       anonymous
     * @param method         the HTTP method (e.g. {@code POST})
     * @param path           the request path
     * @param payload        the request payload bytes (may be {@code null} or
     *                       empty)
     * @param includePayload whether the payload hash participates in the
     *                       fingerprint
     * @return a stable, URL-safe hex fingerprint
     */
    public static String compute(String idempotencyKey, String principal, String method,
            String path, byte[] payload, boolean includePayload) {
        MessageDigest digest = sha256();
        update(digest, safe(idempotencyKey));
        update(digest, safe(principal));
        update(digest, safe(method));
        update(digest, safe(path));
        if (includePayload) {
            digest.update((byte) '\0');
            if (payload != null) {
                digest.update(payload);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) '\n'); // length-independent field separator
    }

    private static String safe(String value) {
        return value == null || value.isEmpty() ? NONE : value;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JLS/JCA — this cannot happen.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
