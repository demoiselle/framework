/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the default methods {@code hasAnyRole} and {@code hasAllRoles}
 * on the {@link SecurityContext} interface.
 */
class SecurityContextDefaultMethodsTest {

    /**
     * Minimal stub that delegates hasRole to a fixed set of roles.
     */
    private static SecurityContext contextWithRoles(String... userRoles) {
        final Set<String> roles = Set.of(userRoles);
        return new SecurityContext() {
            @Override public boolean isLoggedIn() { return true; }
            @Override public boolean hasPermission(String resource, String operation) { return false; }
            @Override public boolean hasRole(String role) { return roles.contains(role); }
            @Override public DemoiselleUser getUser() { return null; }
            @Override public DemoiselleUser getUser(String issuer, String audience) { return null; }
            @Override public void setUser(DemoiselleUser loggedUser) {}
            @Override public void setUser(DemoiselleUser loggedUser, String issuer, String audience) {}
            @Override public void removeUser(DemoiselleUser loggedUser) {}
        };
    }

    // --- hasAnyRole ---

    @Test
    void hasAnyRole_returnsTrue_whenUserHasOneOfTheRoles() {
        SecurityContext ctx = contextWithRoles("admin", "user");
        assertTrue(ctx.hasAnyRole("admin", "manager"));
    }

    @Test
    void hasAnyRole_returnsFalse_whenUserHasNoneOfTheRoles() {
        SecurityContext ctx = contextWithRoles("user");
        assertFalse(ctx.hasAnyRole("admin", "manager"));
    }

    @Test
    void hasAnyRole_returnsFalse_forNullParameter() {
        SecurityContext ctx = contextWithRoles("admin");
        assertFalse(ctx.hasAnyRole((String[]) null));
    }

    @Test
    void hasAnyRole_returnsFalse_forEmptyArray() {
        SecurityContext ctx = contextWithRoles("admin");
        assertFalse(ctx.hasAnyRole());
    }

    // --- hasAllRoles ---

    @Test
    void hasAllRoles_returnsTrue_whenUserHasAllRoles() {
        SecurityContext ctx = contextWithRoles("admin", "manager", "user");
        assertTrue(ctx.hasAllRoles("admin", "manager"));
    }

    @Test
    void hasAllRoles_returnsFalse_whenUserMissesOneRole() {
        SecurityContext ctx = contextWithRoles("admin");
        assertFalse(ctx.hasAllRoles("admin", "manager"));
    }

    @Test
    void hasAllRoles_returnsFalse_forNullParameter() {
        SecurityContext ctx = contextWithRoles("admin");
        assertFalse(ctx.hasAllRoles((String[]) null));
    }

    @Test
    void hasAllRoles_returnsFalse_forEmptyArray() {
        SecurityContext ctx = contextWithRoles("admin");
        assertFalse(ctx.hasAllRoles());
    }
}
