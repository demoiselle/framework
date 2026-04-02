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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;

import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KeyRotationManager.
 * Tests fallback to KeyPairHolder, multiple keys, and unknown kid rejection.
 */
class KeyRotationManagerTest {

    private KeyPair fallbackKeyPair;
    private KeyPair rotatedKeyPair;
    private KeyPairHolder fallbackHolder;
    private DemoiselleSecurityJWTConfig config;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        fallbackKeyPair = keyGen.generateKeyPair();
        rotatedKeyPair = keyGen.generateKeyPair();

        fallbackHolder = new KeyPairHolder();
        setField(fallbackHolder, "publicKey", fallbackKeyPair.getPublic());
        setField(fallbackHolder, "privateKey", fallbackKeyPair.getPrivate());

        config = new DemoiselleSecurityJWTConfig();
    }

    @Test
    void fallbackToKeyPairHolderWhenNoKeysConfigured() throws Exception {
        KeyRotationManager krm = createManager(config, fallbackHolder, new ConcurrentHashMap<>());

        // Should fall back to KeyPairHolder
        assertSame(fallbackKeyPair.getPrivate(), krm.getActivePrivateKey());
        assertSame(fallbackKeyPair.getPublic(), krm.getPublicKey("any-kid"));
    }

    @Test
    void defaultKidWhenNoActiveKeyIdConfigured() throws Exception {
        KeyRotationManager krm = createManager(config, fallbackHolder, new ConcurrentHashMap<>());

        assertEquals("demoiselle-security-jwt", krm.getActiveKeyId());
    }

    @Test
    void activeKeyIdFromConfig() throws Exception {
        setField(config, "activeKeyId", "key-2024-01");
        KeyRotationManager krm = createManager(config, fallbackHolder, new ConcurrentHashMap<>());

        assertEquals("key-2024-01", krm.getActiveKeyId());
    }

    @Test
    void usesRotatedKeyWhenConfigured() throws Exception {
        setField(config, "activeKeyId", "key-2024-01");

        ConcurrentHashMap<String, KeyPair> keys = new ConcurrentHashMap<>();
        keys.put("key-2024-01", rotatedKeyPair);

        KeyRotationManager krm = createManager(config, fallbackHolder, keys);

        assertSame(rotatedKeyPair.getPrivate(), krm.getActivePrivateKey());
        assertSame(rotatedKeyPair.getPublic(), krm.getPublicKey("key-2024-01"));
    }

    @Test
    void multipleKeysCanBeRetrievedByKid() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair key1 = keyGen.generateKeyPair();
        KeyPair key2 = keyGen.generateKeyPair();

        ConcurrentHashMap<String, KeyPair> keys = new ConcurrentHashMap<>();
        keys.put("kid-a", key1);
        keys.put("kid-b", key2);

        setField(config, "activeKeyId", "kid-a");
        KeyRotationManager krm = createManager(config, fallbackHolder, keys);

        assertSame(key1.getPublic(), krm.getPublicKey("kid-a"));
        assertSame(key2.getPublic(), krm.getPublicKey("kid-b"));
    }

    @Test
    void unknownKidFallsBackToKeyPairHolder() throws Exception {
        ConcurrentHashMap<String, KeyPair> keys = new ConcurrentHashMap<>();
        keys.put("known-kid", rotatedKeyPair);

        KeyRotationManager krm = createManager(config, fallbackHolder, keys);

        // Unknown kid should fall back to KeyPairHolder
        PublicKey result = krm.getPublicKey("unknown-kid");
        assertSame(fallbackKeyPair.getPublic(), result);
    }

    @Test
    void unknownKidThrowsWhenNoFallbackAvailable() throws Exception {
        // Create a KeyPairHolder with null public key
        KeyPairHolder emptyHolder = new KeyPairHolder();
        setField(emptyHolder, "publicKey", null);
        setField(emptyHolder, "privateKey", null);

        ConcurrentHashMap<String, KeyPair> keys = new ConcurrentHashMap<>();
        keys.put("known-kid", rotatedKeyPair);

        KeyRotationManager krm = createManager(config, emptyHolder, keys);

        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> krm.getPublicKey("unknown-kid"));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    void nullKidFallsBackToKeyPairHolder() throws Exception {
        KeyRotationManager krm = createManager(config, fallbackHolder, new ConcurrentHashMap<>());

        PublicKey result = krm.getPublicKey(null);
        assertSame(fallbackKeyPair.getPublic(), result);
    }

    // --- Helpers ---

    private KeyRotationManager createManager(
            DemoiselleSecurityJWTConfig cfg,
            KeyPairHolder holder,
            ConcurrentHashMap<String, KeyPair> keys) throws Exception {
        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", cfg);
        setField(krm, "fallbackKeyPairHolder", holder);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", keys);
        return krm;
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
