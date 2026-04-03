/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.TokenManagerMock;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;
import org.demoiselle.jee.security.test.TestTokenProducer;
import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author SERPRO
 */
@EnableAutoWeld
@ActivateScopes(RequestScoped.class)
@AddBeanClasses({TokenManagerMock.class, DemoiselleSecurityMessages.class, TestTokenProducer.class, SecurityContextImpl.class, DemoiselleUserImpl.class})
@AddExtensions(org.demoiselle.jee.core.message.MessageBundleExtension.class)
class SecurityContextImplTest {

    @Inject
    private SecurityContext instance;

    @Inject
    private DemoiselleUser dml;

    @BeforeEach
    void setUp() {
        dml.setIdentity("123456789");
        dml.setName("Teste");
        dml.addParam("Teste1", "TesteParam1");
        dml.addParam("Teste2", "TesteParam3");
        dml.addPermission("Teste1", "TesteOp1");
        dml.addPermission("Teste1", "TesteOp2");
        dml.addPermission("Teste2", "TesteOp1");
        dml.addPermission("Teste2", "TesteOp2");
        dml.addRole("Role1");
        dml.addRole("Role2");
    }

    @Test
    void test11() {
        DemoiselleUser loggedUser = dml;
        instance.setUser(loggedUser);
    }

    @Test
    void test12() {
        instance.setUser(dml);
        String resource = "Teste1";
        String operation = "TesteOp2";
        boolean expResult = true;
        boolean result = instance.hasPermission(resource, operation);
        assertEquals(expResult, result);
    }

    @Test
    void test13() {
        instance.setUser(dml);
        String role = "Role1";
        boolean expResult = true;
        boolean result = instance.hasRole(role);
        assertEquals(expResult, result);
    }

    @Test
    void test14() {
        instance.setUser(dml);
        boolean expResult = true;
        boolean result = instance.isLoggedIn();
        assertEquals(expResult, result);
    }

    @Test
    void test15() {
        instance.setUser(dml);
        DemoiselleUser result = instance.getUser();
        assertEquals(dml.getIdentity(), result.getIdentity());
    }

    @Test
    void test16() {
        instance.setUser(dml);
        String resource = "Teste";
        String operation = "TesteOp3";
        boolean expResult = false;
        boolean result = instance.hasPermission(resource, operation);
        assertEquals(expResult, result);
    }

    @Test
    void test17() {
        instance.setUser(dml);
        String role = "Role3";
        boolean expResult = false;
        boolean result = instance.hasRole(role);
        assertEquals(expResult, result);
    }

    @Test
    void hasPermissionReturnsFalseWhenUserIsNull() {
        // No user set — getUser() returns null
        assertFalse(instance.hasPermission("anyResource", "anyOperation"));
    }

    @Test
    void hasRoleReturnsFalseWhenUserIsNull() {
        // No user set — getUser() returns null
        assertFalse(instance.hasRole("anyRole"));
    }

    @Test
    void isLoggedInReturnsFalseWhenNoUserSet() {
        // No user set — TokenManager.validate() returns false
        assertFalse(instance.isLoggedIn());
    }

    @Test
    void getUserReturnsNullWhenNoUserSet() {
        // No user set — getUser() should return null without NPE
        assertNull(instance.getUser());
    }

    // --- hasAnyRole tests ---

    @Test
    void hasAnyRoleReturnsTrueWhenUserHasOneOfTheRoles() {
        instance.setUser(dml);
        assertTrue(instance.hasAnyRole("Role1", "NonExistent"));
    }

    @Test
    void hasAnyRoleReturnsFalseWhenUserHasNoneOfTheRoles() {
        instance.setUser(dml);
        assertFalse(instance.hasAnyRole("NonExistent1", "NonExistent2"));
    }

    @Test
    void hasAnyRoleReturnsFalseWhenUserIsNull() {
        assertFalse(instance.hasAnyRole("Role1"));
    }

    @Test
    void hasAnyRoleReturnsFalseForNullArgument() {
        instance.setUser(dml);
        assertFalse(instance.hasAnyRole((String[]) null));
    }

    @Test
    void hasAnyRoleReturnsFalseForEmptyArgument() {
        instance.setUser(dml);
        assertFalse(instance.hasAnyRole());
    }

    // --- hasAllRoles tests ---

    @Test
    void hasAllRolesReturnsTrueWhenUserHasAllRoles() {
        instance.setUser(dml);
        assertTrue(instance.hasAllRoles("Role1", "Role2"));
    }

    @Test
    void hasAllRolesReturnsFalseWhenUserMissesOneRole() {
        instance.setUser(dml);
        assertFalse(instance.hasAllRoles("Role1", "NonExistent"));
    }

    @Test
    void hasAllRolesReturnsFalseWhenUserIsNull() {
        assertFalse(instance.hasAllRoles("Role1"));
    }

    @Test
    void hasAllRolesReturnsFalseForNullArgument() {
        instance.setUser(dml);
        assertFalse(instance.hasAllRoles((String[]) null));
    }

    @Test
    void hasAllRolesReturnsFalseForEmptyArgument() {
        instance.setUser(dml);
        assertFalse(instance.hasAllRoles());
    }

}
