/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.annotation.Authenticated;
import org.demoiselle.jee.security.annotation.RequiredPermission;
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
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RequiredPermissionInterceptor}.
 *
 * <p>Validates: Requirements 8.7, 8.8, 8.9</p>
 */
class RequiredPermissionInterceptorTest {

    // ---- Stubs ----

    /** SecurityContext stub with configurable login state and permission. */
    static class StubSecurityContext implements SecurityContext {
        private final boolean loggedIn;
        private final String allowedResource;
        private final String allowedOperation;

        StubSecurityContext(boolean loggedIn, String allowedResource, String allowedOperation) {
            this.loggedIn = loggedIn;
            this.allowedResource = allowedResource;
            this.allowedOperation = allowedOperation;
        }

        @Override public boolean isLoggedIn() { return loggedIn; }
        @Override public boolean hasPermission(String resource, String operation) {
            return resource.equals(allowedResource) && operation.equals(allowedOperation);
        }
        @Override public boolean hasRole(String role) { return false; }
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
        @Override public String doesNotHavePermission(String operacao, String recurso) { return "no permission: " + operacao + " on " + recurso; }
        @Override public String cloneError() { return "clone error"; }
    }

    // ---- Annotated method holders ----

    /** Holder with @RequiredPermission on the method. */
    static class MethodPermissionHolder {
        @RequiredPermission(resource = "document", operation = "read")
        public void targetMethod() {}
    }

    /** Holder with @RequiredPermission and @Authenticated(enable = false) on the method. */
    static class DisabledAuthPermissionHolder {
        @RequiredPermission(resource = "document", operation = "read")
        @Authenticated(enable = false)
        public void targetMethod() {}
    }

    /** Holder with @RequiredPermission at class level and NO annotation on the method. */
    @RequiredPermission(resource = "report", operation = "export")
    static class ClassPermissionHolder {
        public void targetMethod() {}
    }

    // ---- InvocationContext stubs ----

    /** InvocationContext for a method WITH @RequiredPermission annotation. */
    static class MethodPermissionInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;
        static {
            try {
                TARGET_METHOD = MethodPermissionHolder.class.getMethod("targetMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new MethodPermissionHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
    }

    /** InvocationContext for a method WITH @Authenticated(enable = false). */
    static class DisabledAuthInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;
        static {
            try {
                TARGET_METHOD = DisabledAuthPermissionHolder.class.getMethod("targetMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new DisabledAuthPermissionHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "bypassed"; }
    }

    /** InvocationContext for a method WITHOUT @RequiredPermission — falls back to class-level. */
    static class ClassPermissionInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;
        static {
            try {
                TARGET_METHOD = ClassPermissionHolder.class.getMethod("targetMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new ClassPermissionHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "class-proceeded"; }
    }

    // ---- Helper ----

    private RequiredPermissionInterceptor createInterceptor(
            SecurityContext secCtx, StubAuthzEvent eventStub) throws Exception {
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

    // ---- Tests ----

    /**
     * Validates: Requirement 8.7
     * When the user has the required permission (resource + operation), the intercepted method executes normally.
     */
    @Test
    void manage_whenUserHasRequiredPermission_proceedsSuccessfully() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, "document", "read");
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredPermissionInterceptor interceptor = createInterceptor(ctx, eventStub);

        Object result = interceptor.manage(new MethodPermissionInvocationContext());

        assertEquals("proceeded", result, "Method should proceed when user has the required permission");
        assertTrue(eventStub.fired.isEmpty(), "No authorization event should be fired on success");
    }

    /**
     * Validates: Requirement 8.8
     * When the user does not have the required permission, DemoiselleSecurityException with status 403 is thrown.
     */
    @Test
    void manage_whenUserLacksPermission_throwsForbidden403() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, "other", "write");
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredPermissionInterceptor interceptor = createInterceptor(ctx, eventStub);

        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new MethodPermissionInvocationContext()));

        assertEquals(403, thrown.getStatusCode(), "Status code must be 403 FORBIDDEN");
        assertEquals(1, eventStub.fired.size(), "One authorization event should be fired on permission denial");
    }

    /**
     * Validates: Requirement 8.8
     * When the user is not authenticated, DemoiselleSecurityException with status 403 is thrown.
     */
    @Test
    void manage_whenUserNotAuthenticated_throwsForbidden403() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(false, "", "");
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredPermissionInterceptor interceptor = createInterceptor(ctx, eventStub);

        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new MethodPermissionInvocationContext()));

        assertEquals(403, thrown.getStatusCode(), "Status code must be 403 FORBIDDEN for unauthenticated user");
        assertTrue(eventStub.fired.isEmpty(), "No authorization event should be fired for unauthenticated user");
    }

    /**
     * Validates: Requirement 8.9
     * When @Authenticated(enable = false) is present, the method executes regardless of login state.
     */
    @Test
    void manage_whenAuthDisabled_bypassesEvenIfNotLoggedIn() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(false, "", "");
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredPermissionInterceptor interceptor = createInterceptor(ctx, eventStub);

        Object result = interceptor.manage(new DisabledAuthInvocationContext());

        assertEquals("bypassed", result, "Method should proceed when @Authenticated(enable=false)");
        assertTrue(eventStub.fired.isEmpty(), "No event should be fired when auth is disabled");
    }

    /**
     * Validates: Requirement 8.9
     * When @Authenticated(enable = false) is present and user IS logged in,
     * the method still executes normally (bypass is unconditional).
     */
    @Test
    void manage_whenAuthDisabledAndLoggedIn_proceedsNormally() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, "document", "read");
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredPermissionInterceptor interceptor = createInterceptor(ctx, eventStub);

        Object result = interceptor.manage(new DisabledAuthInvocationContext());

        assertEquals("bypassed", result, "Method should proceed when @Authenticated(enable=false)");
        assertTrue(eventStub.fired.isEmpty(), "No event should be fired when auth is disabled");
    }

    /**
     * Validates: Requirement 8.7
     * When @RequiredPermission is on class level and user has the permission, method proceeds.
     */
    @Test
    void manage_whenAnnotationOnClassOnly_andUserHasPermission_proceeds() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, "report", "export");
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredPermissionInterceptor interceptor = createInterceptor(ctx, eventStub);

        Object result = interceptor.manage(new ClassPermissionInvocationContext());

        assertEquals("class-proceeded", result, "Method should proceed when user has class-level required permission");
        assertTrue(eventStub.fired.isEmpty(), "No authorization event should be fired on success");
    }

    /**
     * Validates: Requirement 8.8
     * When @RequiredPermission is on class level and user lacks the permission, 403 is thrown.
     */
    @Test
    void manage_whenAnnotationOnClassOnly_andUserLacksPermission_throwsForbidden403() throws Exception {
        StubSecurityContext ctx = new StubSecurityContext(true, "other", "other");
        StubAuthzEvent eventStub = new StubAuthzEvent();
        RequiredPermissionInterceptor interceptor = createInterceptor(ctx, eventStub);

        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new ClassPermissionInvocationContext()));

        assertEquals(403, thrown.getStatusCode(), "Status code must be 403 FORBIDDEN for class-level permission check");
        assertEquals(1, eventStub.fired.size(), "One authorization event should be fired on permission denial");
    }
}
