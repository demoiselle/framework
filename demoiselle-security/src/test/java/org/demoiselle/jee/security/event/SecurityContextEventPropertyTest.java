/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.event;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.impl.SecurityContextImpl;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.util.TypeLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 2: Eventos de autenticação em login e logout
/**
 * Property-based tests for authentication events fired by {@link SecurityContextImpl}.
 *
 * <p><b>Validates: Requirements 2.1, 2.2</b></p>
 */
class SecurityContextEventPropertyTest {

    /**
     * A simple Event stub that captures all fired events into a list.
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
     * A minimal TokenManager stub that records setUser/removeUser calls.
     */
    static class StubTokenManager implements TokenManager {
        DemoiselleUser currentUser;

        @Override public DemoiselleUser getUser() { return currentUser; }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return currentUser; }
        @Override public void setUser(DemoiselleUser user) { this.currentUser = user; }
        @Override public void setUser(DemoiselleUser user, String issuer, String audience) { this.currentUser = user; }
        @Override public void removeUser(DemoiselleUser user) { this.currentUser = null; }
        @Override public boolean validate() { return currentUser != null; }
        @Override public boolean validate(String issuer, String audience) { return currentUser != null; }
    }

    private SecurityContextImpl createContext(StubTokenManager tm, StubAuthEvent eventStub) throws Exception {
        SecurityContextImpl ctx = new SecurityContextImpl();

        Field tmField = SecurityContextImpl.class.getDeclaredField("tm");
        tmField.setAccessible(true);
        tmField.set(ctx, tm);

        Field authEventField = SecurityContextImpl.class.getDeclaredField("authEvent");
        authEventField.setAccessible(true);
        authEventField.set(ctx, eventStub);

        return ctx;
    }

    private DemoiselleUser createUser(String identity, String name) {
        DemoiselleUserImpl user = new DemoiselleUserImpl();
        user.init();
        user.setIdentity(identity);
        user.setName(name);
        return user;
    }

    @Provide
    Arbitrary<String> userIdentities() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    @Provide
    Arbitrary<String> userNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    // Feature: security-enhancements, Property 2: Eventos de autenticação em login e logout
    /**
     * Property 2: For any DemoiselleUser, when SecurityContext.setUser(user) is invoked,
     * the system must fire an AuthenticationEvent with action LOGIN and the user's data.
     * When SecurityContext.removeUser(user) is invoked, it must fire an AuthenticationEvent
     * with action LOGOUT and the user's data.
     *
     * <p><b>Validates: Requirements 2.1, 2.2</b></p>
     */
    @Property(tries = 100)
    void loginLogoutFiresCorrectEvent(
            @ForAll("userIdentities") String identity,
            @ForAll("userNames") String name) throws Exception {

        StubTokenManager tm = new StubTokenManager();
        StubAuthEvent eventStub = new StubAuthEvent();
        SecurityContextImpl ctx = createContext(tm, eventStub);

        DemoiselleUser user = createUser(identity, name);

        // --- LOGIN ---
        ctx.setUser(user);

        assertEquals(1, eventStub.fired.size(),
                "Exactly one event should be fired after setUser");

        AuthenticationEvent loginEvent = eventStub.fired.get(0);
        assertEquals(AuthenticationEvent.Action.LOGIN, loginEvent.action(),
                "setUser must fire a LOGIN event");
        assertSame(user, loginEvent.user(),
                "LOGIN event must carry the same user passed to setUser");
        assertNotNull(loginEvent.timestamp(),
                "LOGIN event must have a non-null timestamp");

        // --- LOGOUT ---
        ctx.removeUser(user);

        assertEquals(2, eventStub.fired.size(),
                "Exactly two events should be fired after setUser + removeUser");

        AuthenticationEvent logoutEvent = eventStub.fired.get(1);
        assertEquals(AuthenticationEvent.Action.LOGOUT, logoutEvent.action(),
                "removeUser must fire a LOGOUT event");
        assertSame(user, logoutEvent.user(),
                "LOGOUT event must carry the same user passed to removeUser");
        assertNotNull(logoutEvent.timestamp(),
                "LOGOUT event must have a non-null timestamp");
    }
}
