/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author 70744416353
 */
@RunWith(CdiTestRunner.class)
@TestControl(startScopes = RequestScoped.class)
public class DemoisellePrincipalImplTest {

    @Inject
    private DemoisellePrincipal instance;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setIdentity method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test11() {
        System.out.println("setIdentity");
        String identity = "123456789";
        instance.setIdentity(identity);
    }

    /**
     * Test of getIdentity method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test12() {
        System.out.println("getIdentity");
        String expResult = "123456789";
        String result = instance.getIdentity();
        assertEquals(expResult, result);
    }

    /**
     * Test of setName method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test13() {
        System.out.println("setName");
        String name = "Teste";
        instance.setName(name);
    }

    /**
     * Test of getName method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test14() {
        System.out.println("getName");
        String expResult = "Teste";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRoles method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test15() {
        System.out.println("setParams");
        instance.addParam("Teste1", "TesteParam1");
        instance.addParam("Teste1", "TesteParam2");
        instance.addParam("Teste1", "TesteParam2");
        instance.addParam("Teste2", "TesteParam3");
        instance.addParam("Teste2", "TesteParam4");
        instance.addParam("Teste2", "TesteParam4");
    }

    /**
     * Test of getRoles method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test16() {
        System.out.println("getParams");
        assertEquals(instance.getParams().size(), 2);
    }

    /**
     * Test of setPermissions method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test17() {
        System.out.println("setPermissions");
        instance.addPermission("Teste1", "TesteOp1");
        instance.addPermission("Teste1", "TesteOp2");
        instance.addPermission("Teste1", "TesteOp2");
        instance.addPermission("Teste2", "TesteOp1");
        instance.addPermission("Teste2", "TesteOp2");
        instance.addPermission("Teste2", "TesteOp2");
    }

    /**
     * Test of getPermissions method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test18() {
        System.out.println("getPermissions");
        assertEquals(instance.getPermissions().size(), 2);
    }

    /**
     * Test of setParams method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test21() {
        System.out.println("setRoles");
        instance.addRole("Role1");
        instance.addRole("Role1");
        instance.addRole("Role2");
    }

    /**
     * Test of getParams method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test22() {
        System.out.println("getRoles");
        assertEquals(instance.getRoles().size(), 2);
    }

    /**
     * Test of addRole method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test23() {
        System.out.println("addRole");
        String role = "Role10";
        instance.addRole(role);
    }

    /**
     * Test of getPermissions method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test24() {
        System.out.println("getPermissions");
        String resource = "Teste1";
        List<String> result = instance.getPermissions(resource);
        assertEquals(2, result.size());
    }

    /**
     * Test of addPermission method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test25() {
        System.out.println("addPermission");
        String resource = "Teste3";
        String operetion = "Teste1";
        instance.addPermission(resource, operetion);
    }

    /**
     * Test of addParam method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test26() {
        System.out.println("addParam");
        String key = "Teste3";
        String value = "Teste1";
        instance.addParam(key, value);
    }

    /**
     * Test of hashCode method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test27() {
        System.out.println("hashCode");
        int expResult = 0;
        int result = instance.hashCode();
        assertNotEquals(expResult, result);
    }

    /**
     * Test of equals method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test28() {
        System.out.println("equals");
        boolean result = instance.equals(instance.clone());
        assertEquals(false, result);
    }

    /**
     * Test of toString method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test29() {
        System.out.println("toString");
        String expResult = "{\"identity\":\"123456789\", \"name\":\"Teste\"}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of clone method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test31() {
        System.out.println("clone");
        DemoisellePrincipal result = instance.clone();
        assertEquals(instance.getIdentity(), result.getIdentity());
    }

    /**
     * Test of clone method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test32() {
        System.out.println("removePermission");
        instance.removePermission("Teste1", "TesteOp2");
        instance.removePermission("Teste1", "TesteOp1");
        assertEquals(instance.getPermissions().size(), 2);
        assertEquals(instance.getPermissions("Teste2").size(), 2);
    }

    /**
     * Test of clone method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test33() {
        System.out.println("removeParams");
        instance.removeParam("Teste1", "TesteParam1");
        instance.removeParam("Teste1", "TesteParam2");
        assertEquals(instance.getParams().size(), 2);
        assertEquals(instance.getParams("Teste2").size(), 2);
    }

    /**
     * Test of clone method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test34() {
        System.out.println("removeRoles");
        instance.removeRole("Role1");
        assertEquals(instance.getRoles().size(), 2);
    }

    /**
     * Test of clone method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test41() {
        System.out.println("Permissions");
        Map<String, List<String>> perm = new ConcurrentHashMap<>();
        instance.setPermissions(perm);
        assertEquals(instance.getPermissions().size(), 0);
    }

    /**
     * Test of clone method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test42() {
        System.out.println("Params");
        Map<String, List<String>> param = new ConcurrentHashMap<>();
        instance.setParams(param);
        assertEquals(instance.getParams().size(), 0);
    }

    /**
     * Test of clone method, of class DemoisellePrincipalImpl.
     */
    @Test
    public void test43() {
        System.out.println("Roles");
        List<String> roles = new ArrayList<>();
        instance.setRoles(roles);
        assertEquals(instance.getRoles().size(), 0);
    }

}
