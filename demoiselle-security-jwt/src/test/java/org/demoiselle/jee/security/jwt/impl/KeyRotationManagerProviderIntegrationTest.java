/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;

import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.jwt.api.JwtKey;
import org.demoiselle.jee.security.jwt.api.JwtKeyProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests: {@link KeyRotationManager} consuming a {@link JwtKeyProvider}
 * SPI, while preserving fallback only for tokens without a kid and rejecting
 * unknown kids (fail-closed).
 */
class KeyRotationManagerProviderIntegrationTest {

    private static KeyPair fallbackKp;
    private static KeyPair providerKp;

    @BeforeAll
    static void generate() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        fallbackKp = gen.generateKeyPair();
        providerKp = gen.generateKeyPair();
    }

    @Test
    void usesProviderKeyForKnownKid() throws Exception {
        JwtKeyProvider provider = stubProvider(kid ->
                "kid-prov".equals(kid)
                        ? Optional.of(new JwtKey("kid-prov", providerKp.getPublic(), providerKp.getPrivate(), null, null))
                        : Optional.empty());

        KeyRotationManager krm = manager(provider, "kid-default");

        assertSame(providerKp.getPublic(), krm.getPublicKey("kid-prov"));
    }

    @Test
    void unknownKidRejectedEvenWithFallbackAndProvider() throws Exception {
        JwtKeyProvider provider = stubProvider(kid -> Optional.empty());
        KeyRotationManager krm = manager(provider, "kid-default");

        DemoiselleSecurityException ex = assertThrows(DemoiselleSecurityException.class,
                () -> krm.getPublicKey("totally-unknown"));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    void nullKidStillFallsBackToKeyPairHolder() throws Exception {
        // Provider returns nothing for null kid, framework fallback applies.
        JwtKeyProvider provider = stubProvider(kid -> Optional.empty());
        KeyRotationManager krm = manager(provider, "kid-default");

        assertSame(fallbackKp.getPublic(), krm.getPublicKey(null));
    }

    @Test
    void nullKidPrefersProviderActiveKeyWhenAvailable() throws Exception {
        JwtKeyProvider provider = stubProvider(kid ->
                kid == null
                        ? Optional.of(new JwtKey("active", providerKp.getPublic(), providerKp.getPrivate(), null, null))
                        : Optional.empty());
        KeyRotationManager krm = manager(provider, "kid-default");

        assertSame(providerKp.getPublic(), krm.getPublicKey(null));
    }

    // --- helpers ---

    private KeyRotationManager manager(JwtKeyProvider provider, String activeKid) throws Exception {
        KeyRotationManager krm = new KeyRotationManager();

        KeyPairHolder holder = new KeyPairHolder();
        setField(holder, "publicKey", fallbackKp.getPublic());
        setField(holder, "privateKey", fallbackKp.getPrivate());

        DemoiselleSecurityJWTConfig config = new DemoiselleSecurityJWTConfig();
        setField(config, "activeKeyId", activeKid);

        setField(krm, "config", config);
        setField(krm, "fallbackKeyPairHolder", holder);
        setField(krm, "bundle", new KeyRotationManagerTest.StubMessages());
        setField(krm, "keyPairs", new java.util.concurrent.ConcurrentHashMap<String, KeyPair>());
        setField(krm, "keyProviders", new SingletonInstance<>(provider));
        return krm;
    }

    private static void setField(Object target, String field, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(field);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(field);
    }

    private static JwtKeyProvider stubProvider(Function<String, Optional<JwtKey>> verify) {
        return new JwtKeyProvider() {
            @Override
            public Optional<String> activeKeyId() {
                return Optional.empty();
            }

            @Override
            public Optional<JwtKey> activeSigningKey() {
                return Optional.empty();
            }

            @Override
            public Optional<JwtKey> verificationKey(String kid) {
                return verify.apply(kid);
            }
        };
    }

    /** Minimal {@link Instance} exposing a single satisfied bean. */
    static final class SingletonInstance<T> implements Instance<T> {
        private final T value;

        SingletonInstance(T value) {
            this.value = value;
        }

        @Override public T get() { return value; }
        @Override public Iterator<T> iterator() { return List.of(value).iterator(); }
        @Override public boolean isUnsatisfied() { return value == null; }
        @Override public boolean isAmbiguous() { return false; }
        @Override public Instance<T> select(Annotation... qualifiers) { return this; }
        @Override public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public void destroy(T instance) { }
        @Override public jakarta.enterprise.inject.Instance.Handle<T> getHandle() { throw new UnsupportedOperationException(); }
        @Override public Iterable<? extends jakarta.enterprise.inject.Instance.Handle<T>> handles() { throw new UnsupportedOperationException(); }
    }
}
