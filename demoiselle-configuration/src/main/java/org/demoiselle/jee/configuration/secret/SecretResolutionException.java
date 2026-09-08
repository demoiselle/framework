/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.secret;

/**
 * Raised when a secret reference addressed to a {@link SecretProvider} cannot be
 * resolved. Enforces the fail-closed policy: rather than returning a default or
 * empty value, resolution aborts with this exception.
 *
 * <p>By contract, the message and cause of this exception must never carry the
 * plaintext secret value. Only the opaque reference (scheme + key) is safe to
 * echo, and even the key should be treated cautiously by callers.</p>
 *
 * @author SERPRO
 */
public class SecretResolutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SecretResolutionException(String message) {
        super(message);
    }

    public SecretResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
