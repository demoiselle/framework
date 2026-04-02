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
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

import jakarta.interceptor.InvocationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 6: RequiredAllPermissions permite se e somente se o usuário possui todas as permissões
/**
 * Property-based tests for {@link RequiredAllPermissionsInterceptor}.
 *
 * <p><b>Validates: Requirements 3.3, 3.4</b></p>
 */
class RequiredAllPermissionsPropertyTest {

    // Fixed permissions used in the @RequiredAllPermissions annotation on the holder method.
    // Each permission is a resource:operation pair. The property test varies the USER's
    // granted permissions against this fixed set.
    private static final String[][] ANNOTATED_PERMISSIONS = {
        {"user", "read"},
        {"user", "write"},
        {"report", "export"}
    };

    // ---- Stubs ----

    /**
     * A SecurityContext stub that is always logged in and has a configurable set of permissions.
     * Permissions are stored as "resource:operation" strings.
     */
    static class StubSecurityContext implements SecurityContext {
        private final Set<String> userPermissions;

        StubSecurityContext(Set<String> userPermissions) {
            this.userPermissions = userPermissions;
        }

        @Override public boolean isLoggedIn() { return true; }
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
        @Override public String doesNotHavePermission(String operacao, String recurso) { return "no permission: " + operacao + " on " + recurso; }
        @Override public String cloneError() { return "clone error"; }
    }

    /**
     * Holder class with a method annotated with @RequiredAllPermissions.
     * Since annotation values are fixed at compile time, we use a fixed set of permissions
     * and vary the user's granted permissions in the property test.
     */
    static class AnnotatedHolder {
        @RequiredAllPermissions({
            @Permission(resource = "user", operation = "read"),
            @Permission(resource = "user", operation = "write"),
            @Permission(resource = "report", operation = "export")
        })
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

    private RequiredAllPermissionsInterceptor createInterceptor(SecurityContext secCtx) throws Exception {
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

    // Feature: security-enhancements, Property 6: RequiredAllPermissions permite se e somente se o usuário possui todas as permissões
    /**
     * Property 6: For any non-empty set of required permissions and any authenticated user,
     * the RequiredAllPermissionsInterceptor must allow execution if and only if the user has
     * all of the listed permissions. If any is missing, it must reject with HTTP 403.
     *
     * <p><b>Validates: Requirements 3.3, 3.4</b></p>
     *
     * The annotation on the holder method requires:
     *   user:read, user:write, report:export
     * We generate random subsets of permissions for the user and verify the interceptor
     * allows iff the user has ALL required permissions.
     */
    @Property(tries = 100)
    void allPermissionsAllowsIffUserHasAll(@ForAll("userPermissionSets") Set<String> userPermissions) throws Exception {
        StubSecurityContext secCtx = new StubSecurityContext(userPermissions);
        RequiredAllPermissionsInterceptor interceptor = createInterceptor(secCtx);
        InvocationContext ic = new StubInvocationContext();

        // Compute expected: user must have ALL annotated permissions
        boolean expectedAllow = true;
        for (String[] perm : ANNOTATED_PERMISSIONS) {
            String key = perm[0] + ":" + perm[1];
            if (!userPermissions.contains(key)) {
                expectedAllow = false;
                break;
            }
        }

        if (expectedAllow) {
            // Should allow — proceed() returns "proceeded"
            Object result = interceptor.manage(ic);
            assertEquals("proceeded", result,
                    "Interceptor must allow when user has all required permissions. User permissions: " + userPermissions);
        } else {
            // Should reject with 403
            DemoiselleSecurityException thrown = assertThrows(
                    DemoiselleSecurityException.class,
                    () -> interceptor.manage(ic),
                    "Interceptor must reject when user is missing at least one required permission. User permissions: " + userPermissions);
            assertEquals(403, thrown.getStatusCode(),
                    "Exception status must be 403 FORBIDDEN");
        }
    }

    /**
     * Generates random sets of permissions as "resource:operation" strings.
     * The pool includes the 3 annotated permissions plus 3 non-matching permissions,
     * ensuring we test both matching and non-matching scenarios.
     */
    @Provide
    Arbitrary<Set<String>> userPermissionSets() {
        // Pool: the 3 annotated permissions + 3 non-matching permissions
        Arbitrary<String> permissionPool = Arbitraries.of(
                "user:read", "user:write", "report:export",   // matching
                "user:delete", "report:view", "admin:manage"   // non-matching
        );
        return permissionPool.set().ofMinSize(0).ofMaxSize(6);
    }
}
