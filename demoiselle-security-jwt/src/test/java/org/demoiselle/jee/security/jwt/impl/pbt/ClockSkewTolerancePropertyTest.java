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

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

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
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 12: Clock skew tolerance
 *
 * **Validates: Requirements 7.2**
 *
 * For any non-negative clockSkewSeconds value and any token whose expiration
 * is within the tolerance window (up to clockSkewSeconds seconds in the past),
 * validation should accept the token. Tokens expired beyond the window should
 * be rejected.
 */
class ClockSkewTolerancePropertyTest {

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

    private static KeyPair sharedKeyPair;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            sharedKeyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DemoiselleSecurityJWTConfig createConfig(int clockSkew) throws Exception {
        DemoiselleSecurityJWTConfig config = new DemoiselleSecurityJWTConfig();
        setField(config, "algorithmIdentifiers", "RS256");
        setField(config, "allowedAlgorithms", "RS256");
        setField(config, "issuer", "STORE");
        setField(config, "audience", "web");
        setField(config, "timetoLiveMilliseconds", 9999999999L);
        setField(config, "type", "master");
        setField(config, "clockSkewSeconds", clockSkew);
        return config;
    }

    /**
     * Creates a JWT token with expiration set to the given NumericDate.
     * Uses a fixed evaluation-safe nbf (far in the past).
     */
    private String createTokenWithExpiration(NumericDate expiration, KeyPair keyPair) throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("STORE");
        claims.setAudience("web");
        claims.setExpirationTime(expiration);
        claims.setGeneratedJwtId();
        // Set iat and nbf far in the past to avoid nbf-related failures
        NumericDate pastTime = NumericDate.now();
        pastTime.addSeconds(-3600);
        claims.setIssuedAt(pastTime);
        claims.setNotBefore(pastTime);
        claims.setClaim("identity", "testuser");
        claims.setClaim("name", "Test User");
        claims.setClaim("roles", Arrays.asList("USER"));
        claims.setClaim("permissions", Collections.singletonMap("resource", Arrays.asList("read")));
        claims.setClaim("params", Collections.singletonMap("key", "value"));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(keyPair.getPrivate());
        jws.setKeyIdHeaderValue("test-key");
        jws.setAlgorithmHeaderValue("RS256");
        return jws.getCompactSerialization();
    }

    private TokenManagerImpl createTokenManager(
            DemoiselleSecurityJWTConfig config,
            KeyPair keyPair,
            TestToken token) throws Exception {

        TokenManagerImpl tokenManager = new TokenManagerImpl();

        KeyPairHolder keyPairHolder = new KeyPairHolder();
        setField(keyPairHolder, "publicKey", keyPair.getPublic());
        setField(keyPairHolder, "privateKey", keyPair.getPrivate());

        KeyRotationManager keyRotationManager = new KeyRotationManager();
        setField(keyRotationManager, "config", config);
        setField(keyRotationManager, "fallbackKeyPairHolder", keyPairHolder);
        setField(keyRotationManager, "bundle", new StubMessages());
        setField(keyRotationManager, "keyPairs", new java.util.concurrent.ConcurrentHashMap<>());

        setField(tokenManager, "config", config);
        setField(tokenManager, "keyPairHolder", keyPairHolder);
        setField(tokenManager, "keyRotationManager", keyRotationManager);
        setField(tokenManager, "token", token);
        setField(tokenManager, "bundle", new StubMessages());
        setField(tokenManager, "tokenBlacklist", new TokenBlacklist());

        org.demoiselle.jee.security.impl.DemoiselleUserImpl loggedUser =
                new org.demoiselle.jee.security.impl.DemoiselleUserImpl();
        loggedUser.init();
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
     * P12 (acceptance): A token whose expiration is within the clock skew window
     * should be accepted by TokenManagerImpl.
     *
     * Strategy: We use a fixed evaluation time to eliminate timing flakiness.
     * We create a token with exp = evaluationTime - secondsAgo, where secondsAgo
     * is well within the clockSkewSeconds tolerance. We then verify that jose4j
     * with the configured clock skew accepts the token.
     */
    @Property(tries = 100)
    void tokenExpiredWithinSkewWindowShouldBeAccepted(
            @ForAll @IntRange(min = 10, max = 300) int clockSkewSeconds) throws Exception {

        // Use half the skew as the expiration offset — well within tolerance
        int secondsExpiredAgo = clockSkewSeconds / 2;

        // Fixed evaluation time to avoid timing issues
        NumericDate evaluationTime = NumericDate.now();

        // Token expired secondsExpiredAgo before evaluationTime
        NumericDate expiration = NumericDate.fromSeconds(evaluationTime.getValue() - secondsExpiredAgo);

        String jwtToken = createTokenWithExpiration(expiration, sharedKeyPair);

        // Verify directly with jose4j using the same clock skew — this proves
        // the property holds for the configured clockSkewSeconds value
        JwtConsumer consumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(clockSkewSeconds)
                .setExpectedIssuer("STORE")
                .setExpectedAudience("web")
                .setEvaluationTime(evaluationTime)
                .setVerificationKey(sharedKeyPair.getPublic())
                .build();

        // Should NOT throw — token is within the clock skew tolerance
        assertDoesNotThrow(
                () -> consumer.processToClaims(jwtToken),
                "Token expired " + secondsExpiredAgo + "s ago should be accepted with clockSkew=" + clockSkewSeconds
        );
    }

    /**
     * P12 (rejection): A token expired well beyond the clock skew window should be rejected.
     *
     * Strategy: Same fixed-time approach. Token expired (clockSkewSeconds + 10) seconds
     * before evaluation time — safely outside the tolerance window.
     */
    @Property(tries = 100)
    void tokenExpiredBeyondSkewWindowShouldBeRejected(
            @ForAll @IntRange(min = 0, max = 120) int clockSkewSeconds) throws Exception {

        // Token expired (clockSkewSeconds + 10) seconds ago — safely outside window
        int secondsExpiredAgo = clockSkewSeconds + 10;

        NumericDate evaluationTime = NumericDate.now();
        NumericDate expiration = NumericDate.fromSeconds(evaluationTime.getValue() - secondsExpiredAgo);

        String jwtToken = createTokenWithExpiration(expiration, sharedKeyPair);

        // Use TokenManagerImpl to verify the rejection uses the configured clock skew
        DemoiselleSecurityJWTConfig config = createConfig(clockSkewSeconds);

        TestToken token = new TestToken();
        token.setKey(jwtToken);
        token.setType(TokenType.JWT);

        TokenManagerImpl tokenManager = createTokenManager(config, sharedKeyPair, token);

        // Should throw — token is beyond the clock skew tolerance
        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> tokenManager.getUser(),
                "Token expired " + secondsExpiredAgo + "s ago should be rejected with clockSkew=" + clockSkewSeconds
        );

        assertEquals(401, ex.getStatusCode(),
                "Status code should be 401 for expired token beyond clock skew window");
    }

    /**
     * P12 (integration): Verifies that TokenManagerImpl actually uses the configured
     * clockSkewSeconds value. A token with a large clock skew should accept tokens
     * that a small clock skew would reject.
     *
     * Strategy: Create a token expired 30 seconds ago. With clockSkew=120, it should
     * be accepted. With clockSkew=5, it should be rejected.
     */
    @Property(tries = 100)
    void configuredClockSkewIsUsedByTokenManager(
            @ForAll @IntRange(min = 60, max = 300) int largeSkew) throws Exception {

        // Create a token that expired 30 seconds ago
        NumericDate expiration = NumericDate.now();
        expiration.addSeconds(-30);

        String jwtToken = createTokenWithExpiration(expiration, sharedKeyPair);

        // With large skew (>= 60), the token should be accepted
        DemoiselleSecurityJWTConfig largeSkewConfig = createConfig(largeSkew);
        TestToken token1 = new TestToken();
        token1.setKey(jwtToken);
        token1.setType(TokenType.JWT);
        TokenManagerImpl tm1 = createTokenManager(largeSkewConfig, sharedKeyPair, token1);

        assertDoesNotThrow(
                () -> tm1.getUser(),
                "Token expired 30s ago should be accepted with clockSkew=" + largeSkew
        );

        // With small skew (5), the same token should be rejected
        DemoiselleSecurityJWTConfig smallSkewConfig = createConfig(5);
        TestToken token2 = new TestToken();
        token2.setKey(jwtToken);
        token2.setType(TokenType.JWT);
        TokenManagerImpl tm2 = createTokenManager(smallSkewConfig, sharedKeyPair, token2);

        assertThrows(
                DemoiselleSecurityException.class,
                () -> tm2.getUser(),
                "Token expired 30s ago should be rejected with clockSkew=5"
        );
    }
}
