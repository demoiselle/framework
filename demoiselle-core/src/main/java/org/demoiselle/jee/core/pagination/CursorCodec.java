/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.pagination;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.demoiselle.jee.core.api.pagination.Cursor;

/**
 * Encodes/decodes a {@link Cursor} to a compact, tamper-evident, URL-safe token.
 *
 * <h2>Format</h2>
 * <pre>{@code base64url(payload) + "." + base64url(HMAC-SHA256(payload))}</pre>
 * where {@code payload} is a deterministic serialization of the cursor
 * (direction, expiry and the ordered key/value pairs). The HMAC binds the token
 * to a server-side secret so a client cannot forge or mutate it: any change to
 * the payload invalidates the signature and {@link #decode(String, long)}
 * rejects it. Expired cursors are likewise rejected.
 *
 * <p>
 * The token is opaque to clients and safe to place in query strings or
 * {@code Link} headers. This class depends only on the JDK.
 * </p>
 *
 * @author SERPRO
 */
public final class CursorCodec {

    /** Thrown when a token is malformed, tampered with, or expired. */
    public static final class InvalidCursorException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public InvalidCursorException(String message) {
            super(message);
        }
    }

    private static final String HMAC_ALG = "HmacSHA256";
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();
    private static final char FIELD_SEP = '\u001f';   // unit separator
    private static final char RECORD_SEP = '\u001e';  // record separator

    private final byte[] secret;

    /**
     * @param secret the HMAC secret (must be non-empty); keep it server-side
     */
    public CursorCodec(byte[] secret) {
        Objects.requireNonNull(secret, "secret");
        if (secret.length == 0) {
            throw new IllegalArgumentException("secret must not be empty");
        }
        this.secret = secret.clone();
    }

    /**
     * Convenience constructor accepting a string secret (UTF-8).
     *
     * @param secret the HMAC secret
     */
    public CursorCodec(String secret) {
        this(Objects.requireNonNull(secret, "secret").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a cursor to a signed, URL-safe token.
     *
     * @param cursor the cursor
     * @return the token {@code payload.signature}
     */
    public String encode(Cursor cursor) {
        Objects.requireNonNull(cursor, "cursor");
        byte[] payload = serialize(cursor).getBytes(StandardCharsets.UTF_8);
        byte[] sig = hmac(payload);
        return B64.encodeToString(payload) + "." + B64.encodeToString(sig);
    }

    /**
     * Decodes and verifies a token.
     *
     * @param token           the token produced by {@link #encode(Cursor)}
     * @param nowEpochSeconds the current time in epoch seconds (for expiry check)
     * @return the verified cursor
     * @throws InvalidCursorException if the token is malformed, its signature
     *                                does not match (tampering) or it has expired
     */
    public Cursor decode(String token, long nowEpochSeconds) {
        if (token == null || token.isBlank()) {
            throw new InvalidCursorException("empty cursor token");
        }
        int dot = token.indexOf('.');
        if (dot <= 0 || dot == token.length() - 1) {
            throw new InvalidCursorException("malformed cursor token");
        }
        byte[] payload;
        byte[] providedSig;
        try {
            payload = B64D.decode(token.substring(0, dot));
            providedSig = B64D.decode(token.substring(dot + 1));
        } catch (IllegalArgumentException e) {
            throw new InvalidCursorException("invalid base64url in cursor token");
        }

        byte[] expectedSig = hmac(payload);
        // Constant-time comparison to avoid timing side channels.
        if (!MessageDigest.isEqual(expectedSig, providedSig)) {
            throw new InvalidCursorException("cursor signature mismatch (tampered token)");
        }

        Cursor cursor = deserialize(new String(payload, StandardCharsets.UTF_8));
        if (cursor.isExpired(nowEpochSeconds)) {
            throw new InvalidCursorException("cursor expired");
        }
        return cursor;
    }

    // ── serialization ──────────────────────────────────────────────

    private static String serialize(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        sb.append(cursor.direction().name());
        sb.append(FIELD_SEP).append(cursor.expiresAtEpochSeconds());
        for (Map.Entry<String, String> e : cursor.orderedKeys().entrySet()) {
            sb.append(RECORD_SEP)
              .append(e.getKey())
              .append(FIELD_SEP)
              .append(e.getValue() == null ? "" : e.getValue());
        }
        return sb.toString();
    }

    private static Cursor deserialize(String payload) {
        String[] records = payload.split(String.valueOf(RECORD_SEP), -1);
        if (records.length < 2) {
            throw new InvalidCursorException("cursor payload missing keys");
        }
        String[] header = records[0].split(String.valueOf(FIELD_SEP), -1);
        if (header.length != 2) {
            throw new InvalidCursorException("cursor header malformed");
        }
        Cursor.Direction direction;
        long expiresAt;
        try {
            direction = Cursor.Direction.valueOf(header[0]);
            expiresAt = Long.parseLong(header[1]);
        } catch (IllegalArgumentException e) {
            throw new InvalidCursorException("cursor header invalid");
        }

        Map<String, String> keys = new LinkedHashMap<>();
        for (int i = 1; i < records.length; i++) {
            String[] kv = records[i].split(String.valueOf(FIELD_SEP), -1);
            if (kv.length != 2) {
                throw new InvalidCursorException("cursor key/value malformed");
            }
            keys.put(kv[0], kv[1]);
        }
        return new Cursor(keys, direction, expiresAt);
    }

    private byte[] hmac(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secret, HMAC_ALG));
            return mac.doFinal(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute HMAC", e);
        }
    }
}
