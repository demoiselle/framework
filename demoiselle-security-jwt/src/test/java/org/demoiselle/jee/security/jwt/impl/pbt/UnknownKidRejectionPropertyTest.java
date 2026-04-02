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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.inject.Instance;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.jwt.api.ClaimsEnricher;
import org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig;
import org.demoiselle.jee.security.jwt.impl.KeyPairHolder;
import org.demoiselle.jee.security.jwt.impl.KeyRotationManager;
import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 11: Unknown kid rejection
 *
 * **Validates: Requisito 6.5**
 *
 * For any token with a kid not in KeyRotationManager and no fallback available,
 * validation fails with DemoiselleSecurityException and status 401.
 */
class UnknownKidRejectionPropertyTest {

    private static final KeyPair SIGNING_KEY_PAIR;
    private static final KeyPair KNOWN_KEY_PAIR;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            SIGNING_KEY_PAIR = keyGen.generateKeyPair();
            KNOWN_KEY_PAIR = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates arbitrary unknown kid identifiers that won't match the known kid.
     */
    @Provide
    Arbitrary<String> unknownKids() {
        return Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(3).ofMaxLength(15)
                .map(s -> "unknown-" + s);
    }

    /**
     * P11: For any token with a kid not in KeyRotationManager and no fallback,
     * validation must fail with DemoiselleSecurityException and status 401.
     */
    @Property(tries = 100)
    void tokenWithUnknownKidShouldBeRejected(
            @ForAll("unknownKids") String unknownKid) throws Exception {

        DemoiselleSecurityJWTConfig config = createConfig();
        setField(config, "activeKeyId", "known-kid");

        // Create a token signed with SIGNING_KEY_PAIR but with an unknown kid
        String jwt = createSignedToken(unknownKid, SIGNING_KEY_PAIR);

        // Set up KeyRotationManager with only "known-kid" mapped to KNOWN_KEY_PAIR
        // and NO fallback (null public key in fallback holder)
        ConcurrentHashMap<String, KeyPair> keyPairs = new ConcurrentHashMap<>();
        keyPairs.put("known-kid", KNOWN_KEY_PAIR);

        KeyPairHolder emptyFallback = new KeyPairHolder();
        setField(emptyFallback, "publicKey", null);
        setField(emptyFallback, "privateKey", null);

        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", config);
        setField(krm, "fallbackKeyPairHolder", emptyFallback);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", keyPairs);

        // Try to validate the token — should fail with 401
        TokenManagerImpl reader = createTokenManager(config, krm, emptyFallback);
        TestToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);

        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> reader.getUser(),
                "Token with unknown kid '" + unknownKid + "' should be rejected");

        assertEquals(401, ex.getStatusCode(),
                "Status code should be 401 for unknown kid '" + unknownKid + "'");
    }

    // --- Helpers ---

    private String createSignedToken(String kid, KeyPair keyPair) throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("STORE");
        claims.setAudience("web");
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(1);
        claims.setClaim("identity", "testuser");
        claims.setClaim("name", "Test User");
        claims.setClaim("roles", Arrays.asList("USER"));
        claims.setClaim("permissions", Collections.singletonMap("resource", Arrays.asList("read")));
        claims.setClaim("params", Collections.singletonMap("key", "value"));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(keyPair.getPrivate());
        jws.setKeyIdHeaderValue(kid);
        jws.setAlgorithmHeaderValue("RS256");
        return jws.getCompactSerialization();
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

    private TokenManagerImpl createTokenManager(
            DemoiselleSecurityJWTConfig config,
            KeyRotationManager krm,
            KeyPairHolder kph) throws Exception {
        TokenManagerImpl tm = new TokenManagerImpl();

        TestToken token = new TestToken();
        token.setType(TokenType.JWT);
        token.setKey("");

        DemoiselleUserImpl loggedUser = new DemoiselleUserImpl();
        loggedUser.init();

        setField(tm, "config", config);
        setField(tm, "keyPairHolder", kph);
        setField(tm, "keyRotationManager", krm);
        setField(tm, "token", token);
        setField(tm, "bundle", new StubMessages());
        setField(tm, "tokenBlacklist", new TokenBlacklist());
        setField(tm, "loggedUser", loggedUser);
        setField(tm, "claimsEnrichers", new SimpleInstance<>(Collections.emptyList()));

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

    static class SimpleInstance<T> implements Instance<T> {
        private final List<T> items;
        SimpleInstance(List<T> items) { this.items = items; }
        @Override public Iterator<T> iterator() { return items.iterator(); }
        @Override public T get() { return items.isEmpty() ? null : items.get(0); }
        @Override public Instance<T> select(java.lang.annotation.Annotation... q) { return this; }
        @Override @SuppressWarnings("unchecked")
        public <U extends T> Instance<U> select(Class<U> s, java.lang.annotation.Annotation... q) { return (Instance<U>) this; }
        @Override @SuppressWarnings("unchecked")
        public <U extends T> Instance<U> select(jakarta.enterprise.util.TypeLiteral<U> s, java.lang.annotation.Annotation... q) { return (Instance<U>) this; }
        @Override public boolean isUnsatisfied() { return items.isEmpty(); }
        @Override public boolean isAmbiguous() { return items.size() > 1; }
        @Override public boolean isResolvable() { return items.size() == 1; }
        @Override public void destroy(T i) {}
        @Override public Handle<T> getHandle() { return null; }
        @Override public Iterable<? extends Handle<T>> handles() { return Collections.emptyList(); }
    }
}
