/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.api;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.jose4j.jwt.JwtClaims;

/**
 * CDI interface for injecting custom claims into JWT tokens during creation
 * and extracting them during validation.
 *
 * <p>Implementations are discovered via CDI {@code Instance<ClaimsEnricher>}
 * and invoked by {@code TokenManagerImpl} during {@code setUser()} and
 * {@code getUser()} respectively.</p>
 *
 * <p>When no implementations are registered, the system works normally
 * without custom claims (retrocompatible).</p>
 *
 * @author SERPRO
 */
public interface ClaimsEnricher {

    /**
     * Adds custom claims during token creation.
     *
     * @param claims the JWT claims being built before signing
     * @param user   the user being encoded into the token
     */
    void enrich(JwtClaims claims, DemoiselleUser user);

    /**
     * Extracts custom claims during token validation.
     *
     * @param claims the JWT claims extracted from the validated token
     * @param user   the user being populated from the token
     */
    void extract(JwtClaims claims, DemoiselleUser user);
}
