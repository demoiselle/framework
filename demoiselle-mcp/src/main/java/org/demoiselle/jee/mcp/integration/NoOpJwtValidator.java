/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Default no-op JWT validator used when {@code demoiselle-security} is not
 * on the classpath.
 *
 * <p>Always returns a successful validation result, effectively disabling
 * token verification. When the security module is present, it should provide
 * a higher-priority {@link JwtValidator} implementation that performs real
 * JWT validation.</p>
 */
@ApplicationScoped
public class NoOpJwtValidator implements JwtValidator {

    @Override
    public JwtValidationResult validate(String token) {
        return JwtValidationResult.ok();
    }
}
