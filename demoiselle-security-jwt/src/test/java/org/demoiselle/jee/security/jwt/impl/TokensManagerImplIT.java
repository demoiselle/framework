/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.jwt.impl;

import javax.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokensManager;
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
public class TokensManagerImplIT {

    @Inject
    private DemoisellePrincipalImpl dml;

    @Inject
    private Token token;

    @Inject
    private TokensManager instance;

    public TokensManagerImplIT() {
    }

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
     * Test of init method, of class TokensManagerImpl.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        //instance.init();
    }

    /**
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test
    public void testGetUser() {
        System.out.println("getUser");
//        dml.setName("Teste");
//        dml.setIdentity("" + System.currentTimeMillis());
//        dml.addRole("ADMINISTRATOR");
//        dml.addRole("MANAGER");
//        dml.addPermission("Produto", "Alterar");
//        dml.addPermission("Categoria", "Consultar");
//        instance.setUser(dml);
//        DemoisellePrincipal expResult = dml;
//        DemoisellePrincipal result = instance.getUser();
//        assertEquals(expResult, result);
    }

    /**
     * Test of setUser method, of class TokensManagerImpl.
     */
    @Test
    public void testSetUser() {
        System.out.println("setUser");
//        dml.setName("Teste");
//        dml.setIdentity("" + System.currentTimeMillis());
//        dml.addRole("ADMINISTRATOR");
//        dml.addRole("MANAGER");
//        dml.addPermission("Produto", "Alterar");
//        dml.addPermission("Categoria", "Consultar");
//        instance.setUser(dml);
//        System.out.println(token.getKey());
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void testValidate() {
        System.out.println("validate");
        boolean expResult = false;
        dml.setName("Teste");
        dml.setIdentity("" + System.currentTimeMillis());
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

}
