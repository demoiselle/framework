/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import static java.lang.System.out;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.core.api.security.TokenType;
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
public class TokenManagerImplTest {

    @Inject
    private DemoiselleUser dml;

    @Inject
    private Token token;

    private static String localtoken;

    @Inject
    private TokenManager instance;

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

    @Test
    public void test20() {
        out.println("setUser");
        token.setKey("");
        token.setType(TokenType.JWT);
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Produto", "Excluir");
        dml.addPermission("Categoria", "Consultar");
        dml.addPermission("Categoria", "Alterar");
        dml.addPermission("Categoria", "Incluir");
        dml.addPermission("Produto", "Alterar");
        dml.addParam("email", "user@demoiselle.org");
        dml.addParam("endereco", "rua carlos pioli, 133");
        dml.addParam("fone", "4135938000");
        instance.setUser(dml);
        localtoken = token.getKey();
        assertNotEquals("", token.getKey());
    }

    @Test
    public void test21() {
        out.println("getUser");
        token.setKey(localtoken);
        token.setType(TokenType.JWT);
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Produto", "Excluir");
        dml.addPermission("Categoria", "Consultar");
        dml.addPermission("Categoria", "Alterar");
        dml.addPermission("Categoria", "Incluir");
        dml.addPermission("Produto", "Alterar");
        dml.addParam("email", "user@demoiselle.org");
        dml.addParam("endereco", "rua carlos pioli, 133");
        dml.addParam("fone", "4135938000");
        DemoiselleUser expResult = dml;
        DemoiselleUser result = instance.getUser();
        assertEquals(expResult, result);
    }

    @Test
    public void test22() {
        out.println("validate");
        token.setKey(localtoken);
        token.setType(TokenType.JWT);
        boolean expResult = true;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

    @Test
    public void test23() {
        out.println("getUserError");
        token.setKey(localtoken);
        token.setType(TokenType.JWT);
        dml.setName("Teste2");
        dml.setIdentity("2");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Produto", "Excluir");
        dml.addPermission("Categoria", "Consultar");
        dml.addPermission("Categoria", "Alterar");
        dml.addPermission("Categoria", "Incluir");
        dml.addPermission("Produto", "Alterar");
        dml.addParam("email", "user@demoiselle.org");
        dml.addParam("endereco", "rua carlos pioli, 133");
        dml.addParam("fone", "4135938000");
        String expResult = dml.getIdentity();
        String result = instance.getUser().getIdentity();
        assertNotEquals(expResult, result);
    }

    @Test
    public void test24() {
        out.println("validateError");
        token.setKey("");
        token.setType(TokenType.JWT);
        boolean expResult = false;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test25() {
        out.println("Delete");
        instance.removeUser(dml);
    }

}
