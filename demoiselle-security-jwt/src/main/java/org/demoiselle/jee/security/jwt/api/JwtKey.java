/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.api;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;

/**
 * Immutable descriptor of a JWT signing/verification key identified by its
 * {@code kid}. Carries an optional validity window used for key rotation.
 *
 * <p>{@code validUntil} marks the instant after which the key must no longer be
 * used for <em>signing</em>. A separate rotation window (configured on the
 * provider) may still allow the key to be used for <em>verification</em> for a
 * limited time so that tokens signed just before rotation remain valid until
 * they naturally expire.</p>
 *
 * @param kid         the key identifier (never {@code null} or blank)
 * @param publicKey   the verification key (never {@code null})
 * @param privateKey  the signing key, or {@code null} for verification-only keys
 * @param validFrom   the instant the key becomes usable, or {@code null} for "always"
 * @param validUntil  the instant the key stops being usable for signing, or {@code null} for "never expires"
 *
 * @author SERPRO
 */
public record JwtKey(
        String kid,
        PublicKey publicKey,
        PrivateKey privateKey,
        Instant validFrom,
        Instant validUntil) {

    public JwtKey {
        if (kid == null || kid.isBlank()) {
            throw new IllegalArgumentException("JwtKey requires a non-blank kid");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("JwtKey requires a public key");
        }
    }

    /**
     * @param at the reference instant
     * @return {@code true} when the key may be used to <em>sign</em> at {@code at}
     */
    public boolean isSigningEligible(Instant at) {
        if (validFrom != null && at.isBefore(validFrom)) {
            return false;
        }
        return validUntil == null || !at.isAfter(validUntil);
    }

    /**
     * @param at             the reference instant
     * @param windowSeconds  the additional grace window after {@code validUntil} for verification
     * @return {@code true} when the key may be used to <em>verify</em> at {@code at}
     */
    public boolean isVerificationEligible(Instant at, long windowSeconds) {
        if (validFrom != null && at.isBefore(validFrom)) {
            return false;
        }
        if (validUntil == null) {
            return true;
        }
        return !at.isAfter(validUntil.plusSeconds(Math.max(0L, windowSeconds)));
    }
}
