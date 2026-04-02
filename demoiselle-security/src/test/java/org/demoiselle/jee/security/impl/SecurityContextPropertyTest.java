/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.TokenManager;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link SecurityContextImpl} convenience methods.
 *
 * <p><b>Validates: Requirements 6.1</b></p>
 */
class SecurityContextPropertyTest {

    /**
     * A minimal TokenManager stub that holds a DemoiselleUser with a fixed set of roles.
     */
    static class StubTokenManager implements TokenManager {
        private final DemoiselleUser user;

        StubTokenManager(Set<String> userRoles) {
            DemoiselleUserImpl u = new DemoiselleUserImpl();
            u.init();
            u.setIdentity("test-user");
            u.setName("Test");
            for (String role : userRoles) {
                u.addRole(role);
            }
            this.user = u;
        }

        @Override public DemoiselleUser getUser() { return user; }
        @Override public DemoiselleUser getUser(String issuer, String audience) { return user; }
        @Override public void setUser(DemoiselleUser user) {}
        @Override public void setUser(DemoiselleUser user, String issuer, String audience) {}
        @Override public void removeUser(DemoiselleUser user) {}
        @Override public boolean validate() { return true; }
        @Override public boolean validate(String issuer, String audience) { return true; }
    }

    private SecurityContextImpl createContext(Set<String> userRoles) throws Exception {
        SecurityContextImpl ctx = new SecurityContextImpl();
        Field tmField = SecurityContextImpl.class.getDeclaredField("tm");
        tmField.setAccessible(true);
        tmField.set(ctx, new StubTokenManager(userRoles));
        return ctx;
    }

    @Provide
    Arbitrary<Set<String>> roleSets() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .set()
                .ofMinSize(1)
                .ofMaxSize(10);
    }

    // Feature: security-enhancements, Property 12: hasAnyRole retorna true se e somente se o usuário possui pelo menos uma role
    /**
     * Property 12: For any non-empty set of roles and any authenticated user with a set
     * of assigned roles, hasAnyRole(roles) must return true if and only if the intersection
     * between the provided roles and the user's roles is non-empty.
     *
     * <p><b>Validates: Requirements 6.1</b></p>
     */
    @Property(tries = 100)
    void hasAnyRoleReturnsTrueIffIntersectionNonEmpty(
            @ForAll("roleSets") Set<String> userRoles,
            @ForAll("roleSets") Set<String> queryRoles) throws Exception {

        SecurityContextImpl ctx = createContext(userRoles);
        String[] queryArray = queryRoles.toArray(new String[0]);

        // Expected: intersection between queryRoles and userRoles is non-empty
        boolean expectedHasAny = queryRoles.stream().anyMatch(userRoles::contains);

        boolean actual = ctx.hasAnyRole(queryArray);

        assertEquals(expectedHasAny, actual,
                String.format("hasAnyRole mismatch — userRoles=%s, queryRoles=%s", userRoles, queryRoles));
    }

    // Feature: security-enhancements, Property 13: hasAllRoles retorna true se e somente se o usuário possui todas as roles
    /**
     * Property 13: For any non-empty set of roles and any authenticated user with a set
     * of assigned roles, hasAllRoles(roles) must return true if and only if all provided
     * roles are contained in the user's roles.
     *
     * <p><b>Validates: Requirements 6.2</b></p>
     */
    @Property(tries = 100)
    void hasAllRolesReturnsTrueIffAllContained(
            @ForAll("roleSets") Set<String> userRoles,
            @ForAll("roleSets") Set<String> queryRoles) throws Exception {

        SecurityContextImpl ctx = createContext(userRoles);
        String[] queryArray = queryRoles.toArray(new String[0]);

        // Expected: all queryRoles are contained in userRoles
        boolean expectedHasAll = userRoles.containsAll(queryRoles);

        boolean actual = ctx.hasAllRoles(queryArray);

        assertEquals(expectedHasAll, actual,
                String.format("hasAllRoles mismatch — userRoles=%s, queryRoles=%s", userRoles, queryRoles));
    }
}
