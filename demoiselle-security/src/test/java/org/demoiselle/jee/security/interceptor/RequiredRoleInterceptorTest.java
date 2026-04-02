/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

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

import org.junit.jupiter.api.Test;

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

/**
 * Unit tests for {@link RequiredRoleInterceptor}.
 *
 * <p>Validates: Requirements 8.4, 8.5, 8.6, 8.9, 8.10</p>
 */
class RequiredRoleInterceptorTest {

    // ---- Stubs ----

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

    /** DemoiselleSecurityMessages stub returning plain strings. */
    static class StubMessages implements DemoiselleSecurityMessages {
        @Override public String accessCheckingPermission(String operacao, String recurso) { return "checking"; }
        @Override public String accessDenied(String usuario, String operacao, String recurso) { return "denied"; }
        @Override public String userNotAuthenticated() { return "User not authenticated"; }
        @Override public String invalidCredentials() { return "invalid"; }
        @Override public String doesNotHaveRole(String role) { return "no role: " + role; }
        @Override public String doesNotHavePermission(String operacao, String recurso) { return "no permission"; }
        @Override public String cloneError() { return "clone error"; }
    }

    // ---- Annotated method holders ----

    /** Holder with @RequiredRole on the method. */
    static class MethodRoleHolder {
        @RequiredRole({"admin", "manager"})
        public void targetMethod() {}
    }

    /** Holder with @RequiredRole at class level and NO annotation on the method. */
    @RequiredRole({"editor", "reviewer"})
    static class ClassRoleHolder {
        public void targetMethod() {}
    }

    // ---- InvocationContext stubs ----

    /** InvocationContext for a method WITH @RequiredRole annotation. */
    static class MethodRoleInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;
        static {
            try {
                TARGET_METHOD = MethodRoleHolder.class.getMethod("targetMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new MethodRoleHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
    }

    /** InvocationContext for a method WITHOUT @RequiredRole — falls back to class-level. */
    static class ClassRoleInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;
        static {
            try {
                TARGET_METHOD = ClassRoleHolder.class.getMethod("targetMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new ClassRoleHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "class-proceeded"; }
    }

    // ---- Helper ----

    private RequiredRoleInterceptor createInterceptor(
            SecurityContext secCtx, StubAuthzEvent eventStub) throws Exception {
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

    // ---- Tests ----

    /**
     * Validates: Requirement 8.4
     * When the user has one of the required roles, the intercepted method executes normally.
     */
    @Test
    void manage_whenUserHasRequiredRole_proceedsSuccessfully() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, Set.of("admin"));
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredRoleInterceptor interceptor = createInterceptor(ctx, eventStub);

        Object result = interceptor.manage(new MethodRoleInvocationContext());

        assertEquals("proceeded", result, "Method should proceed when user has a required role");
        assertTrue(eventStub.fired.isEmpty(), "No authorization event should be fired on success");
    }

    /**
     * Validates: Requirement 8.5
     * When the user does not have any of the required roles, DemoiselleSecurityException with status 403 is thrown.
     */
    @Test
    void manage_whenUserLacksRequiredRole_throwsForbidden403() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, Set.of("viewer"));
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredRoleInterceptor interceptor = createInterceptor(ctx, eventStub);

        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new MethodRoleInvocationContext()));

        assertEquals(403, thrown.getStatusCode(), "Status code must be 403 FORBIDDEN");
        assertEquals(1, eventStub.fired.size(), "One FAILURE authorization event should be fired");
    }

    /**
     * Validates: Requirement 8.6
     * When the user is not authenticated, DemoiselleSecurityException with status 401 is thrown.
     */
    @Test
    void manage_whenUserNotAuthenticated_throwsUnauthorized401() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(false, Set.of());
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredRoleInterceptor interceptor = createInterceptor(ctx, eventStub);

        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new MethodRoleInvocationContext()));

        assertEquals(401, thrown.getStatusCode(), "Status code must be 401 UNAUTHORIZED");
        assertTrue(eventStub.fired.isEmpty(), "No authorization event should be fired for unauthenticated user");
    }

    /**
     * Validates: Requirement 8.10
     * When @RequiredRole is absent on the method, the interceptor falls back to the class-level annotation.
     */
    @Test
    void manage_whenAnnotationOnClassOnly_readsClassLevelRoles() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, Set.of("editor"));
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredRoleInterceptor interceptor = createInterceptor(ctx, eventStub);

        Object result = interceptor.manage(new ClassRoleInvocationContext());

        assertEquals("class-proceeded", result, "Method should proceed when user has class-level required role");
        assertTrue(eventStub.fired.isEmpty(), "No authorization event should be fired on success");
    }

    /**
     * Validates: Requirement 8.10
     * When @RequiredRole is on class level and user lacks the roles, 403 is thrown.
     */
    @Test
    void manage_whenAnnotationOnClassOnly_andUserLacksRole_throwsForbidden403() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, Set.of("viewer"));
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredRoleInterceptor interceptor = createInterceptor(ctx, eventStub);

        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new ClassRoleInvocationContext()));

        assertEquals(403, thrown.getStatusCode(), "Status code must be 403 FORBIDDEN for class-level role check");
        assertEquals(1, eventStub.fired.size(), "One FAILURE authorization event should be fired");
    }
}
