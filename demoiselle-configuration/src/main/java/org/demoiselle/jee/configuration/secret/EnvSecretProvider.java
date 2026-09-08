/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.secret;

import java.util.Optional;

/**
 * JDK-only {@link SecretProvider} that resolves secrets from environment
 * variables. Handles the {@code env} scheme.
 *
 * <p>Fail-closed: when the environment variable is absent or blank, a
 * {@link SecretResolutionException} is thrown. The variable value is never
 * logged nor placed in the exception message.</p>
 *
 * @author SERPRO
 */
public class EnvSecretProvider implements SecretProvider {

    @Override
    public String scheme() {
        return "env";
    }

    @Override
    public Optional<String> resolve(String key) {
        if (key == null || key.isBlank()) {
            throw new SecretResolutionException("Secret reference 'env:' has a blank key.");
        }
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            // Fail-closed: do not fall back to any default value.
            throw new SecretResolutionException(
                    "Secret unavailable for reference 'env:" + key + "'.");
        }
        return Optional.of(value);
    }
}
