/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.api;

import java.util.Optional;

/**
 * Service Provider Interface (SPI) for supplying JWT keys with rotation support.
 *
 * <p>Implementations may source keys from local configuration, a JWKS endpoint,
 * a KMS/HSM, etc. Only a JDK-based local provider ships with the framework; no
 * external SDKs are used.</p>
 *
 * <h2>Fail-closed contract</h2>
 * <p>Verification lookups MUST be fail-closed with respect to {@code kid}:</p>
 * <ul>
 *   <li>When a token presents a {@code kid} that the provider does not recognize
 *       (or that has fully expired past any rotation window), the provider MUST
 *       return {@link Optional#empty()} — it MUST NOT substitute a different key.
 *       The caller rejects such tokens with HTTP 401.</li>
 *   <li>When a token presents <em>no</em> {@code kid}, the provider MAY return
 *       the active key so that legacy tokens keep working; the caller decides
 *       whether the framework-level fallback applies.</li>
 * </ul>
 * <p>A provider that cannot serve any key is expected to return
 * {@link Optional#empty()} rather than a stale or arbitrary key.</p>
 *
 * @author SERPRO
 */
public interface JwtKeyProvider {

    /**
     * @return the {@code kid} of the key currently used for signing, or
     *         {@link Optional#empty()} when this provider has no active key
     */
    Optional<String> activeKeyId();

    /**
     * @return the active signing key, or {@link Optional#empty()} when this
     *         provider cannot sign (e.g. verification-only deployment)
     */
    Optional<JwtKey> activeSigningKey();

    /**
     * Resolves the verification key for the given {@code kid}.
     *
     * @param kid the key identifier from the JWT header (may be {@code null})
     * @return the matching key, or {@link Optional#empty()} when unknown/expired.
     *         When {@code kid} is {@code null}, providers may return the active key.
     */
    Optional<JwtKey> verificationKey(String kid);
}
