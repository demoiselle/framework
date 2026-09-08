/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.secret;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link SecretProvider} SPI, the JDK providers and the
 * {@link SecretResolver}: fail-closed on unavailability, no default fallback,
 * TTL caching and redaction (no secret value in messages/logs).
 */
class SecretProviderTest {

    private static final String SECRET_VALUE = "s3cr3t-VALUE-should-never-leak";

    // --- JDK providers: fail-closed unavailability ---

    @Test
    void envProvider_throwsWhenVariableMissing() {
        EnvSecretProvider provider = new EnvSecretProvider();
        SecretResolutionException ex = assertThrows(SecretResolutionException.class,
                () -> provider.resolve("DEMOISELLE_SECRET_DEFINITELY_ABSENT_" + System.nanoTime()));
        assertMessageRedacted(ex.getMessage());
    }

    @Test
    void systemPropertyProvider_resolvesAndThenFailsWhenAbsent() {
        String key = "demoiselle.test.secret." + System.nanoTime();
        SystemPropertySecretProvider provider = new SystemPropertySecretProvider();

        // Absent -> fail-closed
        assertThrows(SecretResolutionException.class, () -> provider.resolve(key));

        // Present -> resolves
        System.setProperty(key, SECRET_VALUE);
        try {
            assertEquals(SECRET_VALUE, provider.resolve(key).orElseThrow());
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    void fileProvider_resolvesFileContentTrimmingTrailingNewline() throws IOException {
        Path tmp = Files.createTempFile("demoiselle-secret", ".txt");
        try {
            Files.writeString(tmp, SECRET_VALUE + "\n", StandardCharsets.UTF_8);
            FileSecretProvider provider = new FileSecretProvider();
            assertEquals(SECRET_VALUE, provider.resolve(tmp.toString()).orElseThrow());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void fileProvider_throwsWhenFileMissing() {
        FileSecretProvider provider = new FileSecretProvider();
        String path = "/nonexistent/demoiselle/secret-" + System.nanoTime();
        SecretResolutionException ex = assertThrows(SecretResolutionException.class,
                () -> provider.resolve(path));
        // Path may appear (opaque reference) but no secret value.
        assertFalse(ex.getMessage().contains(SECRET_VALUE));
    }

    @Test
    void fileProvider_throwsWhenFileEmpty() throws IOException {
        Path tmp = Files.createTempFile("demoiselle-empty-secret", ".txt");
        try {
            Files.writeString(tmp, "", StandardCharsets.UTF_8);
            FileSecretProvider provider = new FileSecretProvider();
            assertThrows(SecretResolutionException.class, () -> provider.resolve(tmp.toString()));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    // --- Resolver: unknown scheme is fail-closed (no fallback) ---

    @Test
    void resolver_unknownScheme_failsClosed() {
        SecretResolver resolver = new SecretResolver(Map.of(), 0L);
        SecretResolutionException ex = assertThrows(SecretResolutionException.class,
                () -> resolver.resolve("${secret:vault:some/key}"));
        assertTrue(ex.getMessage().contains("vault"));
        assertFalse(ex.getMessage().contains(SECRET_VALUE));
    }

    @Test
    void resolver_providerFailure_isNotCachedAndNoDefaultFallback() {
        AtomicInteger calls = new AtomicInteger();
        SecretProvider flaky = provider("test", key -> {
            calls.incrementAndGet();
            throw new SecretResolutionException("Secret unavailable for reference 'test:" + key + "'.");
        });
        SecretResolver resolver = new SecretResolver(Map.of("test", flaky), 300_000L);

        assertThrows(SecretResolutionException.class, () -> resolver.resolve("${secret:test:k}"));
        assertThrows(SecretResolutionException.class, () -> resolver.resolve("${secret:test:k}"));
        // Failures must never be cached: the provider is invoked each time.
        assertEquals(2, calls.get());
    }

    // --- Resolver: TTL cache ---

    @Test
    void resolver_cachesSuccessfulResolutionWithinTtl() {
        AtomicInteger calls = new AtomicInteger();
        SecretProvider counting = provider("test", key -> {
            calls.incrementAndGet();
            return Optional.of(SECRET_VALUE);
        });
        SecretResolver resolver = new SecretResolver(Map.of("test", counting), 300_000L);

        assertEquals(SECRET_VALUE, resolver.resolve("${secret:test:k}"));
        assertEquals(SECRET_VALUE, resolver.resolve("${secret:test:k}"));
        assertEquals(1, calls.get(), "second lookup within TTL must hit the cache");
        assertEquals(1, resolver.getHitCount());
        assertEquals(1, resolver.getMissCount());
    }

    @Test
    void resolver_cacheDisabledWhenTtlZero() {
        AtomicInteger calls = new AtomicInteger();
        SecretProvider counting = provider("test", key -> {
            calls.incrementAndGet();
            return Optional.of(SECRET_VALUE);
        });
        SecretResolver resolver = new SecretResolver(Map.of("test", counting), 0L);

        resolver.resolve("${secret:test:k}");
        resolver.resolve("${secret:test:k}");
        assertEquals(2, calls.get());
    }

    @Test
    void resolver_invalidateAll_forcesReRead() {
        AtomicInteger calls = new AtomicInteger();
        SecretProvider counting = provider("test", key -> {
            calls.incrementAndGet();
            return Optional.of(SECRET_VALUE);
        });
        SecretResolver resolver = new SecretResolver(Map.of("test", counting), 300_000L);

        resolver.resolve("${secret:test:k}");
        resolver.invalidateAll();
        resolver.resolve("${secret:test:k}");
        assertEquals(2, calls.get());
    }

    // --- Reference parsing ---

    @Test
    void isSecretReference_detectsWrappedAndBareForms() {
        assertTrue(SecretResolver.isSecretReference("${secret:env:X}"));
        assertTrue(SecretResolver.isSecretReference("secret:env:X"));
        assertFalse(SecretResolver.isSecretReference("${env:X}"));
        assertFalse(SecretResolver.isSecretReference("plain"));
        assertFalse(SecretResolver.isSecretReference(null));
    }

    @Test
    void resolver_malformedReference_failsClosed() {
        SecretResolver resolver = new SecretResolver(Map.of(), 0L);
        assertThrows(SecretResolutionException.class, () -> resolver.resolve("${secret:onlyscheme}"));
        assertThrows(SecretResolutionException.class, () -> resolver.resolve("${secret:}"));
    }

    // --- Placeholder integration ---

    @Test
    void placeholderResolver_delegatesSecretReferences() {
        String key = "demoiselle.placeholder.secret." + System.nanoTime();
        System.setProperty(key, SECRET_VALUE);
        try {
            // The default resolver discovers the SystemPropertySecretProvider via ServiceLoader.
            String resolved = org.demoiselle.jee.configuration.ConfigurationPlaceholderResolver
                    .resolve("${secret:sys:" + key + "}");
            assertEquals(SECRET_VALUE, resolved);
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    void placeholderResolver_failsClosedOnMissingSecret() {
        String ref = "${secret:sys:demoiselle.placeholder.absent." + System.nanoTime() + "}";
        assertThrows(SecretResolutionException.class,
                () -> org.demoiselle.jee.configuration.ConfigurationPlaceholderResolver.resolve(ref));
    }

    // --- helpers ---

    private static void assertMessageRedacted(String message) {
        assertNotNull(message);
        assertFalse(message.contains(SECRET_VALUE), "message must not leak the secret value");
    }

    private interface Resolver {
        Optional<String> apply(String key);
    }

    private static SecretProvider provider(String scheme, Resolver fn) {
        return new SecretProvider() {
            @Override
            public String scheme() {
                return scheme;
            }

            @Override
            public Optional<String> resolve(String key) {
                return fn.apply(key);
            }
        };
    }
}
