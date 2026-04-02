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
import org.demoiselle.jee.security.event.AuthenticationEvent;
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
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 3: Evento de falha de autenticação
/**
 * Property-based tests for authentication failure events fired by
 * {@link AuthenticatedInterceptor}.
 *
 * <p><b>Validates: Requirements 2.3</b></p>
 */
class AuthenticatedInterceptorPropertyTest {

    // ---- Stubs ----

    /**
     * A SecurityContext stub that always returns a fixed isLoggedIn value.
     */
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

    /**
     * A simple Event stub that captures all fired AuthenticationEvents.
     */
    static class StubAuthEvent implements Event<AuthenticationEvent> {
        final List<AuthenticationEvent> fired = new ArrayList<>();

        @Override
        public void fire(AuthenticationEvent event) {
            fired.add(event);
        }

        @Override
        public <U extends AuthenticationEvent> CompletionStage<U> fireAsync(U event) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <U extends AuthenticationEvent> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Event<AuthenticationEvent> select(Annotation... qualifiers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <U extends AuthenticationEvent> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <U extends AuthenticationEvent> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A simple DemoiselleSecurityMessages stub that returns plain strings.
     */
    static class StubMessages implements DemoiselleSecurityMessages {
        @Override public String accessCheckingPermission(String operacao, String recurso) { return "checking"; }
        @Override public String accessDenied(String usuario, String operacao, String recurso) { return "denied"; }
        @Override public String userNotAuthenticated() { return "User not authenticated"; }
        @Override public String invalidCredentials() { return "invalid"; }
        @Override public String doesNotHaveRole(String role) { return "no role"; }
        @Override public String doesNotHavePermission(String operacao, String recurso) { return "no permission"; }
        @Override public String cloneError() { return "clone error"; }
    }

    /**
     * A minimal InvocationContext stub whose getMethod() returns a method
     * without the @Authenticated annotation (so the interceptor won't bypass).
     */
    static class StubInvocationContext implements InvocationContext {
        private static final Method DUMMY_METHOD;

        static {
            try {
                DUMMY_METHOD = StubInvocationContext.class.getDeclaredMethod("dummyTarget");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        /** A plain method with no @Authenticated annotation. */
        @SuppressWarnings("unused")
        private void dummyTarget() {}

        @Override public Object getTarget() { return null; }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return DUMMY_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
    }

    // ---- Helper ----

    private AuthenticatedInterceptor createInterceptor(
            SecurityContext secCtx,
            DemoiselleSecurityMessages msgs,
            StubAuthEvent eventStub) throws Exception {

        AuthenticatedInterceptor interceptor = new AuthenticatedInterceptor();

        Field scField = AuthenticatedInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = AuthenticatedInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, msgs);

        Field authEventField = AuthenticatedInterceptor.class.getDeclaredField("authEvent");
        authEventField.setAccessible(true);
        authEventField.set(interceptor, eventStub);

        return interceptor;
    }

    // ---- Property Test ----

    // Feature: security-enhancements, Property 3: Evento de falha de autenticação
    /**
     * Property 3: For any invocation intercepted by AuthenticatedInterceptor
     * where SecurityContext.isLoggedIn() returns false, the interceptor must
     * fire an AuthenticationEvent with action FAILURE before throwing the exception.
     *
     * <p><b>Validates: Requirements 2.3</b></p>
     *
     * We use an arbitrary seed string to ensure the property holds across many
     * independent invocations (each iteration creates a fresh interceptor).
     */
    @Property(tries = 100)
    void failedAuthFiresFailureEvent(@ForAll("seeds") String seed) throws Exception {
        // SecurityContext that always returns false for isLoggedIn
        StubSecurityContext notLoggedIn = new StubSecurityContext(false);
        StubMessages messages = new StubMessages();
        StubAuthEvent eventStub = new StubAuthEvent();

        AuthenticatedInterceptor interceptor = createInterceptor(notLoggedIn, messages, eventStub);
        InvocationContext ic = new StubInvocationContext();

        // The interceptor must throw DemoiselleSecurityException
        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(ic));

        assertEquals(403, thrown.getStatusCode(),
                "Exception status must be 403 FORBIDDEN");

        // The interceptor must have fired exactly one FAILURE event before throwing
        assertEquals(1, eventStub.fired.size(),
                "Exactly one AuthenticationEvent must be fired on auth failure");

        AuthenticationEvent event = eventStub.fired.get(0);
        assertEquals(AuthenticationEvent.Action.FAILURE, event.action(),
                "The fired event must have action FAILURE");
        assertNull(event.user(),
                "The fired event user must be null (no authenticated user)");
        assertNotNull(event.timestamp(),
                "The fired event must have a non-null timestamp");
    }

    @Provide
    Arbitrary<String> seeds() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }
}
