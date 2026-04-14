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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jakarta.enterprise.inject.Instance;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.jwt.api.ClaimsEnricher;
import org.demoiselle.jee.security.jwt.impl.JwtTokenValidatorImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClaimsEnricher integration in TokenManagerImpl.
 * Tests zero enrichers (retrocompatibility) and multiple enrichers.
 */
class ClaimsEnricherTest {

    private KeyPair keyPair;
    private DemoiselleSecurityJWTConfig config;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        config = createConfig();
    }

    @Test
    void setUserGetUserWithZeroEnrichersShouldWorkNormally() throws Exception {
        // No enrichers registered — retrocompatible behavior
        TokenManagerImpl tokenManager = createTokenManager(emptyInstance());

        DemoiselleUserImpl user = newUser("user1", "User One");
        user.addRole("ADMIN");
        user.addPermission("resource", "read");
        user.addParam("key", "value");

        TestToken token = getToken(tokenManager);
        tokenManager.setUser(user);
        assertNotNull(token.getKey());

        // Re-read
        TokenManagerImpl reader = createTokenManager(emptyInstance());
        TestToken readToken = getToken(reader);
        readToken.setKey(token.getKey());
        readToken.setType(TokenType.JWT);

        DemoiselleUser result = reader.getUser();
        assertNotNull(result);
        assertEquals("user1", result.getIdentity());
        assertEquals("User One", result.getName());
        assertTrue(result.getRoles().contains("ADMIN"));
    }

    @Test
    void setUserGetUserWithMultipleEnrichersShouldRoundTrip() throws Exception {
        // Two enrichers: one adds "department", another adds "tenant"
        List<ClaimsEnricher> enrichers = new ArrayList<>();
        enrichers.add(new ClaimsEnricher() {
            @Override
            public void enrich(JwtClaims claims, DemoiselleUser user) {
                claims.setClaim("department", "engineering");
            }
            @Override
            public void extract(JwtClaims claims, DemoiselleUser user) {
                String dept = (String) claims.getClaimValue("department");
                if (dept != null) {
                    user.addParam("department", dept);
                }
            }
        });
        enrichers.add(new ClaimsEnricher() {
            @Override
            public void enrich(JwtClaims claims, DemoiselleUser user) {
                claims.setClaim("tenant", "acme-corp");
            }
            @Override
            public void extract(JwtClaims claims, DemoiselleUser user) {
                String tenant = (String) claims.getClaimValue("tenant");
                if (tenant != null) {
                    user.addParam("tenant", tenant);
                }
            }
        });

        TokenManagerImpl tokenManager = createTokenManager(listInstance(enrichers));

        DemoiselleUserImpl user = newUser("user2", "User Two");
        user.addRole("USER");
        user.addPermission("res", "write");
        user.addParam("original", "param");

        TestToken token = getToken(tokenManager);
        tokenManager.setUser(user);
        assertNotNull(token.getKey());

        // Re-read with same enrichers
        TokenManagerImpl reader = createTokenManager(listInstance(enrichers));
        TestToken readToken = getToken(reader);
        readToken.setKey(token.getKey());
        readToken.setType(TokenType.JWT);

        DemoiselleUser result = reader.getUser();
        assertNotNull(result);
        assertEquals("user2", result.getIdentity());
        assertEquals("User Two", result.getName());
        // Enricher-added params should be extracted
        assertEquals("engineering", result.getParams("department"));
        assertEquals("acme-corp", result.getParams("tenant"));
        // Original param preserved
        assertEquals("param", result.getParams("original"));
    }

    // --- Helpers ---

    private DemoiselleSecurityJWTConfig createConfig() throws Exception {
        DemoiselleSecurityJWTConfig cfg = new DemoiselleSecurityJWTConfig();
        setField(cfg, "algorithmIdentifiers", "RS256");
        setField(cfg, "issuer", "STORE");
        setField(cfg, "audience", "web");
        setField(cfg, "timetoLiveMilliseconds", 9999999999L);
        setField(cfg, "type", "master");
        return cfg;
    }

    private DemoiselleUserImpl newUser(String identity, String name) {
        DemoiselleUserImpl u = new DemoiselleUserImpl();
        u.init();
        u.setIdentity(identity);
        u.setName(name);
        return u;
    }

    private TokenManagerImpl createTokenManager(Instance<ClaimsEnricher> enricherInstance) throws Exception {
        TokenManagerImpl tm = new TokenManagerImpl();

        KeyPairHolder kph = new KeyPairHolder();
        setField(kph, "publicKey", keyPair.getPublic());
        setField(kph, "privateKey", keyPair.getPrivate());

        TestToken token = new TestToken();
        token.setType(TokenType.JWT);
        token.setKey("");

        DemoiselleUserImpl loggedUser = new DemoiselleUserImpl();
        loggedUser.init();

        setField(tm, "config", config);
        setField(tm, "keyPairHolder", kph);

        // Create and configure KeyRotationManager with fallback to KeyPairHolder
        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", config);
        setField(krm, "fallbackKeyPairHolder", kph);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", new java.util.concurrent.ConcurrentHashMap<>());
        setField(tm, "keyRotationManager", krm);

        setField(tm, "token", token);
        setField(tm, "bundle", new StubMessages());
        setField(tm, "tokenBlacklist", new TokenBlacklist());
        setField(tm, "loggedUser", loggedUser);
        setField(tm, "claimsEnrichers", enricherInstance);

        JwtTokenValidatorImpl validator = new JwtTokenValidatorImpl();
        setField(validator, "keyRotationManager", krm);
        setField(validator, "config", config);
        setField(validator, "bundle", new StubMessages());
        setField(validator, "tokenBlacklist", new TokenBlacklist());
        setField(validator, "claimsEnrichers", enricherInstance);
        setField(tm, "jwtTokenValidator", validator);

        return tm;
    }

    private TestToken getToken(TokenManagerImpl tm) throws Exception {
        Field f = TokenManagerImpl.class.getDeclaredField("token");
        f.setAccessible(true);
        return (TestToken) f.get(tm);
    }

    @SuppressWarnings("unchecked")
    private Instance<ClaimsEnricher> emptyInstance() {
        return listInstance(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private Instance<ClaimsEnricher> listInstance(List<ClaimsEnricher> list) {
        return new SimpleInstance<>(list);
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

    /** Simple Token implementation for testing. */
    static class TestToken implements Token {
        private String key;
        private TokenType type;
        @Override public String getKey() { return key; }
        @Override public void setKey(String key) { this.key = key; }
        @Override public TokenType getType() { return type; }
        @Override public void setType(TokenType type) { this.type = type; }
    }

    /** Stub messages implementation. */
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
