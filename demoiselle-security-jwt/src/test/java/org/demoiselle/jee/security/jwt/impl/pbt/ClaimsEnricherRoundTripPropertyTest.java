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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.Instance;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.jwt.api.ClaimsEnricher;
import org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig;
import org.demoiselle.jee.security.jwt.impl.KeyPairHolder;
import org.demoiselle.jee.security.jwt.impl.KeyRotationManager;
import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jwt.JwtClaims;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 9: ClaimsEnricher round-trip
 *
 * **Validates: Requirements 5.2, 5.3, 5.5**
 *
 * For any DemoiselleUser and any set of ClaimsEnricher implementations,
 * claims added by enrich() during setUser() must be available for extract()
 * during getUser(), and each enricher must operate independently.
 */
class ClaimsEnricherRoundTripPropertyTest {

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

    /**
     * Generates arbitrary custom claim maps (1-3 entries, alphanumeric keys/values).
     */
    @Provide
    Arbitrary<Map<String, String>> customClaims() {
        Arbitrary<String> keys = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(10)
                .map(s -> "custom_" + s);
        Arbitrary<String> values = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(20);
        return Arbitraries.maps(keys, values).ofMinSize(1).ofMaxSize(3);
    }

    /**
     * P9: For any set of custom claims, enrich() adds them during setUser()
     * and extract() retrieves them during getUser(). Each enricher operates
     * independently.
     */
    @Property(tries = 100)
    void claimsEnricherRoundTripPreservesCustomClaims(
            @ForAll("customClaims") Map<String, String> claims1,
            @ForAll("customClaims") Map<String, String> claims2) throws Exception {

        // Two independent enrichers, each adding their own claims
        List<String> extractedByEnricher1 = new ArrayList<>();
        List<String> extractedByEnricher2 = new ArrayList<>();

        ClaimsEnricher enricher1 = new ClaimsEnricher() {
            @Override
            public void enrich(JwtClaims claims, DemoiselleUser user) {
                for (Map.Entry<String, String> e : claims1.entrySet()) {
                    claims.setClaim("e1_" + e.getKey(), e.getValue());
                }
            }
            @Override
            public void extract(JwtClaims claims, DemoiselleUser user) {
                for (Map.Entry<String, String> e : claims1.entrySet()) {
                    Object val = claims.getClaimValue("e1_" + e.getKey());
                    if (val != null) {
                        extractedByEnricher1.add(e.getKey() + "=" + val);
                    }
                }
            }
        };

        ClaimsEnricher enricher2 = new ClaimsEnricher() {
            @Override
            public void enrich(JwtClaims claims, DemoiselleUser user) {
                for (Map.Entry<String, String> e : claims2.entrySet()) {
                    claims.setClaim("e2_" + e.getKey(), e.getValue());
                }
            }
            @Override
            public void extract(JwtClaims claims, DemoiselleUser user) {
                for (Map.Entry<String, String> e : claims2.entrySet()) {
                    Object val = claims.getClaimValue("e2_" + e.getKey());
                    if (val != null) {
                        extractedByEnricher2.add(e.getKey() + "=" + val);
                    }
                }
            }
        };

        List<ClaimsEnricher> enrichers = List.of(enricher1, enricher2);
        Instance<ClaimsEnricher> enricherInstance = new SimpleInstance<>(enrichers);

        // Create user
        DemoiselleUserImpl user = newUser("user1", "Test User");
        user.addRole("USER");
        user.addPermission("res", "read");
        user.addParam("k", "v");

        // setUser with enrichers
        TokenManagerImpl writer = createTokenManager(enricherInstance);
        TestToken writerToken = getToken(writer);
        writer.setUser(user);
        String jwt = writerToken.getKey();
        assertNotNull(jwt);

        // getUser with same enrichers
        TokenManagerImpl reader = createTokenManager(enricherInstance);
        TestToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);
        DemoiselleUser result = reader.getUser();
        assertNotNull(result);

        // Verify enricher1 extracted all its claims
        for (Map.Entry<String, String> e : claims1.entrySet()) {
            assertTrue(extractedByEnricher1.contains(e.getKey() + "=" + e.getValue()),
                    "Enricher1 should have extracted claim " + e.getKey());
        }

        // Verify enricher2 extracted all its claims
        for (Map.Entry<String, String> e : claims2.entrySet()) {
            assertTrue(extractedByEnricher2.contains(e.getKey() + "=" + e.getValue()),
                    "Enricher2 should have extracted claim " + e.getKey());
        }

        // Standard user fields preserved
        assertEquals("user1", result.getIdentity());
        assertEquals("Test User", result.getName());
        assertTrue(result.getRoles().contains("USER"));
    }

    // --- Helpers ---

    private DemoiselleUserImpl newUser(String identity, String name) {
        DemoiselleUserImpl u = new DemoiselleUserImpl();
        u.init();
        u.setIdentity(identity);
        u.setName(name);
        return u;
    }

    private DemoiselleSecurityJWTConfig createConfig() throws Exception {
        DemoiselleSecurityJWTConfig cfg = new DemoiselleSecurityJWTConfig();
        setField(cfg, "algorithmIdentifiers", "RS256");
        setField(cfg, "issuer", "STORE");
        setField(cfg, "audience", "web");
        setField(cfg, "timetoLiveMilliseconds", 9999999999L);
        setField(cfg, "type", "master");
        return cfg;
    }

    private TokenManagerImpl createTokenManager(Instance<ClaimsEnricher> enricherInstance) throws Exception {
        TokenManagerImpl tm = new TokenManagerImpl();

        KeyPairHolder kph = new KeyPairHolder();
        setField(kph, "publicKey", KEY_PAIR.getPublic());
        setField(kph, "privateKey", KEY_PAIR.getPrivate());

        TestToken token = new TestToken();
        token.setType(TokenType.JWT);
        token.setKey("");

        DemoiselleUserImpl loggedUser = new DemoiselleUserImpl();
        loggedUser.init();

        setField(tm, "config", createConfig());
        setField(tm, "keyPairHolder", kph);

        // Create and configure KeyRotationManager with fallback to KeyPairHolder
        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", createConfig());
        setField(krm, "fallbackKeyPairHolder", kph);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", new java.util.concurrent.ConcurrentHashMap<>());
        setField(tm, "keyRotationManager", krm);

        setField(tm, "token", token);
        setField(tm, "bundle", new StubMessages());
        setField(tm, "tokenBlacklist", new TokenBlacklist());
        setField(tm, "loggedUser", loggedUser);
        setField(tm, "claimsEnrichers", enricherInstance);

        return tm;
    }

    private TestToken getToken(TokenManagerImpl tm) throws Exception {
        Field f = TokenManagerImpl.class.getDeclaredField("token");
        f.setAccessible(true);
        return (TestToken) f.get(tm);
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

    static class TestToken implements Token {
        private String key;
        private TokenType type;
        @Override public String getKey() { return key; }
        @Override public void setKey(String key) { this.key = key; }
        @Override public TokenType getType() { return type; }
        @Override public void setType(TokenType type) { this.type = type; }
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

    /**
     * Minimal Instance implementation backed by a list, for testing purposes.
     */
    static class SimpleInstance<T> implements Instance<T> {
        private final List<T> items;

        SimpleInstance(List<T> items) {
            this.items = items;
        }

        @Override
        public Iterator<T> iterator() {
            return items.iterator();
        }

        @Override
        public T get() {
            return items.isEmpty() ? null : items.get(0);
        }

        @Override
        public Instance<T> select(java.lang.annotation.Annotation... qualifiers) {
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U extends T> Instance<U> select(Class<U> subtype, java.lang.annotation.Annotation... qualifiers) {
            return (Instance<U>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U extends T> Instance<U> select(jakarta.enterprise.util.TypeLiteral<U> subtype, java.lang.annotation.Annotation... qualifiers) {
            return (Instance<U>) this;
        }

        @Override
        public boolean isUnsatisfied() {
            return items.isEmpty();
        }

        @Override
        public boolean isAmbiguous() {
            return items.size() > 1;
        }

        @Override
        public boolean isResolvable() {
            return items.size() == 1;
        }

        @Override
        public void destroy(T instance) {
            // no-op
        }

        @Override
        public Handle<T> getHandle() {
            return null;
        }

        @Override
        public Iterable<? extends Handle<T>> handles() {
            return Collections.emptyList();
        }
    }
}
