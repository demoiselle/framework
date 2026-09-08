/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.secret;

import java.util.Optional;

/**
 * Service Provider Interface (SPI) for resolving secret references to their
 * plaintext values from a backing secret store (environment variables, system
 * properties, files, external vaults, ...).
 *
 * <p>Providers are discovered via {@link java.util.ServiceLoader} using the
 * standard {@code META-INF/services/org.demoiselle.jee.configuration.secret.SecretProvider}
 * descriptor. Only JDK-based providers ship with the framework: env, system and
 * file. No external SDKs are used.</p>
 *
 * <h2>Fail-closed contract</h2>
 * <p>Implementations MUST be fail-closed: when a secret reference names a scheme
 * handled by the provider but the secret is unavailable (missing key, unreadable
 * file, backend outage), the provider MUST signal the failure by throwing
 * {@link SecretResolutionException}. It MUST NOT fall back to a default value and
 * MUST NOT return an empty/blank value silently. The reference itself must be
 * treated as opaque; secret values must never appear in exceptions or logs.</p>
 *
 * @author SERPRO
 */
public interface SecretProvider {

    /**
     * The scheme handled by this provider, e.g. {@code env}, {@code sys} or
     * {@code file}. Matching is case-insensitive. Must be non-null and non-blank.
     *
     * @return the scheme identifier for this provider
     */
    String scheme();

    /**
     * Resolves the given key (the portion of a secret reference after the
     * scheme) into a plaintext secret value.
     *
     * <p>Return {@link Optional#empty()} only when the provider legitimately
     * has no opinion on the key and delegation to another provider is safe.
     * When the key is addressed to this provider but cannot be resolved because
     * the backing store is unavailable or the entry is missing, throw
     * {@link SecretResolutionException} (fail-closed).</p>
     *
     * @param key the provider-specific key (never {@code null})
     * @return the resolved secret value, or {@link Optional#empty()} when not applicable
     * @throws SecretResolutionException when resolution fails in a fail-closed manner
     */
    Optional<String> resolve(String key);
}
