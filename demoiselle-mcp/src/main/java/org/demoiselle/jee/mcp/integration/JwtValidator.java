/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

/**
 * Pluggable JWT token validator for MCP SSE transport authentication.
 *
 * <p>Implementations are discovered via CDI. When {@code demoiselle-security}
 * is on the classpath, a real JWT validator is registered; otherwise the
 * default {@link NoOpJwtValidator} is used, which always returns valid.</p>
 *
 * <p>This interface avoids a hard dependency on JWT libraries in the
 * core MCP module.</p>
 */
public interface JwtValidator {

    /**
     * Validates a JWT token string.
     *
     * @param token the raw JWT token (without the "Bearer " prefix)
     * @return the validation result
     */
    JwtValidationResult validate(String token);
}
