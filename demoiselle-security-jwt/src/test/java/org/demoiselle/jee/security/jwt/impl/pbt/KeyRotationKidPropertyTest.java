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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
import org.demoiselle.jee.security.jwt.impl.JwtTokenValidatorImpl;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 10: kid round-trip in key rotation
 *
 * **Validates: Requirements 6.3, 6.4**
 *
 * For any token created with activeKeyId configured, the kid header matches
 * activeKeyId, and validation uses the correct public key.
 */
class KeyRotationKidPropertyTest {

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
     * Generates arbitrary kid identifiers (alphanumeric, 3-15 chars).
     */
    @Provide
    Arbitrary<String> kidIdentifiers() {
        return Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(3).ofMaxLength(15)
                .map(s -> "kid-" + s);
    }

    /**
     * P10: For any token created with activeKeyId, the kid header matches
     * activeKeyId, and validation uses the correct public key with success.
     */
    @Property(tries = 100)
    void kidRoundTripInKeyRotation(
            @ForAll("kidIdentifiers") String activeKid) throws Exception {

        // Set up config with the arbitrary activeKeyId
        DemoiselleSecurityJWTConfig config = createConfig();
        setField(config, "activeKeyId", activeKid);

        // Set up KeyRotationManager with the key pair mapped to the activeKid
        ConcurrentHashMap<String, KeyPair> keyPairs = new ConcurrentHashMap<>();
        keyPairs.put(activeKid, KEY_PAIR);

        KeyPairHolder fallbackHolder = new KeyPairHolder();
        setField(fallbackHolder, "publicKey", KEY_PAIR.getPublic());
        setField(fallbackHolder, "privateKey", KEY_PAIR.getPrivate());

        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", config);
        setField(krm, "fallbackKeyPairHolder", fallbackHolder);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", keyPairs);

        // Create user and sign token
        DemoiselleUserImpl user = newUser("user1", "Test User");
        user.addRole("USER");
        user.addPermission("res", "read");
        user.addParam("k", "v");

        TokenManagerImpl writer = createTokenManager(config, krm, fallbackHolder);
        TestToken writerToken = getToken(writer);
        writer.setUser(user);
        String jwt = writerToken.getKey();
        assertNotNull(jwt, "Token should be generated");

        // Verify kid header matches activeKeyId
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(jwt);
        String headerKid = jws.getKeyIdHeaderValue();
        assertEquals(activeKid, headerKid,
                "kid header should match activeKeyId");

        // Validate the token — should succeed using the correct public key
        TokenManagerImpl reader = createTokenManager(config, krm, fallbackHolder);
        TestToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);
        DemoiselleUser result = reader.getUser();
        assertNotNull(result, "Token validation should succeed");
        assertEquals("user1", result.getIdentity());
        assertEquals("Test User", result.getName());
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

        JwtTokenValidatorImpl validator = new JwtTokenValidatorImpl();
        setField(validator, "keyRotationManager", krm);
        setField(validator, "config", config);
        setField(validator, "bundle", new StubMessages());
        setField(validator, "tokenBlacklist", new TokenBlacklist());
        setField(validator, "claimsEnrichers", new SimpleInstance<>(Collections.emptyList()));
        setField(tm, "jwtTokenValidator", validator);

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
