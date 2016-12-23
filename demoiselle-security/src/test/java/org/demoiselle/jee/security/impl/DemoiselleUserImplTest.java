
/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoiselleUser;
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
public class DemoiselleUserImplTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Inject
    private DemoiselleUser instance;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void test11() {
        String identity = "123456789";
        instance.setIdentity(identity);
    }

    @Test
    public void test12() {
        String expResult = "123456789";
        String result = instance.getIdentity();
        assertEquals(expResult, result);
    }

    @Test
    public void test13() {
        String name = "Teste";
        instance.setName(name);
    }

    @Test
    public void test14() {
        String expResult = "Teste";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void test15() {
        instance.addParam("Teste1", "TesteParam1");
        instance.addParam("Teste1", "TesteParam2");
        instance.addParam("Teste1", "TesteParam2");
        instance.addParam("Teste2", "TesteParam3");
        instance.addParam("Teste2", "TesteParam4");
        instance.addParam("Teste2", "TesteParam4");
    }

    @Test
    public void test16() {
        assertEquals(instance.getParams().size(), 2);
    }

    @Test
    public void test17() {
        instance.addPermission("Teste1", "TesteOp1");
        instance.addPermission("Teste1", "TesteOp2");
        instance.addPermission("Teste1", "TesteOp2");
        instance.addPermission("Teste2", "TesteOp1");
        instance.addPermission("Teste2", "TesteOp2");
        instance.addPermission("Teste2", "TesteOp2");
    }

    @Test
    public void test18() {
        assertEquals(instance.getPermissions().size(), 2);
    }

    @Test
    public void test21() {
        instance.addRole("Role1");
        instance.addRole("Role1");
        instance.addRole("Role2");
    }

    @Test
    public void test22() {
        assertEquals(instance.getRoles().size(), 2);
    }

    @Test
    public void test23() {
        String role = "Role10";
        instance.addRole(role);
    }

    @Test
    public void test24() {
        String resource = "Teste1";
        List<String> result = instance.getPermissions(resource);
        assertEquals(2, result.size());
    }

    @Test
    public void test25() {
        String resource = "Teste3";
        String operetion = "Teste1";
        instance.addPermission(resource, operetion);
    }

    @Test
    public void test26() {
        String key = "Teste3";
        String value = "Teste1";
        instance.addParam(key, value);
    }

    @Test
    public void test27() {
        int expResult = 0;
        int result = instance.hashCode();
        assertNotEquals(expResult, result);
    }

    @Test
    public void test28() {
        boolean result = instance.equals(instance.clone());
        assertEquals(false, result);
    }

    @Test
    public void test29() {
        String expResult = "{\"identity\":\"123456789\", \"name\":\"Teste\"}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void test31() {
        DemoiselleUser result = instance.clone();
        assertEquals(instance.getIdentity(), result.getIdentity());
    }

    @Test
    public void test32() {
        instance.removePermission("Teste1", "TesteOp2");
        instance.removePermission("Teste1", "TesteOp1");
        assertEquals(instance.getPermissions().size(), 2);
        assertEquals(instance.getPermissions("Teste2").size(), 2);
    }

    @Test
    public void test33() {
        instance.removeParam("Teste1");
        instance.removeParam("Teste1");
        assertEquals(instance.getParams().size(), 2);
        assertEquals(instance.getParams("Teste2").length(), 11);
    }

    @Test
    public void test34() {
        instance.removeRole("Role1");
        assertEquals(instance.getRoles().size(), 2);
    }

}
