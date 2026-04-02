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
import org.demoiselle.jee.security.annotation.Permission;
import org.demoiselle.jee.security.annotation.RequiredAllPermissions;
import org.demoiselle.jee.security.annotation.RequiredAnyRole;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

import jakarta.interceptor.InvocationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 7: Usuário não autenticado recebe 401 nos novos interceptors
/**
 * Property-based tests verifying that unauthenticated users receive HTTP 401
 * from both {@link RequiredAnyRoleInterceptor} and {@link RequiredAllPermissionsInterceptor}.
 *
 * <p><b>Validates: Requirements 3.5</b></p>
 */
class UnauthenticatedInterceptorPropertyTest {

    // ---- Stubs ----

    /**
     * A SecurityContext stub that always returns false for isLoggedIn(),
     * simulating an unauthenticated user.
     */
    static class UnauthenticatedSecurityContext implements SecurityContext {
        @Override public boolean isLoggedIn() { return false; }
        @Override public boolean hasPermission(String resource, String operation) { return false; }
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

    // ---- Annotated holders ----

    static class RoleAnnotatedHolder {
        @RequiredAnyRole({"admin", "manager", "editor"})
        public void protectedMethod() {}
    }

    static class PermissionAnnotatedHolder {
        @RequiredAllPermissions({
            @Permission(resource = "user", operation = "read"),
            @Permission(resource = "report", operation = "export")
        })
        public void protectedMethod() {}
    }

    // ---- InvocationContext stubs ----

    static class RoleInvocationContext implements InvocationContext {
        private static final Method ANNOTATED_METHOD;

        static {
            try {
                ANNOTATED_METHOD = RoleAnnotatedHolder.class.getMethod("protectedMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new RoleAnnotatedHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return ANNOTATED_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
    }

    static class PermissionInvocationContext implements InvocationContext {
        private static final Method ANNOTATED_METHOD;

        static {
            try {
                ANNOTATED_METHOD = PermissionAnnotatedHolder.class.getMethod("protectedMethod");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override public Object getTarget() { return new PermissionAnnotatedHolder(); }
        @Override public Object getTimer() { return null; }
        @Override public Method getMethod() { return ANNOTATED_METHOD; }
        @Override public Constructor<?> getConstructor() { return null; }
        @Override public Object[] getParameters() { return new Object[0]; }
        @Override public void setParameters(Object[] params) {}
        @Override public Map<String, Object> getContextData() { return Map.of(); }
        @Override public Object proceed() throws Exception { return "proceeded"; }
    }

    // ---- Helpers ----

    private RequiredAnyRoleInterceptor createRoleInterceptor(SecurityContext secCtx) throws Exception {
        RequiredAnyRoleInterceptor interceptor = new RequiredAnyRoleInterceptor();

        Field scField = RequiredAnyRoleInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = RequiredAnyRoleInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, new StubMessages());

        return interceptor;
    }

    private RequiredAllPermissionsInterceptor createPermissionsInterceptor(SecurityContext secCtx) throws Exception {
        RequiredAllPermissionsInterceptor interceptor = new RequiredAllPermissionsInterceptor();

        Field scField = RequiredAllPermissionsInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = RequiredAllPermissionsInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, new StubMessages());

        return interceptor;
    }

    // ---- Property Test ----

    // Feature: security-enhancements, Property 7: Usuário não autenticado recebe 401 nos novos interceptors
    /**
     * Property 7: For any method annotated with @RequiredAnyRole or @RequiredAllPermissions,
     * when the user is not authenticated (isLoggedIn() == false), the corresponding interceptor
     * must reject the request with HTTP 401 (Unauthorized).
     *
     * <p><b>Validates: Requirements 3.5</b></p>
     *
     * We use jqwik to vary an arbitrary seed string across many iterations to ensure
     * the property holds consistently regardless of any external factor.
     */
    @Property(tries = 100)
    void unauthenticatedGets401(@ForAll("seedStrings") String seed) throws Exception {
        UnauthenticatedSecurityContext unauthCtx = new UnauthenticatedSecurityContext();

        // Test RequiredAnyRoleInterceptor
        RequiredAnyRoleInterceptor roleInterceptor = createRoleInterceptor(unauthCtx);
        InvocationContext roleIc = new RoleInvocationContext();

        DemoiselleSecurityException roleEx = assertThrows(
                DemoiselleSecurityException.class,
                () -> roleInterceptor.manage(roleIc),
                "RequiredAnyRoleInterceptor must reject unauthenticated user (seed: " + seed + ")");
        assertEquals(401, roleEx.getStatusCode(),
                "RequiredAnyRoleInterceptor must return 401 UNAUTHORIZED for unauthenticated user");

        // Test RequiredAllPermissionsInterceptor
        RequiredAllPermissionsInterceptor permInterceptor = createPermissionsInterceptor(unauthCtx);
        InvocationContext permIc = new PermissionInvocationContext();

        DemoiselleSecurityException permEx = assertThrows(
                DemoiselleSecurityException.class,
                () -> permInterceptor.manage(permIc),
                "RequiredAllPermissionsInterceptor must reject unauthenticated user (seed: " + seed + ")");
        assertEquals(401, permEx.getStatusCode(),
                "RequiredAllPermissionsInterceptor must return 401 UNAUTHORIZED for unauthenticated user");
    }

    /**
     * Generates arbitrary seed strings to vary across iterations.
     * This ensures the property is tested across many jqwik runs even though
     * the core assertion is deterministic for unauthenticated users.
     */
    @Provide
    Arbitrary<String> seedStrings() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }
}
