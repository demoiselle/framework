package org.demoiselle.jee.security.jwt.impl;

import static java.lang.System.out;
import javax.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokensManager;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
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
public class TokensManagerImplMasterTest {

    @Inject
    private DemoisellePrincipal dml;

    @Inject
    private Token token;

    private static String localtoken;

    @Inject
    private TokensManager instance;

    @Inject
    private Config config;

    /**
     *
     */
    public TokensManagerImplMasterTest() {
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

    @Test
    public void test10() {
        out.println("init - Type null");
        config.setType(null);
        config.setPrivateKey(null);
        config.setPublicKey(null);
        ((TokensManagerImpl) instance).init();
    }

    @Test
    public void test11() {
        out.println("init - Type master");
        config.setType("master");
        ((TokensManagerImpl) instance).init();
    }

    /**
     * Test of setUser method, of class TokensManagerImpl.
     */
    @Test
    public void test20() {
        out.println("setUser");
        token.setKey("");
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        localtoken = token.getKey();
        assertNotEquals("", token.getKey());
    }

    /**
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test
    public void test21() {
        out.println("getUser");
        token.setKey(localtoken);
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        DemoisellePrincipal expResult = dml;
        DemoisellePrincipal result = instance.getUser();
        assertEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test22() {
        out.println("validate");
        token.setKey(localtoken);
        boolean expResult = true;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test
    public void test23() {
        out.println("getUserError");
        instance.setUser(dml);
        token.setKey("");
        DemoisellePrincipal expResult = dml;
        DemoisellePrincipal result = instance.getUser();
        assertNotEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test24() {
        out.println("validateError");
        token.setKey("");
        boolean expResult = false;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

}
