/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.integration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.DemoiselleSecurityConfig;
import org.demoiselle.jee.security.annotation.RequiredRole;
import org.demoiselle.jee.security.event.AuthorizationEvent;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.filter.CorsFilter;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.interceptor.RequiredRoleInterceptor;
import org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig;
import org.demoiselle.jee.security.jwt.impl.KeyPairHolder;
import org.demoiselle.jee.security.jwt.impl.KeyRotationManager;
import org.demoiselle.jee.security.jwt.impl.TokenBlacklist;
import org.demoiselle.jee.security.jwt.impl.TokenManagerImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: Configuration → Security JWT → REST filters.
 *
 * <p>Validates the full flow: load security configuration properties,
 * issue a JWT token, validate the token, enforce role-based access control,
 * apply CORS headers, and reject expired tokens.</p>
 *
 * <p>Validates: Requirements 9.6, 10.1, 10.2, 10.3, 10.4, 10.5</p>
 */
@EnabledIf("isSecurityJwtAvailable")
class ConfigSecurityRestIT {

    static boolean isSecurityJwtAvailable() {
        try {
            Class.forName("org.demoiselle.jee.security.jwt.impl.TokenManagerImpl");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static KeyPair keyPair;

    @BeforeAll
    static void generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
    }

    private TokenManagerImpl tokenManager;
    private SimpleToken token;
    private DemoiselleUserImpl loggedUser;

    @BeforeEach
    void setUp() throws Exception {
        tokenManager = createTokenManager();
        token = getToken(tokenManager);
        loggedUser = getLoggedUser(tokenManager);
    }

    // ---------------------------------------------------------------
    // Test 1: Full flow — config → issue token → validate token
    // Validates: Requirements 10.1, 10.2
    // ---------------------------------------------------------------

    @Test
    void fullFlow_configLoadAndTokenIssueAndValidate() throws Exception {
        DemoiselleUserImpl user = newUser("admin-user", "Admin");
        user.addRole("admin");
        user.addPermission("resource", "read");
        user.addParam("tenant", "acme");

        // Issue token
        tokenManager.setUser(user);
        String jwt = token.getKey();
        assertNotNull(jwt, "JWT token must be issued");
        assertEquals(TokenType.JWT, token.getType());

        // Validate token on a fresh TokenManager (simulates a new request)
        TokenManagerImpl reader = createTokenManager();
        SimpleToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);

        DemoiselleUser result = reader.getUser();
        assertNotNull(result, "Token validation must return a user");
        assertEquals("admin-user", result.getIdentity());
        assertEquals("Admin", result.getName());
        assertTrue(result.getRoles().contains("admin"));
        assertTrue(result.getPermissions().containsKey("resource"));
        assertTrue(result.getPermissions().get("resource").contains("read"));
        assertEquals("acme", result.getParams().get("tenant"));
    }

    // ---------------------------------------------------------------
    // Test 2: @RequiredRole accepts valid token, rejects invalid
    // Validates: Requirement 10.3
    // ---------------------------------------------------------------

    @Test
    void requiredRole_acceptsValidTokenWithCorrectRole() throws Exception {
        // Issue token with "admin" role
        DemoiselleUserImpl user = newUser("admin-user", "Admin");
        user.addRole("admin");
        user.addPermission("res", "op");
        user.addParam("k", "v");
        tokenManager.setUser(user);
        String jwt = token.getKey();

        // Build a SecurityContext backed by a TokenManager that has the token
        SecurityContext secCtx = buildSecurityContext(jwt);
        assertTrue(secCtx.isLoggedIn(), "User with valid token should be logged in");
        assertTrue(secCtx.hasRole("admin"), "User should have admin role");

        // Wire the interceptor with this SecurityContext
        RequiredRoleInterceptor interceptor = createRoleInterceptor(secCtx);
        Object result = interceptor.manage(new AdminMethodInvocationContext());
        assertEquals("proceeded", result, "Interceptor should allow method execution");
    }

    @Test
    void requiredRole_rejectsTokenWithWrongRole() throws Exception {
        // Issue token with "viewer" role only
        DemoiselleUserImpl user = newUser("viewer-user", "Viewer");
        user.addRole("viewer");
        user.addPermission("res", "op");
        user.addParam("k", "v");
        tokenManager.setUser(user);
        String jwt = token.getKey();

        SecurityContext secCtx = buildSecurityContext(jwt);
        assertTrue(secCtx.isLoggedIn());
        assertFalse(secCtx.hasRole("admin"), "Viewer should not have admin role");

        RequiredRoleInterceptor interceptor = createRoleInterceptor(secCtx);
        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new AdminMethodInvocationContext()));
        assertEquals(403, ex.getStatusCode(), "Should be FORBIDDEN");
    }

    @Test
    void requiredRole_rejectsUnauthenticatedUser() throws Exception {
        // No token set — user is not logged in
        SecurityContext secCtx = buildSecurityContext(null);
        assertFalse(secCtx.isLoggedIn());

        RequiredRoleInterceptor interceptor = createRoleInterceptor(secCtx);
        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new AdminMethodInvocationContext()));
        assertEquals(401, ex.getStatusCode(), "Should be UNAUTHORIZED");
    }

    // ---------------------------------------------------------------
    // Test 3: CORS filter applies configured headers
    // Validates: Requirement 10.4
    // ---------------------------------------------------------------

    @Test
    void corsFilter_appliesConfiguredHeaders() throws Exception {
        DemoiselleSecurityConfig corsConfig = new DemoiselleSecurityConfig();
        setField(corsConfig, "corsEnabled", true);
        setField(corsConfig, "corsAllowedOrigins",
                new ArrayList<>(List.of("https://example.com")));
        setField(corsConfig, "corsAllowedMethods",
                new ArrayList<>(List.of("GET", "POST")));
        setField(corsConfig, "corsAllowedHeaders",
                new ArrayList<>(List.of("Content-Type", "Authorization")));
        setField(corsConfig, "corsMaxAge", 7200);

        CorsFilter filter = new CorsFilter();
        setField(filter, "config", corsConfig);
        setField(filter, "info", new StubResourceInfo());

        StubRequestContext reqCtx = new StubRequestContext("https://example.com");
        StubResponseContext resCtx = new StubResponseContext();

        filter.filter(reqCtx, resCtx);

        MultivaluedMap<String, Object> headers = resCtx.getHeaders();
        assertEquals("https://example.com",
                headers.getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST",
                headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("Content-Type, Authorization",
                headers.getFirst("Access-Control-Allow-Headers"));
        assertEquals("7200",
                headers.getFirst("Access-Control-Max-Age"));
    }

    @Test
    void corsFilter_rejectsDisallowedOrigin() throws Exception {
        DemoiselleSecurityConfig corsConfig = new DemoiselleSecurityConfig();
        setField(corsConfig, "corsEnabled", true);
        setField(corsConfig, "corsAllowedOrigins",
                new ArrayList<>(List.of("https://allowed.com")));

        CorsFilter filter = new CorsFilter();
        setField(filter, "config", corsConfig);
        setField(filter, "info", new StubResourceInfo());

        StubRequestContext reqCtx = new StubRequestContext("https://evil.com");
        StubResponseContext resCtx = new StubResponseContext();

        filter.filter(reqCtx, resCtx);

        assertNull(resCtx.getHeaders().getFirst("Access-Control-Allow-Origin"),
                "Disallowed origin should not get CORS headers");
    }

    // ---------------------------------------------------------------
    // Test 4: Expired token rejection
    // Validates: Requirement 10.5
    // ---------------------------------------------------------------

    @Test
    void expiredToken_isRejectedWithUnauthorized() throws Exception {
        // Create a TokenManager with TTL = 1ms (effectively expired immediately)
        // and clockSkew = 0 so the token is immediately invalid
        TokenManagerImpl shortLivedTm = createTokenManagerWithTtl(1L);
        SimpleToken shortToken = getToken(shortLivedTm);

        DemoiselleUserImpl user = newUser("temp-user", "Temp");
        user.addRole("user");
        user.addPermission("res", "op");
        user.addParam("k", "v");
        shortLivedTm.setUser(user);
        String jwt = shortToken.getKey();
        assertNotNull(jwt);

        // Wait briefly to ensure token expires
        Thread.sleep(50);

        // Try to validate on a fresh TokenManager (also with clockSkew=0)
        TokenManagerImpl reader = createTokenManagerWithTtl(1L);
        SimpleToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);

        DemoiselleSecurityException ex = assertThrows(
                DemoiselleSecurityException.class,
                () -> reader.getUser());
        assertEquals(401, ex.getStatusCode(), "Expired token should yield 401");
    }

    // ===============================================================
    // Helpers
    // ===============================================================

    private DemoiselleUserImpl newUser(String identity, String name) {
        DemoiselleUserImpl u = new DemoiselleUserImpl();
        u.init();
        u.setIdentity(identity);
        u.setName(name);
        return u;
    }

    private DemoiselleSecurityJWTConfig createConfig() throws Exception {
        return createConfigWithTtl(9_999_999_999L);
    }

    private DemoiselleSecurityJWTConfig createConfigWithTtl(long ttlMs) throws Exception {
        DemoiselleSecurityJWTConfig cfg = new DemoiselleSecurityJWTConfig();
        setField(cfg, "algorithmIdentifiers", "RS256");
        setField(cfg, "issuer", "STORE");
        setField(cfg, "audience", "web");
        setField(cfg, "timetoLiveMilliseconds", ttlMs);
        setField(cfg, "type", "master");
        setField(cfg, "clockSkewSeconds", 0);
        return cfg;
    }

    private TokenManagerImpl createTokenManager() throws Exception {
        return createTokenManagerWithTtl(9_999_999_999L);
    }

    private TokenManagerImpl createTokenManagerWithTtl(long ttlMs) throws Exception {
        TokenManagerImpl tm = new TokenManagerImpl();

        KeyPairHolder kph = new KeyPairHolder();
        setField(kph, "publicKey", keyPair.getPublic());
        setField(kph, "privateKey", keyPair.getPrivate());

        SimpleToken tkn = new SimpleToken();
        tkn.setType(TokenType.JWT);
        tkn.setKey("");

        DemoiselleUserImpl lu = new DemoiselleUserImpl();
        lu.init();

        DemoiselleSecurityJWTConfig cfg = createConfigWithTtl(ttlMs);

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

    private SimpleToken getToken(TokenManagerImpl tm) throws Exception {
        Field f = TokenManagerImpl.class.getDeclaredField("token");
        f.setAccessible(true);
        return (SimpleToken) f.get(tm);
    }

    private DemoiselleUserImpl getLoggedUser(TokenManagerImpl tm) throws Exception {
        Field f = TokenManagerImpl.class.getDeclaredField("loggedUser");
        f.setAccessible(true);
        return (DemoiselleUserImpl) f.get(tm);
    }

    /**
     * Builds a simple SecurityContext backed by a TokenManager with the given JWT.
     */
    private SecurityContext buildSecurityContext(String jwt) throws Exception {
        if (jwt == null) {
            return new StubSecurityContext(false, Set.of());
        }
        TokenManagerImpl reader = createTokenManager();
        SimpleToken readerToken = getToken(reader);
        readerToken.setKey(jwt);
        readerToken.setType(TokenType.JWT);
        DemoiselleUser user = reader.getUser();
        if (user == null) {
            return new StubSecurityContext(false, Set.of());
        }
        return new StubSecurityContext(true, Set.copyOf(user.getRoles()));
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

    // ===============================================================
    // Stubs and inner classes
    // ===============================================================

    /** Simple Token implementation for testing. */
    static class SimpleToken implements Token {
        private String key;
        private TokenType type;
        @Override public String getKey() { return key; }
        @Override public void setKey(String key) { this.key = key; }
        @Override public TokenType getType() { return type; }
        @Override public void setType(TokenType type) { this.type = type; }
    }

    /** SecurityContext stub with configurable login state and roles. */
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

    /** Stub for DemoiselleSecurityMessages. */
    static class StubSecurityMsgs implements DemoiselleSecurityMessages {
        @Override public String accessCheckingPermission(String o, String r) { return "checking"; }
        @Override public String accessDenied(String u, String o, String r) { return "denied"; }
        @Override public String userNotAuthenticated() { return "User not authenticated"; }
        @Override public String invalidCredentials() { return "invalid"; }
        @Override public String doesNotHaveRole(String role) { return "no role: " + role; }
        @Override public String doesNotHavePermission(String o, String r) { return "no permission"; }
        @Override public String cloneError() { return "clone error"; }
    }

    /** Stub for DemoiselleSecurityJWTMessages. */
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

    /** Event stub that captures fired AuthorizationEvents. */
    static class StubAuthzEvent implements Event<AuthorizationEvent> {
        final List<AuthorizationEvent> fired = new ArrayList<>();
        @Override public void fire(AuthorizationEvent event) { fired.add(event); }
        @Override public <U extends AuthorizationEvent> CompletionStage<U> fireAsync(U event) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> CompletionStage<U> fireAsync(U event, NotificationOptions options) { throw new UnsupportedOperationException(); }
        @Override public Event<AuthorizationEvent> select(Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> Event<U> select(Class<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
    }

    /** Annotated method holder for @RequiredRole("admin"). */
    static class AdminMethodHolder {
        @RequiredRole({"admin"})
        public void protectedMethod() {}
    }

    /** InvocationContext for AdminMethodHolder.protectedMethod(). */
    static class AdminMethodInvocationContext implements InvocationContext {
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

    /** Stub ContainerRequestContext with configurable Origin header. */
    static class StubRequestContext implements ContainerRequestContext {
        private final String origin;
        StubRequestContext(String origin) { this.origin = origin; }
        @Override public String getHeaderString(String name) {
            return "Origin".equalsIgnoreCase(name) ? origin : null;
        }
        @Override public Object getProperty(String name) { return null; }
        @Override public Collection<String> getPropertyNames() { return List.of(); }
        @Override public void setProperty(String name, Object object) {}
        @Override public void removeProperty(String name) {}
        @Override public jakarta.ws.rs.core.UriInfo getUriInfo() { return null; }
        @Override public void setRequestUri(java.net.URI requestUri) {}
        @Override public void setRequestUri(java.net.URI baseUri, java.net.URI requestUri) {}
        @Override public jakarta.ws.rs.core.Request getRequest() { return null; }
        @Override public String getMethod() { return "GET"; }
        @Override public void setMethod(String method) {}
        @Override public MultivaluedMap<String, String> getHeaders() { return new MultivaluedHashMap<>(); }
        @Override public Date getDate() { return null; }
        @Override public java.util.Locale getLanguage() { return null; }
        @Override public int getLength() { return 0; }
        @Override public jakarta.ws.rs.core.MediaType getMediaType() { return null; }
        @Override public List<jakarta.ws.rs.core.MediaType> getAcceptableMediaTypes() { return List.of(); }
        @Override public List<java.util.Locale> getAcceptableLanguages() { return List.of(); }
        @Override public Map<String, jakarta.ws.rs.core.Cookie> getCookies() { return Map.of(); }
        @Override public boolean hasEntity() { return false; }
        @Override public java.io.InputStream getEntityStream() { return null; }
        @Override public void setEntityStream(java.io.InputStream input) {}
        @Override public jakarta.ws.rs.core.SecurityContext getSecurityContext() { return null; }
        @Override public void setSecurityContext(jakarta.ws.rs.core.SecurityContext context) {}
        @Override public void abortWith(jakarta.ws.rs.core.Response response) {}
    }

    /** Stub ContainerResponseContext that captures headers. */
    static class StubResponseContext implements ContainerResponseContext {
        private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        @Override public MultivaluedMap<String, Object> getHeaders() { return headers; }
        @Override public int getStatus() { return 200; }
        @Override public void setStatus(int code) {}
        @Override public jakarta.ws.rs.core.Response.StatusType getStatusInfo() { return null; }
        @Override public void setStatusInfo(jakarta.ws.rs.core.Response.StatusType statusInfo) {}
        @Override public Class<?> getEntityClass() { return null; }
        @Override public java.lang.reflect.Type getEntityType() { return null; }
        @Override public void setEntity(Object entity) {}
        @Override public void setEntity(Object entity, Annotation[] annotations, jakarta.ws.rs.core.MediaType mediaType) {}
        @Override public Annotation[] getEntityAnnotations() { return new Annotation[0]; }
        @Override public java.io.OutputStream getEntityStream() { return null; }
        @Override public void setEntityStream(java.io.OutputStream outputStream) {}
        @Override public int getLength() { return 0; }
        @Override public jakarta.ws.rs.core.MediaType getMediaType() { return null; }
        @Override public Map<String, jakarta.ws.rs.core.NewCookie> getCookies() { return Map.of(); }
        @Override public MultivaluedMap<String, String> getStringHeaders() { return new MultivaluedHashMap<>(); }
        @Override public String getHeaderString(String name) { return null; }
        @Override public boolean hasEntity() { return false; }
        @Override public Object getEntity() { return null; }
        @Override public java.util.Locale getLanguage() { return null; }
        @Override public Date getDate() { return null; }
        @Override public java.net.URI getLocation() { return null; }
        @Override public Set<String> getAllowedMethods() { return Set.of(); }
        @Override public jakarta.ws.rs.core.EntityTag getEntityTag() { return null; }
        @Override public Date getLastModified() { return null; }
        @Override public Set<jakarta.ws.rs.core.Link> getLinks() { return Set.of(); }
        @Override public boolean hasLink(String relation) { return false; }
        @Override public jakarta.ws.rs.core.Link getLink(String relation) { return null; }
        @Override public jakarta.ws.rs.core.Link.Builder getLinkBuilder(String relation) { return null; }
    }

    /** Stub ResourceInfo returning null (no annotation-based CORS override). */
    static class StubResourceInfo implements ResourceInfo {
        @Override public Method getResourceMethod() { return null; }
        @Override public Class<?> getResourceClass() { return null; }
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
