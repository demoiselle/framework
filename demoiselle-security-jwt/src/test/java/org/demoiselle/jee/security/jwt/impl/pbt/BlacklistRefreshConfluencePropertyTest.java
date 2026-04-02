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
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 5: Blacklist + refresh confluence
 *
 * **Validates: Requirements 2.4, 10.3**
 *
 * For any refresh token whose JTI is in the blacklist,
 * validateRefreshToken() throws DemoiselleSecurityException with status HTTP 401,
 * regardless of whether the token is within its validity period.
 */
class BlacklistRefreshConfluencePropertyTest {

    private static final KeyPair KEY_PAIR;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KEY_PAIR = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Provide
    Arbitrary<String> identities() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(50);
    }

    /**
     * P5: For any refresh token whose JTI is blacklisted,
     * validateRefreshToken() fails with 401 even if the token is still valid.
     */
    @Property(tries = 100)
    void blacklistedRefreshTokenIsRejected(
            @ForAll("identities") String identity) throws Exception {

        TokenBlacklist blacklist = new TokenBlacklist();
        RefreshTokenManager manager = createManager(blacklist);

        // Generate a valid refresh token
        String refreshToken = manager.generateRefreshToken(identity);
        assertNotNull(refreshToken);

        // Extract JTI from the token
        JwtConsumer noValidation = new JwtConsumerBuilder()
                .setSkipAllValidators()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .build();
        JwtClaims claims = noValidation.processToClaims(refreshToken);
        String jti = claims.getJwtId();
        assertNotNull(jti);

        // Blacklist the JTI with a future expiration
        blacklist.blacklist(jti, System.currentTimeMillis() + 86400000L);

        // Validation should fail with 401
        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> manager.validateRefreshToken(refreshToken));
        assertEquals(401, ex.getStatusCode(),
                "Blacklisted refresh token should be rejected with 401");
    }

    // --- Helpers ---

    private RefreshTokenManager createManager(TokenBlacklist blacklist) throws Exception {
        DemoiselleSecurityJWTConfig config = new DemoiselleSecurityJWTConfig();
        setField(config, "algorithmIdentifiers", "RS256");
        setField(config, "issuer", "STORE");
        setField(config, "audience", "web");
        setField(config, "refreshTokenTtlMilliseconds", 86400000L);

        KeyPairHolder fallbackHolder = new KeyPairHolder();
        setField(fallbackHolder, "publicKey", KEY_PAIR.getPublic());
        setField(fallbackHolder, "privateKey", KEY_PAIR.getPrivate());

        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", config);
        setField(krm, "fallbackKeyPairHolder", fallbackHolder);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", new ConcurrentHashMap<String, KeyPair>());

        RefreshTokenManager manager = new RefreshTokenManager();
        setField(manager, "keyRotationManager", krm);
        setField(manager, "tokenBlacklist", blacklist);
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
