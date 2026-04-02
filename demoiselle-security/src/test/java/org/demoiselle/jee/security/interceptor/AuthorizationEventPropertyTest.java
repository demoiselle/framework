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
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 4: Eventos de falha de autorização
/**
 * Property-based tests for authorization failure events fired by
 * {@link RequiredRoleInterceptor} and {@link RequiredPermissionInterceptor}.
 *
 * <p><b>Validates: Requirements 2.4, 2.5</b></p>
 */
class AuthorizationEventPropertyTest {

    // ---- Stubs ----

    /**
     * A SecurityContext stub that is logged in but denies all roles/permissions.
     */
    static class StubSecurityContext implements SecurityContext {
        private final DemoiselleUser user;

        StubSecurityContext(DemoiselleUser user) {
            this.user = user;
        }

        @Override public boolean isLoggedIn() { return true; }
        @Override public boolean hasPermission(String resource, String operation) { return false; }
        @Override public boolean hasRole(String role) { return false; }
        @Override public DemoiselleUser getUser() { return user; }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return user; }
        @Override public void setUser(DemoiselleUser loggedUser) {}
        @Override public void setUser(DemoiselleUser loggedUser, String issuer, String audience) {}
        @Override public void removeUser(DemoiselleUser loggedUser) {}
    }

    /**
     * A minimal DemoiselleUser stub with a fixed identity and name.
     */
    static class StubUser implements DemoiselleUser {
        private final String identity;
        private final String name;

        StubUser(String identity, String name) {
            this.identity = identity;
            this.name = name;
        }

        @Override public String getIdentity() { return identity; }
        @Override public void setIdentity(String id) {}
        @Override public String getName() { return name; }
        @Override public void setName(String name) {}
        @Override public void addRole(String role) {}
        @Override public void removeRole(String role) {}
        @Override public List<String> getRoles() { return List.of(); }
        @Override public Map<String, List<String>> getPermissions() { return Map.of(); }
        @Override public List<String> getPermissions(String resource) { return List.of(); }
        @Override public void addPermission(String resource, String operation) {}
        @Override public void removePermission(String resource, String operation) {}
        @Override public Map<String, String> getParams() { return Map.of(); }
        @Override public String getParams(String key) { return null; }
        @Override public void addParam(String key, String value) {}
        @Override public void removeParam(String key) {}
        @Override public DemoiselleUser clone() { return new StubUser(identity, name); }
    }

    /**
     * A simple Event stub that captures all fired AuthorizationEvents.
     */
    static class StubAuthzEvent implements Event<AuthorizationEvent> {
        final List<AuthorizationEvent> fired = new ArrayList<>();

        @Override public void fire(AuthorizationEvent event) { fired.add(event); }
        @Override public <U extends AuthorizationEvent> CompletionStage<U> fireAsync(U event) {
            throw new UnsupportedOperationException();
        }
        @Override public <U extends AuthorizationEvent> CompletionStage<U> fireAsync(U event, NotificationOptions opts) {
            throw new UnsupportedOperationException();
        }
        @Override public Event<AuthorizationEvent> select(Annotation... qualifiers) {
            throw new UnsupportedOperationException();
        }
        @Override public <U extends AuthorizationEvent> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
            throw new UnsupportedOperationException();
        }
        @Override public <U extends AuthorizationEvent> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A simple DemoiselleSecurityMessages stub.
     */
    static class StubMessages implements DemoiselleSecurityMessages {
        @Override public String accessCheckingPermission(String op, String res) { return "checking"; }
        @Override public String accessDenied(String user, String op, String res) { return "denied"; }
        @Override public String userNotAuthenticated() { return "User not authenticated"; }
        @Override public String invalidCredentials() { return "invalid"; }
        @Override public String doesNotHaveRole(String role) { return "no role: " + role; }
        @Override public String doesNotHavePermission(String op, String res) { return "no perm"; }
        @Override public String cloneError() { return "clone error"; }
    }

    // ---- Annotated target methods ----

    @SuppressWarnings("unused")
    static class RoleTarget {
        @RequiredRole(value = {"admin", "manager", "editor"})
        public void protectedMethod() {}
    }

    @SuppressWarnings("unused")
    static class PermissionTarget {
        @RequiredPermission(resource = "document", operation = "delete")
        public void protectedMethod() {}
    }

    // ---- InvocationContext stubs ----

    static class RoleInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;

        static {
            try {
                TARGET_METHOD = RoleTarget.class.getDeclaredMethod("protectedMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return null; }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
    }

    static class PermissionInvocationContext implements InvocationContext {
        private static final Method TARGET_METHOD;

        static {
            try {
                TARGET_METHOD = PermissionTarget.class.getDeclaredMethod("protectedMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return null; }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return TARGET_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
    }

    // ---- Helpers ----

    private RequiredRoleInterceptor createRoleInterceptor(
            SecurityContext secCtx,
            DemoiselleSecurityMessages msgs,
            StubAuthzEvent eventStub) throws Exception {

        RequiredRoleInterceptor interceptor = new RequiredRoleInterceptor();

        Field scField = RequiredRoleInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = RequiredRoleInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, msgs);

        Field eventField = RequiredRoleInterceptor.class.getDeclaredField("authzEvent");
        eventField.setAccessible(true);
        eventField.set(interceptor, eventStub);

        return interceptor;
    }

    private RequiredPermissionInterceptor createPermissionInterceptor(
            SecurityContext secCtx,
            DemoiselleSecurityMessages msgs,
            StubAuthzEvent eventStub) throws Exception {

        RequiredPermissionInterceptor interceptor = new RequiredPermissionInterceptor();

        Field scField = RequiredPermissionInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = RequiredPermissionInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, msgs);

        Field eventField = RequiredPermissionInterceptor.class.getDeclaredField("authzEvent");
        eventField.setAccessible(true);
        eventField.set(interceptor, eventStub);

        return interceptor;
    }

    // ---- Property Tests ----

    // Feature: security-enhancements, Property 4: Eventos de falha de autorização
    /**
     * For any authorization failure in RequiredRoleInterceptor, the interceptor
     * must fire an AuthorizationEvent containing the required roles and the
     * user's data before throwing the exception.
     *
     * <p><b>Validates: Requirements 2.4</b></p>
     *
     * We vary the user identity and name across iterations to ensure the
     * property holds regardless of user data.
     */
    @Property(tries = 100)
    void roleAuthorizationFailureFiresEvent(
            @ForAll("userIdentities") String userId,
            @ForAll("userNames") String userName) throws Exception {

        StubUser user = new StubUser(userId, userName);
        StubSecurityContext secCtx = new StubSecurityContext(user);
        StubMessages messages = new StubMessages();
        StubAuthzEvent eventStub = new StubAuthzEvent();

        RequiredRoleInterceptor interceptor = createRoleInterceptor(secCtx, messages, eventStub);
        InvocationContext ic = new RoleInvocationContext();

        // The interceptor must throw DemoiselleSecurityException
        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(ic));

        assertEquals(403, thrown.getStatusCode(),
                "Exception status must be 403 FORBIDDEN");

        // The interceptor must have fired exactly one AuthorizationEvent
        assertEquals(1, eventStub.fired.size(),
                "Exactly one AuthorizationEvent must be fired on role auth failure");

        AuthorizationEvent event = eventStub.fired.get(0);
        assertSame(user, event.user(),
                "The fired event must contain the current user");
        assertEquals(List.of("admin", "manager", "editor"), event.requiredRoles(),
                "The fired event must contain the required roles from the annotation");
        assertNull(event.resource(),
                "Resource must be null for role-based authorization failures");
        assertNull(event.operation(),
                "Operation must be null for role-based authorization failures");
        assertNotNull(event.timestamp(),
                "The fired event must have a non-null timestamp");
    }

    // Feature: security-enhancements, Property 4: Eventos de falha de autorização
    /**
     * For any authorization failure in RequiredPermissionInterceptor, the interceptor
     * must fire an AuthorizationEvent containing the resource, operation, and the
     * user's data before throwing the exception.
     *
     * <p><b>Validates: Requirements 2.5</b></p>
     *
     * We vary the user identity and name across iterations to ensure the
     * property holds regardless of user data.
     */
    @Property(tries = 100)
    void permissionAuthorizationFailureFiresEvent(
            @ForAll("userIdentities") String userId,
            @ForAll("userNames") String userName) throws Exception {

        StubUser user = new StubUser(userId, userName);
        StubSecurityContext secCtx = new StubSecurityContext(user);
        StubMessages messages = new StubMessages();
        StubAuthzEvent eventStub = new StubAuthzEvent();

        RequiredPermissionInterceptor interceptor = createPermissionInterceptor(secCtx, messages, eventStub);
        InvocationContext ic = new PermissionInvocationContext();

        // The interceptor must throw DemoiselleSecurityException
        DemoiselleSecurityException thrown = assertThrows(
                DemoiselleSecurityException.class,
                () -> interceptor.manage(ic));

        assertEquals(403, thrown.getStatusCode(),
                "Exception status must be 403 FORBIDDEN");

        // The interceptor must have fired exactly one AuthorizationEvent
        assertEquals(1, eventStub.fired.size(),
                "Exactly one AuthorizationEvent must be fired on permission auth failure");

        AuthorizationEvent event = eventStub.fired.get(0);
        assertSame(user, event.user(),
                "The fired event must contain the current user");
        assertEquals("document", event.resource(),
                "The fired event must contain the resource from the annotation");
        assertEquals("delete", event.operation(),
                "The fired event must contain the operation from the annotation");
        assertEquals(List.of(), event.requiredRoles(),
                "Required roles must be empty for permission-based authorization failures");
        assertNotNull(event.timestamp(),
                "The fired event must have a non-null timestamp");
    }

    // ---- Arbitraries ----

    @Provide
    Arbitrary<String> userIdentities() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> userNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }
}
