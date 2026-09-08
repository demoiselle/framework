/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwx.HeaderParameterNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Self-contained validation tests for the JWT profile enforcement in
 * {@link JwtTokenValidatorImpl}. Tokens are minted with jose4j directly using a
 * locally generated RSA key pair so that {@code typ}, {@code iat}, max-age and
 * the {@code compat} profile can be asserted deterministically without CDI.
 *
 * @author SERPRO
 */
class JwtValidationProfileTest {

    private static KeyPair keyPair;

    @BeforeAll
    static void generateKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2_048);
        keyPair = kpg.genKeyPair();
    }

    // ---- helpers -----------------------------------------------------------

    private static void set(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static DemoiselleSecurityJWTMessages messages() {
        return (DemoiselleSecurityJWTMessages) java.lang.reflect.Proxy.newProxyInstance(
                DemoiselleSecurityJWTMessages.class.getClassLoader(),
                new Class<?>[]{DemoiselleSecurityJWTMessages.class},
                (proxy, method, args) -> method.getName());
    }

    private static KeyRotationManager keyRotationManager(DemoiselleSecurityJWTConfig config,
            DemoiselleSecurityJWTMessages bundle) {
        KeyPairHolder holder = new KeyPairHolder();
        set(holder, "publicKey", keyPair.getPublic());
        set(holder, "privateKey", keyPair.getPrivate());
        set(holder, "config", config);
        set(holder, "bundle", bundle);

        KeyRotationManager krm = new KeyRotationManager();
        set(krm, "config", config);
        set(krm, "fallbackKeyPairHolder", holder);
        set(krm, "bundle", bundle);
        set(krm, "keyPairs", new java.util.concurrent.ConcurrentHashMap<String, KeyPair>());
        return krm;
    }

    private static JwtTokenValidatorImpl validator(DemoiselleSecurityJWTConfig config) {
        DemoiselleSecurityJWTMessages bundle = messages();
        JwtTokenValidatorImpl v = new JwtTokenValidatorImpl();
        set(v, "config", config);
        set(v, "bundle", bundle);
        set(v, "keyRotationManager", keyRotationManager(config, bundle));
        set(v, "tokenBlacklist", new TokenBlacklist());
        // claimsEnrichers left null: the impl guards against null.
        return v;
    }

    private static DemoiselleSecurityJWTConfig config(Object... pairs) {
        DemoiselleSecurityJWTConfig cfg = new DemoiselleSecurityJWTConfig();
        for (int i = 0; i < pairs.length; i += 2) {
            set(cfg, (String) pairs[i], pairs[i + 1]);
        }
        return cfg;
    }

    /**
     * Mints a signed JWT with fine-grained control over standard claims/headers.
     *
     * @param issuer   iss value (null to omit)
     * @param audience aud value (null to omit)
     * @param typ      typ header (null to omit)
     * @param withIat  whether to emit iat
     * @param withJti  whether to emit jti
     * @param subject  sub value (null to omit)
     * @param iatOffsetSeconds seconds to add to "now" for iat (negative = past)
     */
    private static String mint(PrivateKey privateKey, String issuer, String audience, String typ,
            boolean withIat, boolean withJti, String subject, long iatOffsetSeconds) throws Exception {
        JwtClaims claims = new JwtClaims();
        if (issuer != null) {
            claims.setIssuer(issuer);
        }
        if (audience != null) {
            claims.setAudience(audience);
        }
        if (subject != null) {
            claims.setSubject(subject);
        }
        claims.setExpirationTimeMinutesInTheFuture(60);
        if (withIat) {
            claims.setIssuedAt(NumericDate.fromSeconds(NumericDate.now().getValue() + iatOffsetSeconds));
        }
        if (withJti) {
            claims.setGeneratedJwtId();
        }
        claims.setClaim("identity", subject != null ? subject : "1");
        claims.setClaim("name", "Teste");

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(privateKey);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        if (typ != null) {
            jws.setHeader(HeaderParameterNames.TYPE, typ);
        }
        return jws.getCompactSerialization();
    }

    // ---- compat profile ----------------------------------------------------

    @Test
    void compatAcceptsMinimalTokenWithoutTypIatJti() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "issuer", "STORE",
                "audience", "web",
                "timetoLiveMilliseconds", 9_999_999L);
        // profile defaults to compat
        String token = mint(keyPair.getPrivate(), "STORE", "web", null, false, false, null, 0);
        DemoiselleUser user = validator(cfg).validate(token);
        assertNotNull(user);
        assertEquals("1", user.getIdentity());
    }

    // ---- typ enforcement ----------------------------------------------------

    @Test
    void recommendedRejectsTokenWithoutTyp() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "issuer", "STORE",
                "audience", "web",
                "timetoLiveMilliseconds", 9_999_999L);
        String token = mint(keyPair.getPrivate(), "STORE", "web", null, true, true, null, 0);
        assertThrows(DemoiselleSecurityException.class, () -> validator(cfg).validate(token));
    }

    @Test
    void recommendedAcceptsTokenWithTypJwt() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "issuer", "STORE",
                "audience", "web",
                "timetoLiveMilliseconds", 9_999_999L);
        String token = mint(keyPair.getPrivate(), "STORE", "web", "JWT", true, true, "1", 0);
        DemoiselleUser user = validator(cfg).validate(token);
        assertNotNull(user);
    }

    @Test
    void expectedTypeMismatchIsRejected() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "expectedType", "at+jwt",
                "issuer", "STORE",
                "audience", "web",
                "timetoLiveMilliseconds", 9_999_999L);
        String token = mint(keyPair.getPrivate(), "STORE", "web", "JWT", true, true, "1", 0);
        assertThrows(DemoiselleSecurityException.class, () -> validator(cfg).validate(token));
    }

    // ---- iat enforcement ----------------------------------------------------

    @Test
    void recommendedRejectsTokenWithoutIat() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "issuer", "STORE",
                "audience", "web",
                "timetoLiveMilliseconds", 9_999_999L);
        String token = mint(keyPair.getPrivate(), "STORE", "web", "JWT", false, true, "1", 0);
        assertThrows(DemoiselleSecurityException.class, () -> validator(cfg).validate(token));
    }

    // ---- max-age enforcement ------------------------------------------------

    @Test
    void recommendedRejectsTokenOlderThanMaxAge() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "issuer", "STORE",
                "audience", "web",
                "maxTokenAgeSeconds", 60L,
                "clockSkewSeconds", 5);
        // iat 10 minutes in the past exceeds maxAge(60) + clockSkew(5)
        String token = mint(keyPair.getPrivate(), "STORE", "web", "JWT", true, true, "1", -600);
        assertThrows(DemoiselleSecurityException.class, () -> validator(cfg).validate(token));
    }

    @Test
    void recommendedAcceptsFreshTokenWithinMaxAge() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "issuer", "STORE",
                "audience", "web",
                "maxTokenAgeSeconds", 3_600L,
                "clockSkewSeconds", 60);
        String token = mint(keyPair.getPrivate(), "STORE", "web", "JWT", true, true, "1", -30);
        assertNotNull(validator(cfg).validate(token));
    }

    // ---- strict / subject ----------------------------------------------------

    @Test
    void strictRejectsTokenWithoutSubject() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "strict",
                "issuer", "STORE",
                "audience", "web",
                "timetoLiveMilliseconds", 9_999_999L);
        String token = mint(keyPair.getPrivate(), "STORE", "web", "JWT", true, true, null, 0);
        assertThrows(DemoiselleSecurityException.class, () -> validator(cfg).validate(token));
    }

    @Test
    void strictAcceptsCompleteToken() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "strict",
                "issuer", "STORE",
                "audience", "web",
                "timetoLiveMilliseconds", 9_999_999L);
        String token = mint(keyPair.getPrivate(), "STORE", "web", "JWT", true, true, "1", 0);
        assertNotNull(validator(cfg).validate(token));
    }

    // ---- fail-fast config validation ----------------------------------------

    @Test
    void recommendedWithoutIssuerConfiguredFailsFastBeforeValidation() throws Exception {
        DemoiselleSecurityJWTConfig cfg = config(
                "validationProfile", "recommended",
                "audience", "web");
        String token = mint(keyPair.getPrivate(), "STORE", "web", "JWT", true, true, "1", 0);
        assertThrows(IllegalStateException.class, () -> validator(cfg).validate(token));
    }
}
