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
import org.demoiselle.jee.security.annotation.RequiredAnyRole;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

import jakarta.interceptor.InvocationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 5: RequiredAnyRole permite se e somente se o usuário possui pelo menos uma role
/**
 * Property-based tests for {@link RequiredAnyRoleInterceptor}.
 *
 * <p><b>Validates: Requirements 3.1, 3.2</b></p>
 */
class RequiredAnyRolePropertyTest {

    // Fixed roles used in the @RequiredAnyRole annotation on the holder method.
    // The property test varies the USER's roles against this fixed set.
    private static final String[] ANNOTATED_ROLES = {"admin", "manager", "editor"};

    // ---- Stubs ----

    /**
     * A SecurityContext stub that is always logged in and has a configurable set of roles.
     */
    static class StubSecurityContext implements SecurityContext {
        private final Set<String> userRoles;

        StubSecurityContext(Set<String> userRoles) {
            this.userRoles = userRoles;
        }

        @Override public boolean isLoggedIn() { return true; }
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
     * Holder class with a method annotated with @RequiredAnyRole.
     * Since annotation values are fixed at compile time, we use a fixed set of roles
     * and vary the user's roles in the property test.
     */
    static class AnnotatedHolder {
        @RequiredAnyRole({"admin", "manager", "editor"})
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

    private RequiredAnyRoleInterceptor createInterceptor(SecurityContext secCtx) throws Exception {
        RequiredAnyRoleInterceptor interceptor = new RequiredAnyRoleInterceptor();

        Field scField = RequiredAnyRoleInterceptor.class.getDeclaredField("securityContext");
        scField.setAccessible(true);
        scField.set(interceptor, secCtx);

        Field bundleField = RequiredAnyRoleInterceptor.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(interceptor, new StubMessages());

        return interceptor;
    }

    // ---- Property Test ----

    // Feature: security-enhancements, Property 5: RequiredAnyRole permite se e somente se o usuário possui pelo menos uma role
    /**
     * Property 5: For any non-empty set of required roles and any authenticated user,
     * the RequiredAnyRoleInterceptor must allow execution if and only if the user has
     * at least one of the listed roles. Otherwise, it must reject with HTTP 403.
     *
     * <p><b>Validates: Requirements 3.1, 3.2</b></p>
     *
     * The annotation on the holder method requires {"admin", "manager", "editor"}.
     * We generate random subsets of roles for the user and verify the interceptor
     * allows iff the intersection with the required roles is non-empty.
     */
    @Property(tries = 100)
    void anyRoleAllowsIffUserHasAtLeastOne(@ForAll("userRoleSets") Set<String> userRoles) throws Exception {
        StubSecurityContext secCtx = new StubSecurityContext(userRoles);
        RequiredAnyRoleInterceptor interceptor = createInterceptor(secCtx);
        InvocationContext ic = new StubInvocationContext();

        // Compute expected: user has at least one of the annotated roles
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
