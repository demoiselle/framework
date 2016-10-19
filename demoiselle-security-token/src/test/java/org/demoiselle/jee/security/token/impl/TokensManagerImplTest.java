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
@RunWith(CdiTestRunner.class)
public class TokensManagerImplTest {

    @Inject
    private DemoisellePrincipal dml;

    @Inject
    private Token token;

    private static String localtoken;

    @Inject
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
        String expResult = dml.getIdentity();
        String result = instance.getUser().getIdentity();
        assertEquals(expResult, result);
    }

    @Test
    public void test22() {
        out.println("setUser já existente");
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        localtoken = token.getKey();
        instance.setUser(dml);
        assertEquals(localtoken, token.getKey());
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test23() {
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
    public void test24() {
        out.println("getUserError");
        dml.setName("Teste2");
        dml.setIdentity("2");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        token.setKey(localtoken);
        DemoisellePrincipal expResult = dml;
        DemoisellePrincipal result = instance.getUser();
        assertNotEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test25() {
        out.println("validateError");
        boolean expResult = false;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test26() {
        out.println("remove token");
        token.setKey(localtoken);
        ((TokensManagerImpl) instance).removeToken();
    }

    @Test
    public void test27() {
        out.println("remove user");
        dml.setName("Teste2");
        dml.setIdentity("2");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        ((TokensManagerImpl) instance).removeToken(dml);
        dml = instance.getUser();
        assertNull(dml);
    }

}
