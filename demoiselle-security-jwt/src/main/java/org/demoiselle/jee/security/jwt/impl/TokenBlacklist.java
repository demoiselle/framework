/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * In-memory blacklist for revoked JWT tokens, keyed by JTI (JWT ID).
 * Each entry stores the token's original expiration timestamp so that
 * expired entries can be cleaned up automatically.
 *
 * @author SERPRO
 */
@ApplicationScoped
public class TokenBlacklist {

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Adds a JTI to the blacklist with its expiration timestamp.
     *
     * @param jti                 the JWT ID to blacklist
     * @param expirationTimestamp epoch millis when the original token expires
     */
    public void blacklist(String jti, long expirationTimestamp) {
        if (jti != null && !jti.isEmpty()) {
            blacklistedTokens.put(jti, expirationTimestamp);
        }
    }

    /**
     * Checks whether a JTI is currently blacklisted.
     * Performs opportunistic cleanup of expired entries before checking.
     *
     * @param jti the JWT ID to check
     * @return {@code true} if the JTI is in the blacklist and has not yet expired
     */
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }
        cleanup();
        return blacklistedTokens.containsKey(jti);
    }

    /**
     * Removes all entries whose expiration timestamp has already passed.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    /**
     * Returns the current number of blacklisted entries (for testing/monitoring).
     */
    int size() {
        return blacklistedTokens.size();
    }
}
