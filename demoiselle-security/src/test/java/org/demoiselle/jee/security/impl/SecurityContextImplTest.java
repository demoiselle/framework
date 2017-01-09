/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import static org.junit.Assert.assertEquals;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author SERPRO
 */
@RunWith(CdiTestRunner.class)
@TestControl(startScopes = RequestScoped.class)
public class SecurityContextImplTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Inject
    private SecurityContext instance;

    @Inject
    private DemoiselleUser dml;

    public SecurityContextImplTest() {
    }

    @Before
    public void setUp() {
        dml.setIdentity("123456789");
        dml.setName("Teste");
        dml.addParam("Teste1", "TesteParam1");
        dml.addParam("Teste1", "TesteParam2");
        dml.addParam("Teste1", "TesteParam2");
        dml.addParam("Teste2", "TesteParam3");
        dml.addParam("Teste2", "TesteParam4");
        dml.addParam("Teste2", "TesteParam4");
        dml.addPermission("Teste1", "TesteOp1");
        dml.addPermission("Teste1", "TesteOp2");
        dml.addPermission("Teste1", "TesteOp2");
        dml.addPermission("Teste2", "TesteOp1");
        dml.addPermission("Teste2", "TesteOp2");
        dml.addPermission("Teste2", "TesteOp2");
        dml.addRole("Role1");
        dml.addRole("Role1");
        dml.addRole("Role2");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test11() {
        DemoiselleUser loggedUser = dml;
        instance.setUser(loggedUser);
    }

    @Test
    public void test12() {
        String resource = "Teste1";
        String operation = "TesteOp2";
        boolean expResult = true;
        boolean result = instance.hasPermission(resource, operation);
        assertEquals(expResult, result);
    }

    @Test
    public void test13() {
        String role = "Role1";
        boolean expResult = true;
        boolean result = instance.hasRole(role);
        assertEquals(expResult, result);
    }

    @Test
    public void test14() {
        boolean expResult = true;
        boolean result = instance.isLoggedIn();
        assertEquals(expResult, result);
    }

    @Test
    public void test15() {
        DemoiselleUser expResult = dml;
        DemoiselleUser result = instance.getUser();
        assertEquals(expResult.getIdentity(), result.getIdentity());
    }

    @Test
    public void test16() {
        String resource = "Teste";
        String operation = "TesteOp3";
        boolean expResult = false;
        boolean result = instance.hasPermission(resource, operation);
        assertEquals(expResult, result);
    }

    @Test
    public void test17() {
        String role = "Role3";
        boolean expResult = false;
        boolean result = instance.hasRole(role);
        assertEquals(expResult, result);
    }

}
