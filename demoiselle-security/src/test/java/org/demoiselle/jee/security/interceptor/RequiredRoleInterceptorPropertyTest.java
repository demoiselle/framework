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
import org.demoiselle.jee.security.annotation.RequiredRole;
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

// Feature: security-enhancements, Property 16: RequiredRoleInterceptor permite se o usuário possui pelo menos uma role exigida
/**
 * Property-based tests for {@link RequiredRoleInterceptor}.
 *
 * <p><b>Validates: Requirements 8.4, 8.5, 8.6</b></p>
 */
class RequiredRoleInterceptorPropertyTest {

    // Fixed roles used in the @RequiredRole annotation on the holder method.
    private static final String[] ANNOTATED_ROLES = {"admin", "manager", "editor"};

    // ---- Stubs ----

    /**
     * A SecurityContext stub with configurable login state and role set.
     */
    static class StubSecurityContext implements SecurityContext {
        private final boolean loggedIn;
        private final Set<String> userRoles;

        StubSecurityContext(boolean loggedIn, Set<String> userRoles) {
            this.loggedIn = loggedIn;
            this.userRoles = userRoles;
        }

        @Override public boolean isLoggedIn() { return loggedIn; }
        @Override public boolean hasPermission(String resource, String operation) { return false; }
        @Override public boolean hasRole(String role) { return userRoles.contains(role); }
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
     * Holder class with a method annotated with @RequiredRole.
     * Since annotation values are fixed at compile time, we use a fixed set of roles
     * and vary the user's roles in the property test.
     */
    static class AnnotatedHolder {
        @RequiredRole({"admin", "manager", "editor"})
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

    private RequiredRoleInterceptor createInterceptor(SecurityContext secCtx, StubAuthzEvent eventStub) throws Exception {
        RequiredRoleInterceptor interceptor = new RequiredRoleInterceptor();

        Field scField = RequiredRoleInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = RequiredRoleInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, new StubMessages());

        Field authzEventField = RequiredRoleInterceptor.class.getDeclaredField("authzEvent");
        authzEventField.setAccessible(true);
        authzEventField.set(interceptor, eventStub);

        return interceptor;
    }

    // ---- Property Tests ----

    // Feature: security-enhancements, Property 16: RequiredRoleInterceptor permite se o usuário possui pelo menos uma role exigida
    /**
     * Property 16: For any set of required roles via @RequiredRole and any authenticated user,
     * the RequiredRoleInterceptor must allow execution if the user has at least one of the roles.
     * If not, reject with 403. If not authenticated, reject with 401.
     *
     * <p><b>Validates: Requirements 8.4, 8.5, 8.6</b></p>
     *
     * The annotation on the holder method requires {"admin", "manager", "editor"}.
     * We generate random subsets of roles for the user and a random login state,
     * then verify the interceptor behaves correctly in all cases.
     */
    @Property(tries = 100)
    void allowsIffUserHasAtLeastOneRole(
            @ForAll("userRoleSets") Set<String> userRoles,
            @ForAll boolean loggedIn) throws Exception {

        StubSecurityContext secCtx = new StubSecurityContext(loggedIn, userRoles);
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredRoleInterceptor interceptor = createInterceptor(secCtx, eventStub);
        InvocationContext ic = new StubInvocationContext();

        if (!loggedIn) {
            // Not authenticated → must reject with 401
            DemoiselleSecurityException thrown = assertThrows(
                    DemoiselleSecurityException.class,
                    () -> interceptor.manage(ic),
                    "Interceptor must reject unauthenticated user with 401. loggedIn=" + loggedIn);
            assertEquals(401, thrown.getStatusCode(),
                    "Exception status must be 401 UNAUTHORIZED for unauthenticated user");
        } else {
            // Authenticated — check role intersection
            boolean expectedAllow = false;
            for (String required : ANNOTATED_ROLES) {
                if (userRoles.contains(required)) {
                    expectedAllow = true;
                    break;
                }
            }

            if (expectedAllow) {
                // Should allow — proceed() returns "proceeded"
                Object result = interceptor.manage(ic);
                assertEquals("proceeded", result,
                        "Interceptor must allow when user has at least one required role. User roles: " + userRoles);
            } else {
                // Should reject with 403
                DemoiselleSecurityException thrown = assertThrows(
                        DemoiselleSecurityException.class,
                        () -> interceptor.manage(ic),
                        "Interceptor must reject when user has none of the required roles. User roles: " + userRoles);
                assertEquals(403, thrown.getStatusCode(),
                        "Exception status must be 403 FORBIDDEN");
            }
        }
    }

    /**
     * Generates random sets of roles. The pool includes the annotated roles plus
     * some extra roles that are NOT in the annotation, ensuring we test both
     * matching and non-matching scenarios.
     */
    @Provide
    Arbitrary<Set<String>> userRoleSets() {
        // Pool: the 3 annotated roles + 3 non-matching roles
        Arbitrary<String> rolePool = Arbitraries.of(
                "admin", "manager", "editor",  // matching
                "viewer", "guest", "auditor"   // non-matching
        );
        return rolePool.set().ofMinSize(0).ofMaxSize(6);
    }
}
