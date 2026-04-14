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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.demoiselle.jee.security.jwt.impl.RefreshTokenManager;
import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;
import org.demoiselle.jee.security.jwt.impl.JwtTokenValidatorImpl;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 1: Round-trip setUser/getUser
 *
 * **Validates: Requirements 8.1, 8.3**
 *
 * For ALL valid DemoiselleUser instances with arbitrary identity, name, roles,
 * permissions and params, the operation setUser(user) followed by getUser()
 * must produce a DemoiselleUser with equivalent identity, name, roles,
 * permissions and params.
 */
class TokenManagerRoundTripPropertyTest {

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

    // --- Arbitrary Generators ---

    /**
     * Generates alphanumeric strings of 1-50 chars for identity.
     */
    @Provide
    Arbitrary<String> identities() {
        return Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(50);
    }

    /**
     * Generates alphanumeric strings of 1-100 chars for name.
     */
    @Provide
    Arbitrary<String> names() {
        return Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(100);
    }

    /**
     * Generates a list of 0-5 unique alphanumeric role strings.
     * DemoiselleUserImpl deduplicates roles via addRole, so we generate unique values.
     */
    @Provide
    Arbitrary<List<String>> roleLists() {
        Arbitrary<String> role = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(20);
        return role.list().ofMinSize(0).ofMaxSize(5).uniqueElements();
    }

    /**
     * Generates a permissions map: 0-3 resources, each with 1-3 unique operations.
     */
    @Provide
    Arbitrary<Map<String, List<String>>> permissionMaps() {
        Arbitrary<String> resource = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(15);
        Arbitrary<List<String>> operations = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(15)
                .list().ofMinSize(1).ofMaxSize(3).uniqueElements();
        return Combinators.combine(
                resource.list().ofMinSize(0).ofMaxSize(3).uniqueElements(),
                operations.list().ofMinSize(0).ofMaxSize(3)
        ).as((resources, opLists) -> {
            Map<String, List<String>> map = new ConcurrentHashMap<>();
            for (int i = 0; i < resources.size() && i < opLists.size(); i++) {
                map.put(resources.get(i), new ArrayList<>(opLists.get(i)));
            }
            return map;
        });
    }

    /**
     * Generates a params map: 0-5 key-value pairs with alphanumeric keys/values.
     */
    @Provide
    Arbitrary<Map<String, String>> paramMaps() {
        Arbitrary<String> keys = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(15);
        Arbitrary<String> values = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1).ofMaxLength(20);
        return Arbitraries.maps(keys, values).ofMinSize(0).ofMaxSize(5);
    }

    /**
     * P1: For ALL valid DemoiselleUser instances, setUser() followed by getUser()
     * produces a DemoiselleUser with equivalent identity, name, roles, permissions and params.
     */
    @Property(tries = 100)
    void roundTripSetUserGetUserPreservesAllFields(
            @ForAll("identities") String identity,
            @ForAll("names") String name,
            @ForAll("roleLists") List<String> roles,
            @ForAll("permissionMaps") Map<String, List<String>> permissions,
            @ForAll("paramMaps") Map<String, String> params) throws Exception {

        // Build the input user
        DemoiselleUserImpl inputUser = new DemoiselleUserImpl();
        inputUser.init();
        inputUser.setIdentity(identity);
        inputUser.setName(name);
        for (String role : roles) {
            inputUser.addRole(role);
        }
        for (Map.Entry<String, List<String>> entry : permissions.entrySet()) {
            for (String op : entry.getValue()) {
                inputUser.addPermission(entry.getKey(), op);
            }
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            inputUser.addParam(entry.getKey(), entry.getValue());
        }

        // setUser
        TokenManagerImpl writer = createTokenManager();
        TestToken writerToken = getToken(writer);
        writer.setUser(inputUser);
        String jwt = writerToken.getKey();
        assertNotNull(jwt, "Token should be generated");

        // getUser
        TokenManagerImpl reader = createTokenManager();
        TestToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);
        DemoiselleUser result = reader.getUser();
        assertNotNull(result, "getUser() should return a user");

        // Verify identity
        assertEquals(identity, result.getIdentity(), "Identity must be preserved");

        // Verify name
        assertEquals(name, result.getName(), "Name must be preserved");

        // Verify roles (compare as sets since implementation deduplicates)
        assertEquals(new HashSet<>(roles), new HashSet<>(result.getRoles()),
                "Roles must be preserved (as set)");

        // Verify permissions
        Map<String, List<String>> resultPermissions = result.getPermissions();
        assertEquals(permissions.size(), resultPermissions.size(),
                "Number of permission resources must match");
        for (Map.Entry<String, List<String>> entry : permissions.entrySet()) {
            assertTrue(resultPermissions.containsKey(entry.getKey()),
                    "Permission resource '" + entry.getKey() + "' must be present");
            assertEquals(new HashSet<>(entry.getValue()),
                    new HashSet<>(resultPermissions.get(entry.getKey())),
                    "Operations for resource '" + entry.getKey() + "' must match");
        }

        // Verify params
        assertEquals(params, result.getParams(), "Params must be preserved");
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

    private TokenManagerImpl createTokenManager() throws Exception {
        TokenManagerImpl tm = new TokenManagerImpl();

        DemoiselleSecurityJWTConfig config = createConfig();

        KeyPairHolder kph = new KeyPairHolder();
        setField(kph, "publicKey", KEY_PAIR.getPublic());
        setField(kph, "privateKey", KEY_PAIR.getPrivate());

        KeyRotationManager krm = new KeyRotationManager();
        setField(krm, "config", config);
        setField(krm, "fallbackKeyPairHolder", kph);
        setField(krm, "bundle", new StubMessages());
        setField(krm, "keyPairs", new ConcurrentHashMap<>());

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
        setField(tm, "claimsEnrichers", new SimpleInstance<ClaimsEnricher>(Collections.emptyList()));
        setField(tm, "refreshTokenManagerInstance", new SimpleInstance<RefreshTokenManager>(Collections.emptyList()));

        JwtTokenValidatorImpl validator = new JwtTokenValidatorImpl();
        setField(validator, "keyRotationManager", krm);
        setField(validator, "config", config);
        setField(validator, "bundle", new StubMessages());
        setField(validator, "tokenBlacklist", new TokenBlacklist());
        setField(validator, "claimsEnrichers", new SimpleInstance<ClaimsEnricher>(Collections.emptyList()));
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
