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
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.demoiselle.jee.security.jwt.api.JwtKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LocalJwtKeyProvider}: multiple keys/kid, cache/refresh,
 * rotation window and fail-closed handling of unknown/expired kids.
 */
class LocalJwtKeyProviderTest {

    private static KeyPair kp1;
    private static KeyPair kp2;

    @BeforeAll
    static void generate() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        kp1 = gen.generateKeyPair();
        kp2 = gen.generateKeyPair();
    }

    private static String pubB64(KeyPair kp) {
        return Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    }

    private static String privB64(KeyPair kp) {
        return Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
    }

    private static DemoiselleSecurityJWTConfig config(String keys, String activeKid) throws Exception {
        DemoiselleSecurityJWTConfig cfg = new DemoiselleSecurityJWTConfig();
        set(cfg, "keys", keys);
        set(cfg, "activeKeyId", activeKid);
        return cfg;
    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void loadsMultipleKeysByKid() throws Exception {
        String keys = "kid-a:" + pubB64(kp1) + ":" + privB64(kp1)
                + ";kid-b:" + pubB64(kp2) + ":" + privB64(kp2);
        LocalJwtKeyProvider provider = new LocalJwtKeyProvider(config(keys, "kid-a"));

        assertEquals(2, provider.getLoadedKeys().size());
        assertTrue(provider.verificationKey("kid-a").isPresent());
        assertTrue(provider.verificationKey("kid-b").isPresent());
        assertEquals(kp1.getPublic(), provider.verificationKey("kid-a").get().publicKey());
        assertEquals(kp2.getPublic(), provider.verificationKey("kid-b").get().publicKey());
    }

    @Test
    void activeSigningKeyMatchesActiveKid() throws Exception {
        String keys = "kid-a:" + pubB64(kp1) + ":" + privB64(kp1)
                + ";kid-b:" + pubB64(kp2) + ":" + privB64(kp2);
        LocalJwtKeyProvider provider = new LocalJwtKeyProvider(config(keys, "kid-b"));

        assertEquals(Optional.of("kid-b"), provider.activeKeyId());
        JwtKey signing = provider.activeSigningKey().orElseThrow();
        assertEquals(kp2.getPrivate(), signing.privateKey());
    }

    @Test
    void unknownKidIsFailClosed() throws Exception {
        String keys = "kid-a:" + pubB64(kp1) + ":" + privB64(kp1);
        LocalJwtKeyProvider provider = new LocalJwtKeyProvider(config(keys, "kid-a"));

        assertTrue(provider.verificationKey("kid-unknown").isEmpty(),
                "unknown kid must not resolve to any key");
    }

    @Test
    void verificationOnlyKeyHasNoPrivate() throws Exception {
        // Omit private material -> verification-only key.
        String keys = "kid-pub:" + pubB64(kp1);
        LocalJwtKeyProvider provider = new LocalJwtKeyProvider(config(keys, "kid-pub"));

        JwtKey key = provider.verificationKey("kid-pub").orElseThrow();
        assertNull(key.privateKey());
        assertTrue(provider.activeSigningKey().isEmpty(),
                "a verification-only active key cannot sign");
    }

    @Test
    void expiredKeyRejectedForSigningButAcceptedWithinRotationWindow() throws Exception {
        long expiredMillis = Instant.now().minusSeconds(30).toEpochMilli();
        String keys = "kid-old:" + pubB64(kp1) + ":" + privB64(kp1) + ":" + expiredMillis;

        // No rotation window: expired key not eligible for verification.
        DemoiselleSecurityJWTConfig cfgNoWindow = config(keys, "kid-old");
        set(cfgNoWindow, "keyRotationWindowSeconds", 0L);
        LocalJwtKeyProvider noWindow = new LocalJwtKeyProvider(cfgNoWindow);
        assertTrue(noWindow.verificationKey("kid-old").isEmpty());
        assertTrue(noWindow.activeSigningKey().isEmpty(), "expired key must not sign");

        // Generous rotation window: still verifiable, but never signable.
        DemoiselleSecurityJWTConfig cfgWindow = config(keys, "kid-old");
        set(cfgWindow, "keyRotationWindowSeconds", 3600L);
        LocalJwtKeyProvider window = new LocalJwtKeyProvider(cfgWindow);
        assertTrue(window.verificationKey("kid-old").isPresent(),
                "expired key must still verify within the rotation window");
        assertTrue(window.activeSigningKey().isEmpty(),
                "expired key must never sign, even within the rotation window");
    }

    @Test
    void cacheReturnsSameSnapshotUntilRefreshOrConfigChange() throws Exception {
        String keys = "kid-a:" + pubB64(kp1) + ":" + privB64(kp1);
        DemoiselleSecurityJWTConfig cfg = config(keys, "kid-a");
        set(cfg, "keyRefreshSeconds", 3600L);
        LocalJwtKeyProvider provider = new LocalJwtKeyProvider(cfg);

        var first = provider.getLoadedKeys();
        var second = provider.getLoadedKeys();
        assertEquals(first.keySet(), second.keySet());

        // Change config source and force a refresh.
        String keys2 = keys + ";kid-b:" + pubB64(kp2) + ":" + privB64(kp2);
        set(cfg, "keys", keys2);
        provider.refresh();
        assertEquals(2, provider.getLoadedKeys().size());
    }

    @Test
    void malformedEntriesAreIgnored() throws Exception {
        // One valid, one malformed (no public material), one blank.
        String keys = "kid-a:" + pubB64(kp1) + ":" + privB64(kp1) + ";broken;;";
        LocalJwtKeyProvider provider = new LocalJwtKeyProvider(config(keys, "kid-a"));

        assertEquals(1, provider.getLoadedKeys().size());
        assertTrue(provider.verificationKey("kid-a").isPresent());
    }

    @Test
    void emptyConfigYieldsNoKeys() throws Exception {
        LocalJwtKeyProvider provider = new LocalJwtKeyProvider(config(null, null));
        assertTrue(provider.getLoadedKeys().isEmpty());
        assertTrue(provider.verificationKey("anything").isEmpty());
        assertTrue(provider.verificationKey(null).isEmpty());
        assertTrue(provider.activeSigningKey().isEmpty());
    }
}
