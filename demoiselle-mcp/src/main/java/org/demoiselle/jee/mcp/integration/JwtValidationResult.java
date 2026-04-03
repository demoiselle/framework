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
 */
public record JwtValidationResult(
    boolean valid,
    boolean expired,
    String detail
) {

    /** A successful validation result. */
    public static JwtValidationResult ok() {
        return new JwtValidationResult(true, false, null);
    }

    /** Token is invalid (malformed, bad signature, etc.). */
    public static JwtValidationResult invalid(String detail) {
        return new JwtValidationResult(false, false, detail);
    }

    /** Token is expired. */
    public static JwtValidationResult tokenExpired() {
        return new JwtValidationResult(false, true, "Token expired");
    }
}
