/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.concurrent.ConcurrentHashMap;

import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RefreshTokenManager.
 * Tests valid token generation/validation, expired tokens, invalid signatures, and blacklisted tokens.
 */
class RefreshTokenManagerTest {

    private static final String IDENTITY = "user-42";
    private KeyPair keyPair;
    private KeyPair otherKeyPair;
    private RefreshTokenManager manager;
    private TokenBlacklist blacklist;
    private DemoiselleSecurityJWTConfig config;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        otherKeyPair = keyGen.generateKeyPair();

        config = new DemoiselleSecurityJWTConfig();
        setField(config, "algorithmIdentifiers", "RS256");
        setField(config, "issuer", "STORE");
        setField(config, "audience", "web");
        setField(config, "refreshTokenTtlMilliseconds", 86400000L);

        blacklist = new TokenBlacklist();

        KeyPairHolder fallbackHolder = new KeyPairHolder();
        setField(fallbackHolder, "publicKey", keyPair.getPublic());
        setField(fallbackHolder, "privateKey", keyPair.getPrivate());

        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", config);
        setField(krm, "fallbackKeyPairHolder", fallbackHolder);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", new ConcurrentHashMap<String, KeyPair>());

        manager = new RefreshTokenManager();
        setField(manager, "keyRotationManager", krm);
        setField(manager, "tokenBlacklist", blacklist);
        setField(manager, "config", config);
        setField(manager, "bundle", new StubMessages());
    }

    @Test
    void validRefreshTokenRoundTrip() {
        String refreshToken = manager.generateRefreshToken(IDENTITY);
        assertNotNull(refreshToken);

        String identity = manager.validateRefreshToken(refreshToken);
        assertEquals(IDENTITY, identity);
    }

    @Test
    void refreshTokenContainsMinimalClaims() throws Exception {
        String refreshToken = manager.generateRefreshToken(IDENTITY);

        // Parse without validation to inspect claims
        org.jose4j.jwt.consumer.JwtConsumer noValidation = new org.jose4j.jwt.consumer.JwtConsumerBuilder()
                .setSkipAllValidators()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .build();
        JwtClaims claims = noValidation.processToClaims(refreshToken);

        assertEquals(IDENTITY, claims.getSubject());
        assertNotNull(claims.getJwtId());
        assertNotNull(claims.getExpirationTime());
        assertEquals("STORE", claims.getIssuer());
        assertTrue(claims.getAudience().contains("web"));
        assertEquals("refresh", claims.getClaimValue("type"));

        // Should NOT contain roles, permissions, params
        assertNull(claims.getClaimValue("roles"));
        assertNull(claims.getClaimValue("permissions"));
        assertNull(claims.getClaimValue("params"));
        assertNull(claims.getClaimValue("identity"));
        assertNull(claims.getClaimValue("name"));
    }

    @Test
    void expiredRefreshTokenIsRejected() throws Exception {
        // Generate a token that's already expired
        setField(config, "refreshTokenTtlMilliseconds", -60000L);
        String refreshToken = manager.generateRefreshToken(IDENTITY);

        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> manager.validateRefreshToken(refreshToken));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    void invalidSignatureIsRejected() throws Exception {
        // Generate token with the normal key
        String refreshToken = manager.generateRefreshToken(IDENTITY);

        // Create a new manager that uses a different key for validation
        KeyPairHolder otherHolder = new KeyPairHolder();
        setField(otherHolder, "publicKey", otherKeyPair.getPublic());
        setField(otherHolder, "privateKey", otherKeyPair.getPrivate());

        KeyRotationManager otherKrm = new KeyRotationManager();
        setField(otherKrm, "config", config);
        setField(otherKrm, "fallbackKeyPairHolder", otherHolder);
        setField(otherKrm, "bundle", new StubMessages());
        setField(otherKrm, "keyPairs", new ConcurrentHashMap<String, KeyPair>());

        RefreshTokenManager otherManager = new RefreshTokenManager();
        setField(otherManager, "keyRotationManager", otherKrm);
        setField(otherManager, "tokenBlacklist", blacklist);
        setField(otherManager, "config", config);
        setField(otherManager, "bundle", new StubMessages());

        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> otherManager.validateRefreshToken(refreshToken));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    void blacklistedRefreshTokenIsRejected() throws Exception {
        String refreshToken = manager.generateRefreshToken(IDENTITY);

        // Extract JTI from the token
        org.jose4j.jwt.consumer.JwtConsumer noValidation = new org.jose4j.jwt.consumer.JwtConsumerBuilder()
                .setSkipAllValidators()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .build();
        JwtClaims claims = noValidation.processToClaims(refreshToken);
        String jti = claims.getJwtId();

        // Blacklist the JTI
        blacklist.blacklist(jti, System.currentTimeMillis() + 86400000L);

        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> manager.validateRefreshToken(refreshToken));
        assertEquals(401, ex.getStatusCode());
    }

    // --- Helpers ---

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
