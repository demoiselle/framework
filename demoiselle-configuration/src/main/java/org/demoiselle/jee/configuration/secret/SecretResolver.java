/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.secret;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves secret references of the form {@code ${secret:scheme:key}} (or the
 * bare {@code secret:scheme:key}) into plaintext values, delegating to the
 * registered {@link SecretProvider}s discovered through {@link ServiceLoader}.
 *
 * <h2>Cache</h2>
 * <p>Successful resolutions are cached with a configurable time-to-live so that
 * repeated lookups of the same reference do not repeatedly hit the backing
 * store. Failures are never cached (fail-closed, and a transient outage should
 * be retried). The TTL can be tuned with the system property
 * {@code demoiselle.configuration.secret.cache.ttl.seconds} (default 300; set to
 * {@code 0} to disable caching).</p>
 *
 * <h2>Security</h2>
 * <p>The resolver never logs secret values. Log statements reference only the
 * opaque {@code scheme:key} coordinate. The {@link #toString()} of resolved
 * values is the caller's responsibility, but this class emits nothing sensitive.</p>
 *
 * <h2>Fail-closed</h2>
 * <p>When a reference names a scheme with no registered provider, or the
 * provider throws/returns empty, resolution throws
 * {@link SecretResolutionException}. No default fallback is applied.</p>
 *
 * @author SERPRO
 */
public final class SecretResolver {

    private static final Logger LOGGER = Logger.getLogger(SecretResolver.class.getName());

    /** Marker prefix accepted both as {@code ${secret:...}} and bare {@code secret:...}. */
    public static final String SECRET_PREFIX = "secret:";

    private static final String TTL_PROPERTY = "demoiselle.configuration.secret.cache.ttl.seconds";
    private static final long DEFAULT_TTL_SECONDS = 300L;

    private static final SecretResolver INSTANCE = new SecretResolver();

    private final Map<String, SecretProvider> providers;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    private record CacheEntry(String value, long expiresAt) {}

    private SecretResolver() {
        this(discoverProviders(), resolveTtlMillis());
    }

    // Visible for testing.
    SecretResolver(Map<String, SecretProvider> providers, long ttlMillis) {
        this.providers = Map.copyOf(providers);
        this.ttlMillis = ttlMillis;
    }

    public static SecretResolver getInstance() {
        return INSTANCE;
    }

    private static Map<String, SecretProvider> discoverProviders() {
        Map<String, SecretProvider> discovered = new LinkedHashMap<>();
        for (SecretProvider provider : ServiceLoader.load(SecretProvider.class,
                Thread.currentThread().getContextClassLoader() != null
                        ? Thread.currentThread().getContextClassLoader()
                        : SecretResolver.class.getClassLoader())) {
            String scheme = provider.scheme();
            if (scheme == null || scheme.isBlank()) {
                continue;
            }
            discovered.putIfAbsent(scheme.toLowerCase(Locale.ROOT), provider);
        }
        return discovered;
    }

    private static long resolveTtlMillis() {
        String raw = System.getProperty(TTL_PROPERTY);
        long seconds = DEFAULT_TTL_SECONDS;
        if (raw != null && !raw.isBlank()) {
            try {
                seconds = Long.parseLong(raw.trim());
            } catch (NumberFormatException e) {
                seconds = DEFAULT_TTL_SECONDS;
            }
        }
        return Math.max(0L, seconds) * 1000L;
    }

    /**
     * Detects whether the supplied value is a secret reference.
     *
     * @param value the configured value
     * @return {@code true} when the value is a {@code secret:} reference
     */
    public static boolean isSecretReference(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = unwrap(value);
        return trimmed != null && trimmed.toLowerCase(Locale.ROOT).startsWith(SECRET_PREFIX);
    }

    /**
     * Resolves a secret reference to its plaintext value.
     *
     * @param reference a value of the form {@code ${secret:scheme:key}} or {@code secret:scheme:key}
     * @return the resolved secret
     * @throws SecretResolutionException when the reference cannot be resolved (fail-closed)
     */
    public String resolve(String reference) {
        String trimmed = unwrap(reference);
        if (trimmed == null || !trimmed.toLowerCase(Locale.ROOT).startsWith(SECRET_PREFIX)) {
            throw new SecretResolutionException("Not a secret reference.");
        }

        String coordinate = trimmed.substring(SECRET_PREFIX.length()).trim();
        int sep = coordinate.indexOf(':');
        if (sep <= 0 || sep == coordinate.length() - 1) {
            throw new SecretResolutionException(
                    "Malformed secret reference; expected 'secret:scheme:key'.");
        }
        String scheme = coordinate.substring(0, sep).trim().toLowerCase(Locale.ROOT);
        String key = coordinate.substring(sep + 1).trim();

        String cacheKey = scheme + ":" + key;

        CacheEntry cached = cache.get(cacheKey);
        long now = System.currentTimeMillis();
        if (cached != null && now < cached.expiresAt()) {
            hits.incrementAndGet();
            return cached.value();
        }
        misses.incrementAndGet();

        SecretProvider provider = providers.get(scheme);
        if (provider == null) {
            // Fail-closed: unknown scheme is never silently ignored.
            throw new SecretResolutionException(
                    "No secret provider registered for scheme '" + scheme + "'.");
        }

        Optional<String> resolved;
        try {
            resolved = provider.resolve(key);
        } catch (SecretResolutionException e) {
            // Never log the value; only the opaque coordinate.
            LOGGER.log(Level.WARNING, "Secret resolution failed for {0}", cacheKey);
            throw e;
        }

        if (resolved == null || resolved.isEmpty()) {
            LOGGER.log(Level.WARNING, "Secret resolution returned empty for {0}", cacheKey);
            throw new SecretResolutionException(
                    "Secret unavailable for reference 'secret:" + cacheKey + "'.");
        }

        String value = resolved.get();
        if (ttlMillis > 0) {
            cache.put(cacheKey, new CacheEntry(value, now + ttlMillis));
        }
        return value;
    }

    /** Removes cached entries; a subsequent resolution re-reads the backing store. */
    public void invalidateAll() {
        cache.clear();
    }

    /** @return number of cache hits observed (diagnostics). */
    public long getHitCount() {
        return hits.get();
    }

    /** @return number of cache misses observed (diagnostics). */
    public long getMissCount() {
        return misses.get();
    }

    /** @return an immutable view of the registered provider schemes. */
    public java.util.Set<String> registeredSchemes() {
        return providers.keySet();
    }

    private static String unwrap(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            return trimmed.substring(2, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}
