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
import java.util.List;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig;
import org.demoiselle.jee.security.jwt.impl.KeyPairHolder;
import org.demoiselle.jee.security.jwt.impl.KeyRotationManager;
import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;
import org.demoiselle.jee.security.jwt.impl.JwtTokenValidatorImpl;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 8: Algorithm validation
 *
 * **Validates: Requirements 4.3, 4.4, 10.1**
 *
 * For all tokens signed with an algorithm NOT in allowedAlgorithms,
 * validation must reject with DemoiselleSecurityException and status 401.
 */
class AlgorithmValidationPropertyTest {

    /** Allowed algorithms for the test — only RS256 is permitted. */
    private static final List<String> ALLOWED_ALGORITHMS = Collections.singletonList("RS256");

    /**
     * Algorithms that are NOT in the allowed list.
     * We pick from real RSA algorithms that jose4j supports but are not RS256.
     */
    @Provide
    Arbitrary<String> disallowedAlgorithms() {
        return Arbitraries.of(
                "RS384",
                "RS512"
        );
    }

    /**
     * Simple Token implementation for testing.
     */
    static class TestToken implements Token {
        private String key;
        private TokenType type;

        @Override public String getKey() { return key; }
        @Override public void setKey(String key) { this.key = key; }
        @Override public TokenType getType() { return type; }
        @Override public void setType(TokenType type) { this.type = type; }
    }

    /**
     * Stub messages implementation that returns simple strings.
     */
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
        @Override public String typeServer(String text) { return "type-server: " + text; }
        @Override public String primaryKey(String text) { return "primary-key: " + text; }
        @Override public String publicKey(String text) { return "public-key: " + text; }
        @Override public String ageToken(String text) { return "age-token: " + text; }
        @Override public String issuer(String text) { return "issuer: " + text; }
        @Override public String audience(String text) { return "audience: " + text; }
        @Override public String tokenBlacklisted() { return "token-blacklisted"; }
        @Override public String algorithmNotAllowed() { return "algorithm-not-allowed"; }
        @Override public String kidNotFound() { return "kid-not-found"; }
        @Override public String refreshTokenInvalid() { return "refresh-token-invalid"; }
    }

    /**
     * Creates a config with allowedAlgorithms set to "RS256" only,
     * and algorithmIdentifiers also set to "RS256".
     */
    private DemoiselleSecurityJWTConfig createConfig() throws Exception {
        DemoiselleSecurityJWTConfig config = new DemoiselleSecurityJWTConfig();
        setField(config, "algorithmIdentifiers", "RS256");
        setField(config, "allowedAlgorithms", "RS256");
        setField(config, "issuer", "STORE");
        setField(config, "audience", "web");
        setField(config, "timetoLiveMilliseconds", 9999999999L);
        setField(config, "type", "master");
        return config;
    }

    /**
     * Creates a JWT token signed with the given algorithm using the provided key pair.
     */
    private String createSignedToken(String algorithm, KeyPair keyPair) throws Exception {
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
        jws.setKeyIdHeaderValue("test-key");
        jws.setAlgorithmHeaderValue(algorithm);
        return jws.getCompactSerialization();
    }

    /**
     * Sets up a TokenManagerImpl with all required fields via reflection.
     */
    private TokenManagerImpl createTokenManager(
            DemoiselleSecurityJWTConfig config,
            KeyPair keyPair,
            TestToken token) throws Exception {

        TokenManagerImpl tokenManager = new TokenManagerImpl();

        // Create and configure KeyPairHolder via reflection
        KeyPairHolder keyPairHolder = new KeyPairHolder();
        setField(keyPairHolder, "publicKey", keyPair.getPublic());
        setField(keyPairHolder, "privateKey", keyPair.getPrivate());

        // Create and configure KeyRotationManager with fallback to KeyPairHolder
        KeyRotationManager keyRotationManager = new KeyRotationManager();
        setField(keyRotationManager, "config", config);
        setField(keyRotationManager, "fallbackKeyPairHolder", keyPairHolder);
        setField(keyRotationManager, "bundle", new StubMessages());
        setField(keyRotationManager, "keyPairs", new java.util.concurrent.ConcurrentHashMap<>());

        // Inject all required fields
        setField(tokenManager, "config", config);
        setField(tokenManager, "keyPairHolder", keyPairHolder);
        setField(tokenManager, "keyRotationManager", keyRotationManager);
        setField(tokenManager, "token", token);
        setField(tokenManager, "bundle", new StubMessages());
        setField(tokenManager, "tokenBlacklist", new TokenBlacklist());

        // Create a DemoiselleUser for the loggedUser field
        org.demoiselle.jee.security.impl.DemoiselleUserImpl loggedUser =
                new org.demoiselle.jee.security.impl.DemoiselleUserImpl();
        setField(tokenManager, "loggedUser", loggedUser);

        JwtTokenValidatorImpl validator = new JwtTokenValidatorImpl();
        setField(validator, "keyRotationManager", keyRotationManager);
        setField(validator, "config", config);
        setField(validator, "bundle", new StubMessages());
        setField(validator, "tokenBlacklist", new TokenBlacklist());
        setField(validator, "claimsEnrichers", null);
        setField(tokenManager, "jwtTokenValidator", validator);

        return tokenManager;
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

    /**
     * P8: For all tokens signed with an algorithm NOT in allowedAlgorithms,
     * validation must reject with DemoiselleSecurityException and status 401.
     */
    @Property(tries = 100)
    void tokenWithDisallowedAlgorithmShouldBeRejected(
            @ForAll("disallowedAlgorithms") String disallowedAlg) throws Exception {

        // Generate an RSA key pair (works for RS256, RS384, RS512)
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        DemoiselleSecurityJWTConfig config = createConfig();

        // Create a token signed with the disallowed algorithm
        String jwtToken = createSignedToken(disallowedAlg, keyPair);

        TestToken token = new TestToken();
        token.setKey(jwtToken);
        token.setType(TokenType.JWT);

        TokenManagerImpl tokenManager = createTokenManager(config, keyPair, token);

        // The token should be rejected because the algorithm is not allowed
        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> tokenManager.getUser(),
                "Token signed with " + disallowedAlg + " should be rejected when only RS256 is allowed"
        );

        assertEquals(401, ex.getStatusCode(),
                "Status code should be 401 for disallowed algorithm " + disallowedAlg);
    }
}
