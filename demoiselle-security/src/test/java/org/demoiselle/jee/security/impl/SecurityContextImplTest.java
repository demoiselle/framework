/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author 70744416353
 */
@RunWith(CdiTestRunner.class)
@TestControl(startScopes = RequestScoped.class)
public class SecurityContextImplTest {

    @Inject
    private SecurityContext instance;

//    @Inject
//    private TokenManager tm;
    @Inject
    private DemoisellePrincipal dml;

    public SecurityContextImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
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

    /**
     * Test of setUser method, of class SecurityContextImpl.
     */
    @Test
    public void test11() {
        System.out.println("setUser");
        DemoisellePrincipal loggedUser = dml;
        instance.setUser(loggedUser);
    }

    /**
     * Test of hasPermission method, of class SecurityContextImpl.
     */
    @Test
    public void test12() {
        System.out.println("hasPermission");
        String resource = "Teste1";
        String operation = "TesteOp2";
        boolean expResult = true;
        boolean result = instance.hasPermission(resource, operation);
        assertEquals(expResult, result);
    }

    /**
     * Test of hasRole method, of class SecurityContextImpl.
     */
    @Test
    public void test13() {
        System.out.println("hasRole");
        String role = "Role1";
        boolean expResult = true;
        boolean result = instance.hasRole(role);
        assertEquals(expResult, result);
    }

    /**
     * Test of isLoggedIn method, of class SecurityContextImpl.
     */
    @Test
    public void test14() {
        System.out.println("isLoggedIn");
        boolean expResult = true;
        boolean result = instance.isLoggedIn();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUser method, of class SecurityContextImpl.
     */
    @Test
    public void test15() {
        System.out.println("getUser");
        DemoisellePrincipal expResult = dml;
        DemoisellePrincipal result = instance.getUser();
        assertEquals(expResult.getIdentity(), result.getIdentity());
    }

    @Test
    public void test16() {
        System.out.println("notPermission");
        String resource = "Teste";
        String operation = "TesteOp3";
        boolean expResult = false;
        boolean result = instance.hasPermission(resource, operation);
        assertEquals(expResult, result);
    }

    /**
     * Test of hasRole method, of class SecurityContextImpl.
     */
    @Test
    public void test17() {
        System.out.println("notRole");
        String role = "Role3";
        boolean expResult = false;
        boolean result = instance.hasRole(role);
        assertEquals(expResult, result);
    }

}
