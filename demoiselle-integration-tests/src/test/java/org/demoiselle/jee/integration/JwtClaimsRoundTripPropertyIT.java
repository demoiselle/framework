/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.integration;

// Feature: cross-cutting-improvements, Property 8: Round-trip de claims JWT

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig;
import org.demoiselle.jee.security.jwt.impl.KeyPairHolder;
import org.demoiselle.jee.security.jwt.impl.KeyRotationManager;
import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based integration test for JWT claims round-trip.
 *
 * <p>Property 8: For any valid set of JWT claims (subject, name, roles,
 * permissions, params), issuing a token and validating it back must preserve
 * all original claims.</p>
 *
 * <p><b>Validates: Requirement 11.2</b></p>
 */
@EnabledIf("isSecurityJwtAvailable")
class JwtClaimsRoundTripPropertyIT {

    static boolean isSecurityJwtAvailable() {
        try {
            Class.forName("org.demoiselle.jee.security.jwt.impl.TokenManagerImpl");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static KeyPair keyPair;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            keyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // ---------------------------------------------------------------
    // Property 8: Round-trip de claims JWT
    // ---------------------------------------------------------------

    /**
     * Property 8: For any randomly generated valid JWT claims (identity, name,
     * roles, permissions, params), issuing a token via TokenManagerImpl.setUser()
     * and validating it via getUser() on a fresh TokenManagerImpl must preserve
     * all original claims.
     *
     * <p><b>Validates: Requirements 11.2</b></p>
     */
    @Property(tries = 100)
    void jwtClaimsRoundTrip(
            @ForAll("identities") String identity,
            @ForAll("names") String name,
            @ForAll("roleSets") Set<String> roles,
            @ForAll("permissionMaps") Map<String, String> permissions,
            @ForAll("paramMaps") Map<String, String> params) throws Exception {

        // --- Build user with generated claims ---
        DemoiselleUserImpl originalUser = newUser(identity, name);
        for (String role : roles) {
            originalUser.addRole(role);
        }
        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            originalUser.addPermission(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            originalUser.addParam(entry.getKey(), entry.getValue());
        }

        // --- Issue token ---
        TokenManagerImpl issuer = createTokenManager(keyPair);
        SimpleToken issuerToken = getToken(issuer);
        issuer.setUser(originalUser);
        String jwt = issuerToken.getKey();
        assertNotNull(jwt, "Token must be issued for valid user");

        // --- Validate token on a fresh TokenManagerImpl ---
        TokenManagerImpl reader = createTokenManager(keyPair);
        SimpleToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);
        DemoiselleUser validatedUser = reader.getUser();
        assertNotNull(validatedUser, "Validated user must not be null");

        // --- Verify all claims are preserved ---
        assertEquals(identity, validatedUser.getIdentity(),
                "Identity must be preserved after round-trip");
        assertEquals(name, validatedUser.getName(),
                "Name must be preserved after round-trip");

        // Roles: compare as sorted lists for order-independent equality
        List<String> expectedRoles = new ArrayList<>(roles);
        Collections.sort(expectedRoles);
        List<String> actualRoles = new ArrayList<>(validatedUser.getRoles());
        Collections.sort(actualRoles);
        assertEquals(expectedRoles, actualRoles,
                "Roles must be preserved after round-trip");

        // Permissions: each resource→operation must be present
        Map<String, List<String>> actualPermissions = validatedUser.getPermissions();
        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            assertTrue(actualPermissions.containsKey(entry.getKey()),
                    "Permission resource '" + entry.getKey() + "' must be preserved");
            assertTrue(actualPermissions.get(entry.getKey()).contains(entry.getValue()),
                    "Permission operation '" + entry.getValue() + "' for resource '"
                            + entry.getKey() + "' must be preserved");
        }
        // Verify no extra permission resources
        assertEquals(permissions.size(), actualPermissions.size(),
                "Number of permission resources must match");

        // Params: each key→value must be present
        Map<String, String> actualParams = validatedUser.getParams();
        assertEquals(params, actualParams,
                "Params must be preserved after round-trip");
    }

    // ---------------------------------------------------------------
    // Generators
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<String> identities() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> names() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    @Provide
    Arbitrary<Set<String>> roleSets() {
        Arbitrary<String> role = Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10);
        return role.set().ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<Map<String, String>> permissionMaps() {
        Arbitrary<String> resource = Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10);
        Arbitrary<String> operation = Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10);
        return Combinators.combine(resource, operation).as(Map::entry)
                .set().ofMinSize(1).ofMaxSize(4)
                .map(entries -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    for (Map.Entry<String, String> e : entries) {
                        map.putIfAbsent(e.getKey(), e.getValue());
                    }
                    return map;
                });
    }

    @Provide
    Arbitrary<Map<String, String>> paramMaps() {
        Arbitrary<String> key = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> value = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15);
        return Combinators.combine(key, value).as(Map::entry)
                .set().ofMinSize(1).ofMaxSize(4)
                .map(entries -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    for (Map.Entry<String, String> e : entries) {
                        map.putIfAbsent(e.getKey(), e.getValue());
                    }
                    return map;
                });
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private DemoiselleUserImpl newUser(String identity, String name) {
        DemoiselleUserImpl u = new DemoiselleUserImpl();
        u.init();
        u.setIdentity(identity);
        u.setName(name);
        return u;
    }

    private TokenManagerImpl createTokenManager(KeyPair kp) throws Exception {
        TokenManagerImpl tm = new TokenManagerImpl();

        KeyPairHolder kph = new KeyPairHolder();
        setField(kph, "publicKey", kp.getPublic());
        setField(kph, "privateKey", kp.getPrivate());

        SimpleToken tkn = new SimpleToken();
        tkn.setType(TokenType.JWT);
        tkn.setKey("");

        DemoiselleUserImpl lu = new DemoiselleUserImpl();
        lu.init();

        DemoiselleSecurityJWTConfig cfg = createConfig();

        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", cfg);
        setField(krm, "fallbackKeyPairHolder", kph);
        setField(krm, "bundle", new StubJWTMessages());
        setField(krm, "keyPairs", new ConcurrentHashMap<>());

        setField(tm, "config", cfg);
        setField(tm, "keyPairHolder", kph);
        setField(tm, "keyRotationManager", krm);
        setField(tm, "token", tkn);
        setField(tm, "bundle", new StubJWTMessages());
        setField(tm, "tokenBlacklist", new TokenBlacklist());
        setField(tm, "loggedUser", lu);
        setField(tm, "claimsEnrichers", new EmptyInstance<>());
        setField(tm, "refreshTokenManagerInstance", new EmptyInstance<>());

        return tm;
    }

    private DemoiselleSecurityJWTConfig createConfig() throws Exception {
        DemoiselleSecurityJWTConfig cfg = new DemoiselleSecurityJWTConfig();
        setField(cfg, "algorithmIdentifiers", "RS256");
        setField(cfg, "issuer", "STORE");
        setField(cfg, "audience", "web");
        setField(cfg, "timetoLiveMilliseconds", 9_999_999_999L);
        setField(cfg, "type", "master");
        setField(cfg, "clockSkewSeconds", 0);
        return cfg;
    }

    private SimpleToken getToken(TokenManagerImpl tm) throws Exception {
        Field f = TokenManagerImpl.class.getDeclaredField("token");
        f.setAccessible(true);
        return (SimpleToken) f.get(tm);
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

    // ---------------------------------------------------------------
    // Stubs
    // ---------------------------------------------------------------

    static class SimpleToken implements Token {
        private String key;
        private TokenType type;
        @Override public String getKey() { return key; }
        @Override public void setKey(String key) { this.key = key; }
        @Override public TokenType getType() { return type; }
        @Override public void setType(TokenType type) { this.type = type; }
    }

    static class StubJWTMessages implements DemoiselleSecurityJWTMessages {
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

    /** Empty Instance implementation for optional CDI dependencies. */
    static class EmptyInstance<T> implements Instance<T> {
        @Override public Iterator<T> iterator() { return Collections.emptyIterator(); }
        @Override public T get() { return null; }
        @Override public Instance<T> select(Annotation... qualifiers) { return this; }
        @Override @SuppressWarnings("unchecked")
        public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) { return (Instance<U>) this; }
        @Override @SuppressWarnings("unchecked")
        public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) { return (Instance<U>) this; }
        @Override public boolean isUnsatisfied() { return true; }
        @Override public boolean isAmbiguous() { return false; }
        @Override public boolean isResolvable() { return false; }
        @Override public void destroy(T instance) {}
        @Override public Handle<T> getHandle() { return null; }
        @Override public Iterable<? extends Handle<T>> handles() { return Collections.emptyList(); }
    }
}
