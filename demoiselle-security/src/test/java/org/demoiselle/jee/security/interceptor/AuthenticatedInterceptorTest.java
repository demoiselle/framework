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
import org.demoiselle.jee.security.event.AuthenticationEvent;
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
 * Unit tests for {@link AuthenticatedInterceptor}.
 *
 * <p>Validates: Requirements 8.1, 8.2, 8.3, 8.9</p>
 */
class AuthenticatedInterceptorTest {

    // ---- Stubs ----

    /** SecurityContext stub with configurable isLoggedIn(). */
    static class StubSecurityContext implements SecurityContext {
        private final boolean loggedIn;

        StubSecurityContext(boolean loggedIn) {
            this.loggedIn = loggedIn;
        }

        @Override public boolean isLoggedIn() { return loggedIn; }
        @Override public boolean hasPermission(String resource, String operation) { return false; }
        @Override public boolean hasRole(String role) { return false; }
        @Override public DemoiselleUser getUser() { return null; }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return null; }
        @Override public void setUser(DemoiselleUser loggedUser) {}
        @Override public void setUser(DemoiselleUser loggedUser, String issuer, String audience) {}
        @Override public void removeUser(DemoiselleUser loggedUser) {}
    }

    /** Event stub that captures fired AuthenticationEvents. */
    static class StubAuthEvent implements Event<AuthenticationEvent> {
        final List<AuthenticationEvent> fired = new ArrayList<>();

        @Override public void fire(AuthenticationEvent event) { fired.add(event); }
        @Override public <U extends AuthenticationEvent> CompletionStage<U> fireAsync(U event) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthenticationEvent> CompletionStage<U> fireAsync(U event, NotificationOptions options) { throw new UnsupportedOperationException(); }
        @Override public Event<AuthenticationEvent> select(Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthenticationEvent> Event<U> select(Class<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
        @Override public <U extends AuthenticationEvent> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) { throw new UnsupportedOperationException(); }
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

    /** Holder with a plain method (no @Authenticated annotation on method). */
    static class PlainMethodHolder {
        public void targetMethod() {}
    }

    /** Holder with @Authenticated(enable = false) on the method. */
    static class DisabledAuthHolder {
        @Authenticated(enable = false)
        public void targetMethod() {}
    }

    // ---- InvocationContext stubs ----

    /** InvocationContext for a method WITHOUT @Authenticated annotation. */
    static class PlainInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;
        static {
            try {
                TARGET_METHOD = PlainMethodHolder.class.getMethod("targetMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new PlainMethodHolder(); }
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
                TARGET_METHOD = DisabledAuthHolder.class.getMethod("targetMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new DisabledAuthHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "bypassed"; }
    }

    // ---- Helper ----

    private AuthenticatedInterceptor createInterceptor(
            SecurityContext secCtx, StubAuthEvent eventStub) throws Exception {
        AuthenticatedInterceptor interceptor = new AuthenticatedInterceptor();

        Field scField = AuthenticatedInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = AuthenticatedInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, new StubMessages());

        Field authEventField = AuthenticatedInterceptor.class.getDeclaredField("authEvent");
        authEventField.setAccessible(true);
        authEventField.set(interceptor, eventStub);

        return interceptor;
    }

    // ---- Tests ----

    /**
     * Validates: Requirement 8.1
     * When isLoggedIn() returns true, the intercepted method executes normally.
     */
    @Test
    void manage_whenLoggedIn_proceedsSuccessfully() throws Exception {
        StubSecurityContext loggedIn = new StubSecurityContext(true);
        StubAuthEvent eventStub = new StubAuthEvent();
        AuthenticatedInterceptor interceptor = createInterceptor(loggedIn, eventStub);

        Object result = interceptor.manage(new PlainInvocationContext());

        assertEquals("proceeded", result, "Method should proceed when user is logged in");
        assertTrue(eventStub.fired.isEmpty(), "No authentication event should be fired on success");
    }

    /**
     * Validates: Requirement 8.2
     * When isLoggedIn() returns false, DemoiselleSecurityException with status 403 is thrown.
     */
    @Test
    void manage_whenNotLoggedIn_throwsForbidden403() throws Exception {
        StubSecurityContext notLoggedIn = new StubSecurityContext(false);
        StubAuthEvent eventStub = new StubAuthEvent();
        AuthenticatedInterceptor interceptor = createInterceptor(notLoggedIn, eventStub);

        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(new PlainInvocationContext()));

        assertEquals(403, thrown.getStatusCode(), "Status code must be 403 FORBIDDEN");
        assertEquals(1, eventStub.fired.size(), "One FAILURE event should be fired");
        assertEquals(AuthenticationEvent.Action.FAILURE, eventStub.fired.get(0).action());
    }

    /**
     * Validates: Requirement 8.3
     * When @Authenticated(enable = false) is present, the method executes
     * regardless of login state.
     */
    @Test
    void manage_whenAuthDisabled_bypassesEvenIfNotLoggedIn() throws Exception {
        StubSecurityContext notLoggedIn = new StubSecurityContext(false);
        StubAuthEvent eventStub = new StubAuthEvent();
        AuthenticatedInterceptor interceptor = createInterceptor(notLoggedIn, eventStub);

        Object result = interceptor.manage(new DisabledAuthInvocationContext());

        assertEquals("bypassed", result, "Method should proceed when @Authenticated(enable=false)");
        assertTrue(eventStub.fired.isEmpty(), "No event should be fired when auth is disabled");
    }

    /**
     * Validates: Requirement 8.3
     * When @Authenticated(enable = false) is present and user IS logged in,
     * the method still executes normally (bypass is unconditional).
     */
    @Test
    void manage_whenAuthDisabledAndLoggedIn_proceedsNormally() throws Exception {
        StubSecurityContext loggedIn = new StubSecurityContext(true);
        StubAuthEvent eventStub = new StubAuthEvent();
        AuthenticatedInterceptor interceptor = createInterceptor(loggedIn, eventStub);

        Object result = interceptor.manage(new DisabledAuthInvocationContext());

        assertEquals("bypassed", result, "Method should proceed when @Authenticated(enable=false)");
        assertTrue(eventStub.fired.isEmpty(), "No event should be fired when auth is disabled");
    }
}
