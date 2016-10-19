package org.demoiselle.jee.security.token.impl;

import static java.lang.System.out;
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
//@RunWith(CdiTestRunner.class)
public class TokensManagerImplTest {

    private DemoisellePrincipal dml;

    //@Inject
    private Token token;

    //@Inject
    private TokensManager instance;

    /**
     *
     */
    public TokensManagerImplTest() {
    }

    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     *
     */
    @Before
    public void setUp() {

    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of setUser method, of class TokensManagerImpl.
     */
    @Test
    public void test20() {
        out.println("setUser");
//        token.setKey("");
//        dml = new DemoisellePrincipalMock();
//        dml.setName("Teste");
//        dml.setIdentity("1");
//        dml.addRole("ADMINISTRATOR");
//        dml.addRole("MANAGER");
//        dml.addPermission("Produto", "Alterar");
//        dml.addPermission("Categoria", "Consultar");
//        instance.setUser(dml);
//        assertNotEquals("", token.getKey());
    }

    /**
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test
    public void test21() {
        out.println("getUser");
//        dml = new DemoisellePrincipalMock();
//        dml.setName("Teste");
//        dml.setIdentity("1");
//        dml.addRole("ADMINISTRATOR");
//        dml.addRole("MANAGER");
//        dml.addPermission("Produto", "Alterar");
//        dml.addPermission("Categoria", "Consultar");
//        DemoisellePrincipal expResult = dml;
//        DemoisellePrincipal result = instance.getUser();
//        assertEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test22() {
        out.println("validate");
//        boolean expResult = true;
//        boolean result = instance.validate();
//        assertEquals(expResult, result);
    }

    /**
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test
    public void test23() {
        out.println("getUserError");
//        instance.setUser(dml);
//        token.setKey("");
//        DemoisellePrincipal expResult = dml;
//        DemoisellePrincipal result = instance.getUser();
//        assertNotEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test24() {
        out.println("validateError");
//        boolean expResult = false;
//        boolean result = instance.validate();
//        assertEquals(expResult, result);
    }

}
