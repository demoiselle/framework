/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl.pbt;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.security.impl.SecurityContextImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jee-migration-v4, Property 12: Null-safety dos métodos de segurança
 *
 * Validates: Requirements 13.1, 13.2, 13.3
 *
 * For any resource/operation/role strings, hasPermission/hasRole should return
 * false when user is null (not authenticated). isLoggedIn should also return
 * false. None of these methods should throw NullPointerException.
 */
class SecurityContextNullSafetyPropertyTest {

    /**
     * A TokenManager that always returns null for getUser() and false for validate(),
     * simulating an unauthenticated state.
     */
    static class NullUserTokenManager implements TokenManager {
        @Override public DemoiselleUser getUser() { return null; }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return null; }
        @Override public void setUser(DemoiselleUser user) {}
        @Override public void setUser(DemoiselleUser user, String issuer, String audience) {}
        @Override public void removeUser(DemoiselleUser user) {}
        @Override public boolean validate() { return false; }
        @Override public boolean validate(String issuer, String audience) { return false; }
    }

    private SecurityContextImpl createSecurityContextWithNullUser() throws Exception {
        SecurityContextImpl ctx = new SecurityContextImpl();
        java.lang.reflect.Field tmField = SecurityContextImpl.class.getDeclaredField("tm");
        tmField.setAccessible(true);
        tmField.set(ctx, new NullUserTokenManager());
        return ctx;
    }

    @Provide
    Arbitrary<String> resources() {
        return Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                Arbitraries.just(""),
                Arbitraries.strings().withCharRange(' ', '~').ofMinLength(1).ofMaxLength(30)
        );
    }

    @Provide
    Arbitrary<String> operations() {
        return Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                Arbitraries.just(""),
                Arbitraries.strings().withCharRange(' ', '~').ofMinLength(1).ofMaxLength(30)
        );
    }

    @Provide
    Arbitrary<String> roles() {
        return Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                Arbitraries.just(""),
                Arbitraries.strings().withCharRange(' ', '~').ofMinLength(1).ofMaxLength(30)
        );
    }

    /**
     * P12: hasPermission() should return false (not throw NPE) when user is null.
     */
    @Property(tries = 100)
    @Tag("Feature_jee-migration-v4_Property-12_null-safe-security-methods")
    void hasPermissionReturnsFalseWhenUserIsNull(
            @ForAll("resources") String resource,
            @ForAll("operations") String operation) throws Exception {

        SecurityContextImpl ctx = createSecurityContextWithNullUser();

        boolean result = assertDoesNotThrow(
                () -> ctx.hasPermission(resource, operation),
                "hasPermission() should not throw when user is null for resource='"
                        + resource + "', operation='" + operation + "'");

        assertFalse(result,
                "hasPermission() should return false when user is null");
    }

    /**
     * P12: hasRole() should return false (not throw NPE) when user is null.
     */
    @Property(tries = 100)
    @Tag("Feature_jee-migration-v4_Property-12_null-safe-security-methods")
    void hasRoleReturnsFalseWhenUserIsNull(
            @ForAll("roles") String role) throws Exception {

        SecurityContextImpl ctx = createSecurityContextWithNullUser();

        boolean result = assertDoesNotThrow(
                () -> ctx.hasRole(role),
                "hasRole() should not throw when user is null for role='" + role + "'");

        assertFalse(result,
                "hasRole() should return false when user is null");
    }

    /**
     * P12: isLoggedIn() should return false (not throw NPE) when user is null.
     */
    @Property(tries = 100)
    @Tag("Feature_jee-migration-v4_Property-12_null-safe-security-methods")
    void isLoggedInReturnsFalseWhenUserIsNull() throws Exception {

        SecurityContextImpl ctx = createSecurityContextWithNullUser();

        boolean result = assertDoesNotThrow(
                () -> ctx.isLoggedIn(),
                "isLoggedIn() should not throw when user is null");

        assertFalse(result,
                "isLoggedIn() should return false when user is null");
    }

    /**
     * P12 (edge case): When TokenManager itself is null, isLoggedIn should still return false.
     */
    @Property(tries = 10)
    @Tag("Feature_jee-migration-v4_Property-12_null-safe-security-methods")
    void isLoggedInReturnsFalseWhenTokenManagerIsNull() throws Exception {
        SecurityContextImpl ctx = new SecurityContextImpl();
        // tm field is null by default (no injection)

        boolean result = assertDoesNotThrow(
                () -> ctx.isLoggedIn(),
                "isLoggedIn() should not throw when TokenManager is null");

        assertFalse(result,
                "isLoggedIn() should return false when TokenManager is null");
    }
}
