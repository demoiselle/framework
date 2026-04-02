/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl.pbt;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.concurrent.ConcurrentHashMap;

import net.jqwik.api.*;

import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig;
import org.demoiselle.jee.security.jwt.impl.KeyPairHolder;
import org.demoiselle.jee.security.jwt.impl.KeyRotationManager;
import org.demoiselle.jee.security.jwt.impl.RefreshTokenManager;
import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 4: Invalid refresh tokens rejected
 *
 * **Validates: Requirements 2.2, 2.3**
 *
 * For any expired or corrupted refresh token, validateRefreshToken()
 * throws DemoiselleSecurityException with status HTTP 401.
 */
class RefreshTokenRejectionPropertyTest {

    private static final KeyPair KEY_PAIR;
    private static final KeyPair OTHER_KEY_PAIR;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KEY_PAIR = keyGen.generateKeyPair();
            OTHER_KEY_PAIR = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Provide
    Arbitrary<String> identities() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(50);
    }

    /**
     * P4a: For any expired refresh token, validateRefreshToken() throws 401.
     */
    @Property(tries = 100)
    void expiredRefreshTokenIsRejected(
            @ForAll("identities") String identity) throws Exception {

        // Generate an already-expired token
        RefreshTokenManager generator = createManager(-60000L, KEY_PAIR);
        String expiredToken = generator.generateRefreshToken(identity);

        // Validate with normal manager
        RefreshTokenManager validator = createManager(86400000L, KEY_PAIR);

        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> validator.validateRefreshToken(expiredToken));
        assertEquals(401, ex.getStatusCode(),
                "Expired refresh token should be rejected with 401");
    }

    /**
     * P4b: For any refresh token with corrupted signature, validateRefreshToken() throws 401.
     */
    @Property(tries = 100)
    void corruptedSignatureRefreshTokenIsRejected(
            @ForAll("identities") String identity) throws Exception {

        // Generate token with one key pair
        RefreshTokenManager generator = createManager(86400000L, KEY_PAIR);
        String token = generator.generateRefreshToken(identity);

        // Validate with a different key pair (simulates corrupted/wrong signature)
        RefreshTokenManager validator = createManager(86400000L, OTHER_KEY_PAIR);

        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> validator.validateRefreshToken(token));
        assertEquals(401, ex.getStatusCode(),
                "Refresh token with invalid signature should be rejected with 401");
    }

    // --- Helpers ---

    private RefreshTokenManager createManager(long ttl, KeyPair kp) throws Exception {
        DemoiselleSecurityJWTConfig config = new DemoiselleSecurityJWTConfig();
        setField(config, "algorithmIdentifiers", "RS256");
        setField(config, "issuer", "STORE");
        setField(config, "audience", "web");
        setField(config, "refreshTokenTtlMilliseconds", ttl);

        KeyPairHolder fallbackHolder = new KeyPairHolder();
        setField(fallbackHolder, "publicKey", kp.getPublic());
        setField(fallbackHolder, "privateKey", kp.getPrivate());

        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", config);
        setField(krm, "fallbackKeyPairHolder", fallbackHolder);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", new ConcurrentHashMap<String, KeyPair>());

        RefreshTokenManager manager = new RefreshTokenManager();
        setField(manager, "keyRotationManager", krm);
        setField(manager, "tokenBlacklist", new TokenBlacklist());
        setField(manager, "config", config);
        setField(manager, "bundle", new StubMessages());
        return manager;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName + " not found in " + target.getClass().getName());
    }

    static class StubMessages implements DemoiselleSecurityJWTMessages {
        @Override public String general() { return "general"; }
        @Override public String expired() { return "expired"; }
        @Override public String master() { return "master"; }
        @Override public String slave() { return "slave"; }
        @Override public String error() { return "error"; }
        @Override public String chooseType() { return "choose-type"; }
        @Override public String notType() { return "not-type"; }
        @Override public String putKey() { return "put-key"; }
        @Override public String notJwt() { return "not-jwt"; }
        @Override public String typeServer(String t) { return "type-server: " + t; }
        @Override public String primaryKey(String t) { return "primary-key: " + t; }
        @Override public String publicKey(String t) { return "public-key: " + t; }
        @Override public String ageToken(String t) { return "age-token: " + t; }
        @Override public String issuer(String t) { return "issuer: " + t; }
        @Override public String audience(String t) { return "audience: " + t; }
        @Override public String tokenBlacklisted() { return "token-blacklisted"; }
        @Override public String algorithmNotAllowed() { return "algorithm-not-allowed"; }
        @Override public String kidNotFound() { return "kid-not-found"; }
        @Override public String refreshTokenInvalid() { return "refresh-token-invalid"; }
    }
}
