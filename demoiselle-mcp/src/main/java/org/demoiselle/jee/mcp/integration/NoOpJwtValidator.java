/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Fallback JWT validator used when no real JWT integration is available.
 *
 * <p>This implementation is deliberately <strong>fail-closed</strong>: enabling
 * MCP security without {@code demoiselle-security-jwt} (or another specialized
 * validator) rejects every token instead of silently accepting it. The
 * framework-provided {@link DemoiselleJwtValidator} specializes this bean when
 * CDI can resolve the standard Demoiselle JWT validator.</p>
 */
@ApplicationScoped
public class NoOpJwtValidator implements JwtValidator {

    @Override
    public JwtValidationResult validate(String token) {
        return JwtValidationResult.invalid("JWT validator unavailable");
    }
}
