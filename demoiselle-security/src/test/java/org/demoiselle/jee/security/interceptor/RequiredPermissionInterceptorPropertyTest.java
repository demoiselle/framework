/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.annotation.RequiredPermission;
import org.demoiselle.jee.security.event.AuthorizationEvent;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.interceptor.InvocationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 17: RequiredPermissionInterceptor permite se o usuário possui a permissão exigida
/**
 * Property-based tests for {@link RequiredPermissionInterceptor}.
 *
 * <p><b>Validates: Requirements 8.7, 8.8</b></p>
 */
class RequiredPermissionInterceptorPropertyTest {

    // The fixed resource/operation required by the @RequiredPermission annotation on the holder method.
    private static final String ANNOTATED_RESOURCE = "document";
    private static final String ANNOTATED_OPERATION = "read";

    // ---- Stubs ----

    /**
     * A SecurityContext stub with configurable login state and permission set.
     * Permissions are modeled as "resource:operation" strings in a set.
     */
    static class StubSecurityContext implements SecurityContext {
        private final boolean loggedIn;
        private final Set<String> userPermissions;

        StubSecurityContext(boolean loggedIn, Set<String> userPermissions) {
            this.loggedIn = loggedIn;
            this.userPermissions = userPermissions;
        }

        @Override public boolean isLoggedIn() { return loggedIn; }
        @Override public boolean hasPermission(String resource, String operation) {
            return userPermissions.contains(resource + ":" + operation);
        }
        @Override public boolean hasRole(String role) { return false; }
        @Override public DemoiselleUser getUser() { return null; }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return null; }
        @Override public void setUser(DemoiselleUser loggedUser) {}
        @Override public void setUser(DemoiselleUser loggedUser, String issuer, String audience) {}
        @Override public void removeUser(DemoiselleUser loggedUser) {}
    }

    /**
     * A simple DemoiselleSecurityMessages stub that returns plain strings.
     */
    static class StubMessages implements DemoiselleSecurityMessages {
        @Override public String accessCheckingPermission(String operacao, String recurso) { return "checking"; }
        @Override public String accessDenied(String usuario, String operacao, String recurso) { return "denied"; }
        @Override public String userNotAuthenticated() { return "User not authenticated"; }
        @Override public String invalidCredentials() { return "invalid"; }
        @Override public String doesNotHaveRole(String role) { return "no role: " + role; }
        @Override public String doesNotHavePermission(String operacao, String recurso) { return "no permission"; }
        @Override public String cloneError() { return "clone error"; }
    }

    /**
     * Event stub that captures fired AuthorizationEvents.
     */
    static class StubAuthzEvent implements Event<AuthorizationEvent> {
        final List<AuthorizationEvent> fired = new ArrayList<>();

        @Override public void fire(AuthorizationEvent event) { fired.add(event); }
        @Override public <U extends AuthorizationEvent> CompletionStage<U> fireAsync(U event) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> CompletionStage<U> fireAsync(U event, NotificationOptions options) { throw new UnsupportedOperationException(); }
        @Override public Event<AuthorizationEvent> select(Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> Event<U> select(Class<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthorizationEvent> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
    }

    /**
     * Holder class with a method annotated with @RequiredPermission.
     * Since annotation values are fixed at compile time, we use a fixed resource/operation
     * and vary the user's permissions in the property test.
     */
    static class AnnotatedHolder {
        @RequiredPermission(resource = "document", operation = "read")
        public void protectedMethod() {}
    }

    /**
     * InvocationContext stub whose getMethod() returns the annotated holder method.
     */
    static class StubInvocationContext implements InvocationContext {
        private static final Method ANNOTATED_METHOD;

        static {
            try {
                ANNOTATED_METHOD = AnnotatedHolder.class.getMethod("protectedMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new AnnotatedHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return ANNOTATED_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
    }

    // ---- Helper ----

    private RequiredPermissionInterceptor createInterceptor(SecurityContext secCtx, StubAuthzEvent eventStub) throws Exception {
        RequiredPermissionInterceptor interceptor = new RequiredPermissionInterceptor();

        Field scField = RequiredPermissionInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = RequiredPermissionInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, new StubMessages());

        Field authzEventField = RequiredPermissionInterceptor.class.getDeclaredField("authzEvent");
        authzEventField.setAccessible(true);
        authzEventField.set(interceptor, eventStub);

        return interceptor;
    }

    // ---- Property Tests ----

    // Feature: security-enhancements, Property 17: RequiredPermissionInterceptor permite se o usuário possui a permissão exigida
    /**
     * Property 17: For any resource/operation pair required via @RequiredPermission and any
     * authenticated user, the RequiredPermissionInterceptor must allow execution if the user
     * has the corresponding permission. If not, reject with 403.
     *
     * <p><b>Validates: Requirements 8.7, 8.8</b></p>
     *
     * The annotation on the holder method requires resource="document", operation="read".
     * We generate random sets of permissions for the user and a random login state,
     * then verify the interceptor behaves correctly in all cases.
     */
    @Property(tries = 100)
    void allowsIffUserHasPermission(
            @ForAll("userPermissionSets") Set<String> userPermissions,
            @ForAll boolean loggedIn) throws Exception {

        StubSecurityContext secCtx = new StubSecurityContext(loggedIn, userPermissions);
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredPermissionInterceptor interceptor = createInterceptor(secCtx, eventStub);
        InvocationContext ic = new StubInvocationContext();

        if (!loggedIn) {
            // Not authenticated → must reject with 403 (RequiredPermissionInterceptor uses 403 for unauthenticated)
            DemoiselleSecurityException thrown = assertThrows(
                    DemoiselleSecurityException.class,
                    () -> interceptor.manage(ic),
                    "Interceptor must reject unauthenticated user. loggedIn=" + loggedIn);
            assertEquals(403, thrown.getStatusCode(),
                    "Exception status must be 403 FORBIDDEN for unauthenticated user");
        } else {
            // Authenticated — check if user has the required permission
            String requiredPermissionKey = ANNOTATED_RESOURCE + ":" + ANNOTATED_OPERATION;
            boolean expectedAllow = userPermissions.contains(requiredPermissionKey);

            if (expectedAllow) {
                // Should allow — proceed() returns "proceeded"
                Object result = interceptor.manage(ic);
                assertEquals("proceeded", result,
                        "Interceptor must allow when user has the required permission. User permissions: " + userPermissions);
            } else {
                // Should reject with 403
                DemoiselleSecurityException thrown = assertThrows(
                        DemoiselleSecurityException.class,
                        () -> interceptor.manage(ic),
                        "Interceptor must reject when user lacks the required permission. User permissions: " + userPermissions);
                assertEquals(403, thrown.getStatusCode(),
                        "Exception status must be 403 FORBIDDEN");
            }
        }
    }

    /**
     * Generates random sets of permissions as "resource:operation" strings.
     * The pool includes the annotated permission plus several non-matching permissions,
     * ensuring we test both matching and non-matching scenarios.
     */
    @Provide
    Arbitrary<Set<String>> userPermissionSets() {
        // Pool: the required permission + non-matching permissions
        Arbitrary<String> permissionPool = Arbitraries.of(
                "document:read",    // matching
                "document:write",   // same resource, different operation
                "report:read",      // different resource, same operation
                "report:export",    // completely different
                "user:delete",      // completely different
                "config:update"     // completely different
        );
        return permissionPool.set().ofMinSize(0).ofMaxSize(6);
    }
}
