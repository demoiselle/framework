/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.secret;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * JDK-only {@link SecretProvider} that reads secrets from a file on disk.
 * Handles the {@code file} scheme, where the key is the file path. This is the
 * canonical pattern for container/orchestrator mounted secrets (e.g. files
 * under {@code /run/secrets} or {@code /var/run/secrets}).
 *
 * <p>The file content is returned trimmed of a single trailing newline (the most
 * common convention for secret files) but is otherwise preserved verbatim.</p>
 *
 * <p>Fail-closed: when the file is missing, unreadable or empty, a
 * {@link SecretResolutionException} is thrown. The file contents are never
 * logged nor placed in the exception message; only the path is echoed.</p>
 *
 * @author SERPRO
 */
public class FileSecretProvider implements SecretProvider {

    @Override
    public String scheme() {
        return "file";
    }

    @Override
    public Optional<String> resolve(String key) {
        if (key == null || key.isBlank()) {
            throw new SecretResolutionException("Secret reference 'file:' has a blank path.");
        }
        Path path = Path.of(key);
        if (!Files.isReadable(path)) {
            // Fail-closed: missing/unreadable file must not degrade to a default.
            throw new SecretResolutionException(
                    "Secret unavailable for reference 'file:" + key + "' (not readable).");
        }
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            content = stripSingleTrailingNewline(content);
            if (content.isEmpty()) {
                throw new SecretResolutionException(
                        "Secret unavailable for reference 'file:" + key + "' (empty).");
            }
            return Optional.of(content);
        } catch (IOException e) {
            // Do not chain the raw content; only the opaque reference is safe.
            throw new SecretResolutionException(
                    "Secret unavailable for reference 'file:" + key + "' (read error).");
        }
    }

    private static String stripSingleTrailingNewline(String value) {
        if (value.endsWith("\r\n")) {
            return value.substring(0, value.length() - 2);
        }
        if (value.endsWith("\n") || value.endsWith("\r")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
