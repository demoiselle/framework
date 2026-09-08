/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

/**
 * Result of a JWT token validation.
 *
 * @param valid   {@code true} if the token is valid
 * @param expired {@code true} if the token was well-formed but expired
 * @param detail  human-readable detail message (used in error responses)
 * @param subject the authenticated principal/subject when known, otherwise
 *                {@code null}. Used by the SSE transport to bind a session to a
 *                stable principal so a token issued for another principal can be
 *                rejected.
 */
public record JwtValidationResult(
    boolean valid,
    boolean expired,
    String detail,
    String subject
) {


    /**
     * Compatibility constructor for validators compiled against the original
     * three-field result contract.
     *
     * @param valid whether validation succeeded
     * @param expired whether the token expired
     * @param detail validation detail
     */
    public JwtValidationResult(boolean valid, boolean expired, String detail) {
        this(valid, expired, detail, null);
    }
    /** A successful validation result with no known subject. */
    public static JwtValidationResult ok() {
        return new JwtValidationResult(true, false, null, null);
    }

    /**
     * A successful validation result carrying the authenticated subject.
     *
     * @param subject the principal/subject identifier
     * @return a valid result bound to {@code subject}
     */
    public static JwtValidationResult ok(String subject) {
        return new JwtValidationResult(true, false, null, subject);
    }

    /**
     * Token is invalid (malformed, bad signature, etc.).
     *
     * @param detail human-readable reason
     * @return an invalid result
     */
    public static JwtValidationResult invalid(String detail) {
        return new JwtValidationResult(false, false, detail, null);
    }

    /**
     * Token is expired.
     *
     * @return an expired result
     */
    public static JwtValidationResult tokenExpired() {
        return new JwtValidationResult(false, true, "Token expired", null);
    }
}
