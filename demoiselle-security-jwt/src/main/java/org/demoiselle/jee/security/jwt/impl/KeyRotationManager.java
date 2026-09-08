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
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;

/**
 * Manages multiple RSA key pairs identified by kid (Key ID) for key rotation.
 * When no multiple keys are configured, delegates entirely to {@link KeyPairHolder}
 * for retrocompatibility.
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

    private Map<String, KeyPair> keyPairs;

    private static final String DEFAULT_KID = "demoiselle-security-jwt";

    @PostConstruct
    void init() {
        // Initialize empty map. The configuration system doesn't support
        // nested map properties easily, so the fallback to KeyPairHolder
        // is the primary path for now.
        keyPairs = new ConcurrentHashMap<>();
    }

    /**
     * Returns the private key for the active kid.
     * If no multiple keys are configured, falls back to KeyPairHolder.getPrivateKey().
     *
     * @return the active private key for signing
     */
    public PrivateKey getActivePrivateKey() {
        String activeKid = getActiveKeyId();
        KeyPair pair = keyPairs.get(activeKid);
        if (pair != null && pair.getPrivate() != null) {
            return pair.getPrivate();
        }
        return fallbackKeyPairHolder.getPrivateKey();
    }

    /**
     * Returns the active key ID from configuration.
     * If not configured, returns the default "demoiselle-security-jwt".
     *
     * @return the active kid
     */
    public String getActiveKeyId() {
        String activeKid = config.getActiveKeyId();
        if (activeKid != null && !activeKid.trim().isEmpty()) {
            return activeKid;
        }
        return DEFAULT_KID;
    }

    /**
     * Returns the public key corresponding to the given kid.
     * A key registered in the rotation map is selected directly. The fallback
     * key is used only when {@code kid} is absent or matches the configured
     * active key ID; any other explicit ID is rejected with HTTP 401.
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

            // The fallback key represents only the configured active key.
            // A token that names any other kid must never be accepted with it.
            if (!kid.equals(getActiveKeyId())) {
                throw kidNotFound();
            }
        }

        PublicKey fallback = fallbackKeyPairHolder.getPublicKey();
        if (fallback != null) {
            return fallback;
        }

        throw kidNotFound();
    }

    private DemoiselleSecurityException kidNotFound() {
        return new DemoiselleSecurityException(
                bundle.kidNotFound(),
                Response.Status.UNAUTHORIZED.getStatusCode());
    }

    /**
     * Returns an unmodifiable view of the configured key pairs.
     * Useful for testing and diagnostics.
     *
     * @return unmodifiable map of kid to KeyPair
     */
    public Map<String, KeyPair> getKeyPairs() {
        return Collections.unmodifiableMap(keyPairs);
    }
}
