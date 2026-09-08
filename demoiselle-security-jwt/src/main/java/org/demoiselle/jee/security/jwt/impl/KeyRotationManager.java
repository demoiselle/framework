/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.jwt.api.JwtKey;
import org.demoiselle.jee.security.jwt.api.JwtKeyProvider;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;

/**
 * Manages multiple RSA key pairs identified by kid (Key ID) for key rotation.
 *
 * <p>Resolution order for a verification key:</p>
 * <ol>
 *   <li>Keys explicitly registered in the internal rotation map (legacy/direct API).</li>
 *   <li>{@link JwtKeyProvider} SPI implementations (e.g. {@link LocalJwtKeyProvider}),
 *       consulted for the presented {@code kid}. Providers are fail-closed: an
 *       unknown or expired {@code kid} yields no key.</li>
 *   <li>The {@link KeyPairHolder} fallback, used <em>only</em> when the token has
 *       no {@code kid} or names the configured active key id.</li>
 * </ol>
 *
 * <p>A token that presents any other explicit {@code kid} is rejected with
 * HTTP 401 — the fallback key never satisfies an unknown identifier.</p>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class KeyRotationManager {

    @Inject
    private DemoiselleSecurityJWTConfig config;

    @Inject
    private KeyPairHolder fallbackKeyPairHolder;

    @Inject
    private DemoiselleSecurityJWTMessages bundle;

    @Inject
    private Instance<JwtKeyProvider> keyProviders;

    private Map<String, KeyPair> keyPairs;

    private static final String DEFAULT_KID = "demoiselle-security-jwt";

    @PostConstruct
    void init() {
        keyPairs = new ConcurrentHashMap<>();
    }

    /**
     * Returns the private key for the active kid.
     *
     * <p>Resolution: internal rotation map, then the {@link JwtKeyProvider} SPI
     * active signing key, then the {@link KeyPairHolder} fallback.</p>
     *
     * @return the active private key for signing
     */
    public PrivateKey getActivePrivateKey() {
        String activeKid = getActiveKeyId();

        KeyPair pair = keyPairs.get(activeKid);
        if (pair != null && pair.getPrivate() != null) {
            return pair.getPrivate();
        }

        Optional<JwtKey> providerKey = providerActiveSigningKey();
        if (providerKey.isPresent() && providerKey.get().privateKey() != null) {
            return providerKey.get().privateKey();
        }

        return fallbackKeyPairHolder.getPrivateKey();
    }

    /**
     * Returns the active key ID.
     *
     * <p>Precedence: explicit configuration, then a {@link JwtKeyProvider} active
     * key id, then the default {@code demoiselle-security-jwt}.</p>
     *
     * @return the active kid
     */
    public String getActiveKeyId() {
        String activeKid = config.getActiveKeyId();
        if (activeKid != null && !activeKid.trim().isEmpty()) {
            return activeKid;
        }
        Optional<String> providerActive = providerActiveKeyId();
        if (providerActive.isPresent()) {
            return providerActive.get();
        }
        return DEFAULT_KID;
    }

    /**
     * Returns the public key corresponding to the given kid.
     *
     * <p>A key registered in the rotation map or served by a {@link JwtKeyProvider}
     * is selected directly. The fallback key is used only when {@code kid} is
     * absent or matches the configured active key ID; any other explicit ID is
     * rejected with HTTP 401.</p>
     *
     * @param kid the key identifier from the JWT header
     * @return the public key for verification
     * @throws DemoiselleSecurityException if the kid is unknown or no key exists
     */
    public PublicKey getPublicKey(String kid) {
        if (kid != null) {
            KeyPair pair = keyPairs.get(kid);
            if (pair != null && pair.getPublic() != null) {
                return pair.getPublic();
            }

            Optional<JwtKey> providerKey = providerVerificationKey(kid);
            if (providerKey.isPresent()) {
                return providerKey.get().publicKey();
            }

            // The fallback key represents only the configured active key.
            // A token that names any other kid must never be accepted with it.
            if (!kid.equals(getActiveKeyId())) {
                throw kidNotFound();
            }
        } else {
            // No kid: a provider may still serve the active key.
            Optional<JwtKey> providerKey = providerVerificationKey(null);
            if (providerKey.isPresent()) {
                return providerKey.get().publicKey();
            }
        }

        PublicKey fallback = fallbackKeyPairHolder.getPublicKey();
        if (fallback != null) {
            return fallback;
        }

        throw kidNotFound();
    }

    private Optional<JwtKey> providerVerificationKey(String kid) {
        for (JwtKeyProvider provider : providers()) {
            try {
                Optional<JwtKey> key = provider.verificationKey(kid);
                if (key.isPresent()) {
                    return key;
                }
            } catch (RuntimeException e) {
                // Fail-closed: a misbehaving provider must not open access.
            }
        }
        return Optional.empty();
    }

    private Optional<JwtKey> providerActiveSigningKey() {
        for (JwtKeyProvider provider : providers()) {
            try {
                Optional<JwtKey> key = provider.activeSigningKey();
                if (key.isPresent()) {
                    return key;
                }
            } catch (RuntimeException e) {
                // ignore and continue
            }
        }
        return Optional.empty();
    }

    private Optional<String> providerActiveKeyId() {
        for (JwtKeyProvider provider : providers()) {
            try {
                Optional<String> id = provider.activeKeyId();
                if (id.isPresent()) {
                    return id;
                }
            } catch (RuntimeException e) {
                // ignore and continue
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the discovered {@link JwtKeyProvider}s. Uses the CDI
     * {@link Instance} when available (managed environment) and otherwise falls
     * back to {@link ServiceLoader} so the SPI works outside CDI as well.
     */
    private Iterable<JwtKeyProvider> providers() {
        if (keyProviders != null && !keyProviders.isUnsatisfied()) {
            return keyProviders;
        }
        return ServiceLoader.load(JwtKeyProvider.class,
                Thread.currentThread().getContextClassLoader() != null
                        ? Thread.currentThread().getContextClassLoader()
                        : KeyRotationManager.class.getClassLoader());
    }

    private DemoiselleSecurityException kidNotFound() {
        return new DemoiselleSecurityException(
                bundle.kidNotFound(),
                Response.Status.UNAUTHORIZED.getStatusCode());
    }

    /**
     * Returns an unmodifiable view of the directly registered key pairs.
     * Useful for testing and diagnostics.
     *
     * @return unmodifiable map of kid to KeyPair
     */
    public Map<String, KeyPair> getKeyPairs() {
        return Collections.unmodifiableMap(keyPairs);
    }
}
