/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.integration;

// Feature: cross-cutting-improvements, Property 6: Interceptor de segurança aceita tokens válidos e rejeita inválidos

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.annotation.RequiredRole;
import org.demoiselle.jee.security.event.AuthorizationEvent;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.interceptor.RequiredRoleInterceptor;
import org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig;
import org.demoiselle.jee.security.jwt.impl.KeyPairHolder;
import org.demoiselle.jee.security.jwt.impl.KeyRotationManager;
import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;
import org.demoiselle.jee.security.jwt.impl.JwtTokenValidatorImpl;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.interceptor.InvocationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based integration test for the security interceptor chain.
 *
 * <p>Property 6: For any JWT token, if valid (correct signature, not expired,
 * correct claims) the security interceptor allows execution; if invalid
 * (corrupted/tampered), the interceptor rejects the request.</p>
 *
 * <p><b>Validates: Requirements 10.3, 10.5</b></p>
 */
@EnabledIf("isSecurityJwtAvailable")
class SecurityInterceptorPropertyIT {

    static boolean isSecurityJwtAvailable() {
        try {
            Class.forName("org.demoiselle.jee.security.jwt.impl.TokenManagerImpl");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static KeyPair keyPair;
    private static KeyPair wrongKeyPair;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            keyPair = keyGen.generateKeyPair();
            wrongKeyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // ---------------------------------------------------------------
    // Property 6: Valid tokens accepted, invalid tokens rejected
    // ---------------------------------------------------------------

    /**
     * Property 6: For any randomly generated valid claims (identity, name, roles),
     * issuing a JWT with the correct key and validating it through the security
     * interceptor must allow execution. Tampering with the token must cause rejection.
     *
     * <p><b>Validates: Requirements 10.3, 10.5</b></p>
     */
    @Property(tries = 100)
    void validTokenAccepted_invalidTokenRejected(
            @ForAll("identities") String identity,
            @ForAll("names") String name,
            @ForAll("roleSets") Set<String> roles,
            @ForAll boolean tamper) throws Exception {

        // --- Issue a valid token ---
        TokenManagerImpl issuer = createTokenManager(keyPair);
        SimpleToken issuerToken = getToken(issuer);

        DemoiselleUserImpl user = newUser(identity, name);
        for (String role : roles) {
            user.addRole(role);
        }
        // TokenManagerImpl requires at least one permission and one param
        user.addPermission("res", "op");
        user.addParam("k", "v");

        issuer.setUser(user);
        String jwt = issuerToken.getKey();
        assertNotNull(jwt, "Token must be issued for valid user");

        if (tamper) {
            // --- Invalid path: corrupt the token payload ---
            jwt = tamperToken(jwt);

            // Build a SecurityContext that tries to validate the tampered token
            // Validation should fail, so the user is not logged in
            SecurityContext secCtx = buildSecurityContextSafe(jwt, keyPair);
            assertFalse(secCtx.isLoggedIn(),
                    "Tampered token must not produce a logged-in user");

            // The interceptor must reject with 401 (unauthenticated)
            RequiredRoleInterceptor interceptor = createRoleInterceptor(secCtx);
            DemoiselleSecurityException ex = assertThrows(
                    DemoiselleSecurityException.class,
                    () -> interceptor.manage(new RoleMethodInvocationContext()),
                    "Interceptor must reject tampered token");
            assertEquals(401, ex.getStatusCode(),
                    "Tampered token rejection must be 401 UNAUTHORIZED");
        } else {
            // --- Valid path: token with correct signature ---
            SecurityContext secCtx = buildSecurityContext(jwt, keyPair);
            assertTrue(secCtx.isLoggedIn(),
                    "Valid token must produce a logged-in user");

            // Check that the user has the expected roles
            boolean hasRequiredRole = roles.contains("admin");

            RequiredRoleInterceptor interceptor = createRoleInterceptor(secCtx);

            if (hasRequiredRole) {
                Object result = interceptor.manage(new RoleMethodInvocationContext());
                assertEquals("proceeded", result,
                        "Interceptor must allow valid token with required role");
            } else {
                DemoiselleSecurityException ex = assertThrows(
                        DemoiselleSecurityException.class,
                        () -> interceptor.manage(new RoleMethodInvocationContext()),
                        "Interceptor must reject valid token without required role");
                assertEquals(403, ex.getStatusCode(),
                        "Missing role rejection must be 403 FORBIDDEN");
            }
        }
    }

    // ---------------------------------------------------------------
    // Generators
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<String> identities() {
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> names() {
        return Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(30);
    }

    @Provide
    Arbitrary<Set<String>> roleSets() {
        // Pool includes "admin" (the required role) and several non-matching roles
        Arbitrary<String> rolePool = Arbitraries.of(
                "admin", "manager", "editor", "viewer", "guest");
        return rolePool.set().ofMinSize(1).ofMaxSize(4);
    }

    // ---------------------------------------------------------------
    // Token tampering
    // ---------------------------------------------------------------

    /**
     * Corrupts the JWT payload by flipping characters in the middle (payload) segment.
     */
    private String tamperToken(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            return jwt + "TAMPERED";
        }
        // Corrupt the payload (second part) by replacing a character
        char[] payload = parts[1].toCharArray();
        if (payload.length > 2) {
            // Flip a character in the middle of the payload
            int mid = payload.length / 2;
            payload[mid] = (payload[mid] == 'A') ? 'B' : 'A';
        }
        return parts[0] + "." + new String(payload) + "." + parts[2];
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

        JwtTokenValidatorImpl validator = new JwtTokenValidatorImpl();
        setField(validator, "keyRotationManager", krm);
        setField(validator, "config", cfg);
        setField(validator, "bundle", new StubJWTMessages());
        setField(validator, "tokenBlacklist", new TokenBlacklist());
        setField(validator, "claimsEnrichers", null);
        setField(tm, "jwtTokenValidator", validator);

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

    /**
     * Builds a SecurityContext by validating the JWT. If validation succeeds,
     * the context is logged in with the user's roles.
     */
    private SecurityContext buildSecurityContext(String jwt, KeyPair kp) throws Exception {
        TokenManagerImpl reader = createTokenManager(kp);
        SimpleToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);
        DemoiselleUser validatedUser = reader.getUser();
        if (validatedUser == null) {
            return new StubSecurityContext(false, Set.of());
        }
        return new StubSecurityContext(true, Set.copyOf(validatedUser.getRoles()));
    }

    /**
     * Builds a SecurityContext that handles validation failure gracefully.
     * Returns a not-logged-in context if the token is invalid.
     */
    private SecurityContext buildSecurityContextSafe(String jwt, KeyPair kp) {
        try {
            return buildSecurityContext(jwt, kp);
        } catch (DemoiselleSecurityException e) {
            return new StubSecurityContext(false, Set.of());
        } catch (Exception e) {
            return new StubSecurityContext(false, Set.of());
        }
    }

    private RequiredRoleInterceptor createRoleInterceptor(SecurityContext secCtx) throws Exception {
        RequiredRoleInterceptor interceptor = new RequiredRoleInterceptor();
        setField(interceptor, "securityContext", secCtx);
        setField(interceptor, "bundle", new StubSecurityMsgs());
        setField(interceptor, "authzEvent", new StubAuthzEvent());
        return interceptor;
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

    static class StubSecurityContext implements SecurityContext {
        private final boolean loggedIn;
        private final Set<String> roles;

        StubSecurityContext(boolean loggedIn, Set<String> roles) {
            this.loggedIn = loggedIn;
            this.roles = roles;
        }

        @Override public boolean isLoggedIn() { return loggedIn; }
        @Override public boolean hasPermission(String resource, String operation) { return false; }
        @Override public boolean hasRole(String role) { return roles.contains(role); }
        @Override public DemoiselleUser getUser() { return null; }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return null; }
        @Override public void setUser(DemoiselleUser loggedUser) {}
        @Override public void setUser(DemoiselleUser loggedUser, String issuer, String audience) {}
        @Override public void removeUser(DemoiselleUser loggedUser) {}
    }

    static class StubSecurityMsgs implements DemoiselleSecurityMessages {
        @Override public String accessCheckingPermission(String o, String r) { return "checking"; }
        @Override public String accessDenied(String u, String o, String r) { return "denied"; }
        @Override public String userNotAuthenticated() { return "User not authenticated"; }
        @Override public String invalidCredentials() { return "invalid"; }
        @Override public String doesNotHaveRole(String role) { return "no role: " + role; }
        @Override public String doesNotHavePermission(String o, String r) { return "no permission"; }
        @Override public String cloneError() { return "clone error"; }
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

    static class StubAuthzEvent implements Event<AuthorizationEvent> {
        final List<AuthorizationEvent> fired = new ArrayList<>();
        @Override public void fire(AuthorizationEvent event) { fired.add(event); }
        @Override public <U extends AuthorizationEvent> CompletionStage<U> fireAsync(U event) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> CompletionStage<U> fireAsync(U event, NotificationOptions options) { throw new UnsupportedOperationException(); }
        @Override public Event<AuthorizationEvent> select(Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> Event<U> select(Class<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
    }

    /** Method holder annotated with @RequiredRole("admin"). */
    static class AdminMethodHolder {
        @RequiredRole({"admin"})
        public void protectedMethod() {}
    }

    /** InvocationContext for AdminMethodHolder.protectedMethod(). */
    static class RoleMethodInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;
        static {
            try {
                TARGET_METHOD = AdminMethodHolder.class.getMethod("protectedMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        @Override public Object getTarget() { return new AdminMethodHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
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
