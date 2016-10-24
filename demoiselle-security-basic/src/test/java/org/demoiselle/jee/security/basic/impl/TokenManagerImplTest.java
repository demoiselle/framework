/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.basic.impl;

import javax.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.security.impl.DemoisellePrincipalImpl;
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
public class TokenManagerImplTest {

    @Inject
    private TokenManager tokensManager;

    public TokenManagerImplTest() {
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
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetUser() {
        System.out.println("getUser");
        tokensManager.getUser();
    }

    /**
     * Test of setUser method, of class TokensManagerImpl.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetUser() {
        System.out.println("setUser");
        DemoisellePrincipal dp = new DemoisellePrincipalImpl();
        tokensManager.setUser(dp);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testValidate() {
        System.out.println("validate");
        tokensManager.validate();
    }

}
