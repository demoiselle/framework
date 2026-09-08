/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.security.jwt.api.JwtKey;
import org.demoiselle.jee.security.jwt.api.JwtKeyProvider;

/**
 * Local {@link JwtKeyProvider} that reads a configurable set of keys from
 * {@link DemoiselleSecurityJWTConfig#getKeys()} and supports multiple
 * {@code kid}s, a cache/refresh interval and a rotation window.
 *
 * <h2>Configuration format</h2>
 * <p>{@code demoiselle.security.jwt.keys} is a {@code ;}-separated list of
 * entries, each {@code kid:publicB64Der[:privateB64Der[:validUntilEpochMillis]]}.
 * The public/private material is the base64 of the DER encoding
 * (X.509 SubjectPublicKeyInfo / PKCS#8) — a PEM body without the
 * {@code -----BEGIN...-----} envelope and without line breaks. A literal
 * {@code -} placeholder can be used to skip the private material for a
 * verification-only key while still supplying {@code validUntil}.</p>
 *
 * <h2>Fail-closed</h2>
 * <p>{@link #verificationKey(String)} returns {@link Optional#empty()} for an
 * unknown or fully expired {@code kid}; it never substitutes a different key.</p>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class LocalJwtKeyProvider implements JwtKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(LocalJwtKeyProvider.class.getName());
    private static final String ENTRY_SEPARATOR = ";";
    private static final String FIELD_SEPARATOR = ":";

    @Inject
    private DemoiselleSecurityJWTConfig config;

    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>();

    private record Snapshot(Map<String, JwtKey> keys, String rawSource, long loadedAtMillis) {}

    /** Full constructor for tests without a CDI container. */
    public LocalJwtKeyProvider() {
    }

    LocalJwtKeyProvider(DemoiselleSecurityJWTConfig config) {
        this.config = config;
    }

    @Override
    public Optional<String> activeKeyId() {
        Map<String, JwtKey> keys = current();
        String active = config.getActiveKeyId();
        if (active != null && !active.isBlank() && keys.containsKey(active)) {
            return Optional.of(active);
        }
        return Optional.empty();
    }

    @Override
    public Optional<JwtKey> activeSigningKey() {
        Map<String, JwtKey> keys = current();
        String active = config.getActiveKeyId();
        if (active == null || active.isBlank()) {
            return Optional.empty();
        }
        JwtKey key = keys.get(active);
        if (key == null || key.privateKey() == null) {
            return Optional.empty();
        }
        if (!key.isSigningEligible(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(key);
    }

    @Override
    public Optional<JwtKey> verificationKey(String kid) {
        Map<String, JwtKey> keys = current();
        if (keys.isEmpty()) {
            return Optional.empty();
        }

        String lookup = kid;
        if (lookup == null || lookup.isBlank()) {
            // No kid: providers may serve the active key.
            lookup = config.getActiveKeyId();
            if (lookup == null || lookup.isBlank()) {
                return Optional.empty();
            }
        }

        JwtKey key = keys.get(lookup);
        if (key == null) {
            // Fail-closed: never substitute another key for an unknown kid.
            return Optional.empty();
        }
        if (!key.isVerificationEligible(Instant.now(), config.getKeyRotationWindowSeconds())) {
            return Optional.empty();
        }
        return Optional.of(key);
    }

    /**
     * @return an immutable view of the currently loaded keys keyed by {@code kid}
     */
    public Map<String, JwtKey> getLoadedKeys() {
        return Map.copyOf(current());
    }

    private Map<String, JwtKey> current() {
        String raw = config != null ? config.getKeys() : null;
        long now = System.currentTimeMillis();
        long refreshMillis = (config != null ? config.getKeyRefreshSeconds() : 0L) * 1000L;

        Snapshot snap = snapshot.get();
        boolean fresh = snap != null
                && java.util.Objects.equals(snap.rawSource(), raw)
                && (refreshMillis == 0L || now - snap.loadedAtMillis() < refreshMillis);
        if (fresh) {
            return snap.keys();
        }

        Map<String, JwtKey> parsed = parse(raw);
        snapshot.set(new Snapshot(parsed, raw, now));
        return parsed;
    }

    /** Forces a reload on the next access (e.g. after a configuration change). */
    public void refresh() {
        snapshot.set(null);
    }

    private Map<String, JwtKey> parse(String raw) {
        Map<String, JwtKey> result = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return Map.copyOf(result);
        }
        for (String entry : raw.split(ENTRY_SEPARATOR)) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(FIELD_SEPARATOR, -1);
            if (parts.length < 2) {
                LOGGER.warning("Ignoring malformed JWT key entry (missing public material).");
                continue;
            }
            String kid = parts[0].trim();
            String publicB64 = parts[1].trim();
            String privateB64 = parts.length >= 3 ? parts[2].trim() : "";
            String validUntilRaw = parts.length >= 4 ? parts[3].trim() : "";
            if (kid.isEmpty() || publicB64.isEmpty()) {
                LOGGER.warning("Ignoring malformed JWT key entry (blank kid or public material).");
                continue;
            }
            try {
                PublicKey publicKey = toPublicKey(publicB64);
                PrivateKey privateKey = (privateB64.isEmpty() || "-".equals(privateB64))
                        ? null
                        : toPrivateKey(privateB64);
                Instant validUntil = null;
                if (!validUntilRaw.isEmpty() && !"-".equals(validUntilRaw)) {
                    validUntil = Instant.ofEpochMilli(Long.parseLong(validUntilRaw));
                }
                result.put(kid, new JwtKey(kid, publicKey, privateKey, null, validUntil));
            } catch (RuntimeException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                // Never log key material; only that the entry (by kid) was rejected.
                LOGGER.warning("Ignoring invalid JWT key entry for kid '" + kid + "'.");
            }
        }
        return Map.copyOf(result);
    }

    private static PublicKey toPublicKey(String base64Der)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] der = Base64.getDecoder().decode(normalizeBase64(base64Der));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static PrivateKey toPrivateKey(String base64Der)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] der = Base64.getDecoder().decode(normalizeBase64(base64Der));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static String normalizeBase64(String value) {
        return value.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
    }
}
