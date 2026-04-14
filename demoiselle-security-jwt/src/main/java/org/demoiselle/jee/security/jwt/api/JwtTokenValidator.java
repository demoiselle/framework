/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.api;

import org.demoiselle.jee.core.api.security.DemoiselleUser;

/**
 * Validates a raw JWT token outside the standard HTTP request pipeline while
 * preserving the same security rules used by the framework request filter.
 */
public interface JwtTokenValidator {

    /**
     * Validates a raw JWT token using the default issuer and audience from the
     * framework configuration.
     *
     * @param rawToken token value, with or without the {@code Bearer } prefix
     * @return the authenticated user, or {@code null} when the token is blank
     */
    DemoiselleUser validate(String rawToken);

    /**
     * Validates a raw JWT token using an explicit issuer and audience.
     *
     * @param rawToken token value, with or without the {@code Bearer } prefix
     * @param issuer token issuer override
     * @param audience token audience override
     * @return the authenticated user, or {@code null} when the token is blank
     */
    DemoiselleUser validate(String rawToken, String issuer, String audience);
}
