/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.annotation.Permission;
import org.demoiselle.jee.security.annotation.RequiredAllPermissions;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

import jakarta.interceptor.InvocationContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RequiredAllPermissionsInterceptor}.
 *
 * <p>Validates: Requirements 3.3, 3.4, 3.5</p>
 */
class RequiredAllPermissionsInterceptorTest {

    private RequiredAllPermissionsInterceptor interceptor;
    private StubSecurityContext securityContext;

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new RequiredAllPermissionsInterceptor();
        securityContext = new StubSecurityContext();

        Field scField = RequiredAllPermissionsInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, securityContext);

        Field bundleField = RequiredAllPermissionsInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, new StubMessages());
    }

    // ---- Annotated holder classes for real annotation resolution ----

    /** Method with a single permission. */
    static class SinglePermMethod {
        @RequiredAllPermissions(@Permission(resource = "orders", operation = "read"))
        void target() {}
    }

    /** Method with two permissions. */
    static class TwoPermsMethod {
        @RequiredAllPermissions({
            @Permission(resource = "orders", operation = "read"),
            @Permission(resource = "orders", operation = "write")
        })
        void target() {}
    }

    /** Method with empty permissions array. */
    static class EmptyPermsMethod {
        @RequiredAllPermissions({})
        void target() {}
    }

    /** Method without annotation; class has annotation (fallback). */
    @RequiredAllPermissions(@Permission(resource = "classRes", operation = "classOp"))
    static class ClassAnnotatedHolder {
        void target() {}
    }

    /** Method without annotation and class without annotation (unannotated). */
    static class UnannotatedMethod {
        void target() {}
    }

    // ---- InvocationContext factory ----

    private InvocationContext ic(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            return new StubInvocationContext(target, method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    // --- Requirement 3.5: Unauthenticated user receives 401 ---

    @Test
    void shouldThrow401WhenUserNotAuthenticated() {
        securityContext.loggedIn = false;
        InvocationContext ic = ic(new SinglePermMethod(), "target");

        DemoiselleSecurityException ex = assertThrows(
            DemoiselleSecurityException.class,
            () -> interceptor.manage(ic));

        assertEquals(401, ex.getStatusCode());
    }

    // --- Requirement 3.3: User with all permissions is allowed ---

    @Test
    void shouldProceedWhenUserHasSingleRequiredPermission() throws Exception {
        securityContext.loggedIn = true;
        securityContext.grantedPermissions = Set.of("orders:read");

        Object result = interceptor.manage(ic(new SinglePermMethod(), "target"));
        assertEquals("proceeded", result);
    }

    @Test
    void shouldProceedWhenUserHasAllRequiredPermissions() throws Exception {
        securityContext.loggedIn = true;
        securityContext.grantedPermissions = Set.of("orders:read", "orders:write");

        Object result = interceptor.manage(ic(new TwoPermsMethod(), "target"));
        assertEquals("proceeded", result);
    }

    // --- Requirement 3.4: User missing a permission receives 403 ---

    @Test
    void shouldThrow403WhenUserMissingOneOfTwoPermissions() {
        securityContext.loggedIn = true;
        securityContext.grantedPermissions = Set.of("orders:read");

        DemoiselleSecurityException ex = assertThrows(
            DemoiselleSecurityException.class,
            () -> interceptor.manage(ic(new TwoPermsMethod(), "target")));

        assertEquals(403, ex.getStatusCode());
    }

    @Test
    void shouldThrow403WhenUserHasNoPermissions() {
        securityContext.loggedIn = true;
        securityContext.grantedPermissions = Set.of();

        DemoiselleSecurityException ex = assertThrows(
            DemoiselleSecurityException.class,
            () -> interceptor.manage(ic(new SinglePermMethod(), "target")));

        assertEquals(403, ex.getStatusCode());
    }

    // --- Fallback to class-level annotation ---

    @Test
    void shouldFallbackToClassAnnotationWhenMethodHasNone() throws Exception {
        securityContext.loggedIn = true;
        securityContext.grantedPermissions = Set.of("classRes:classOp");

        Object result = interceptor.manage(ic(new ClassAnnotatedHolder(), "target"));
        assertEquals("proceeded", result);
    }

    // --- Empty permissions array ---

    @Test
    void shouldProceedWithEmptyPermissionsArray() throws Exception {
        securityContext.loggedIn = true;

        Object result = interceptor.manage(ic(new EmptyPermsMethod(), "target"));
        assertEquals("proceeded", result);
    }

    // ---- Stubs ----

    static class StubSecurityContext implements SecurityContext {
        boolean loggedIn = true;
        Set<String> grantedPermissions = Set.of();

        @Override public boolean isLoggedIn() { return loggedIn; }
        @Override public boolean hasPermission(String resource, String operation) {
            return grantedPermissions.contains(resource + ":" + operation);
        }
        @Override public boolean hasRole(String role) { return false; }
        @Override public DemoiselleUser getUser() { return null; }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return null; }
        @Override public void setUser(DemoiselleUser loggedUser) {}
        @Override public void setUser(DemoiselleUser loggedUser, String issuer, String audience) {}
        @Override public void removeUser(DemoiselleUser loggedUser) {}
    }

    static class StubMessages implements DemoiselleSecurityMessages {
        @Override public String accessCheckingPermission(String op, String res) { return "checking"; }
        @Override public String accessDenied(String user, String op, String res) { return "denied"; }
        @Override public String userNotAuthenticated() { return "User not authenticated"; }
        @Override public String invalidCredentials() { return "invalid"; }
        @Override public String doesNotHaveRole(String role) { return "no role"; }
        @Override public String doesNotHavePermission(String op, String res) { return "no perm"; }
        @Override public String cloneError() { return "clone error"; }
    }

    static class StubInvocationContext implements InvocationContext {
        private final Object target;
        private final Method method;

        StubInvocationContext(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

        @Override public Object getTarget() { return target; }
        @Override public Method getMethod() { return method; }
        @Override public Object getTimer() { return null; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() { return "proceeded"; }
    }
}
