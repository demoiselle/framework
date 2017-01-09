/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */

package org.demoiselle.jee.security.message;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
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
public class DemoiselleSecurityMessagesTest {


    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    @Inject
    private DemoiselleSecurityMessages instance;
    public DemoiselleSecurityMessagesTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of accessCheckingPermission method, of class
     * DemoiselleSecurityMessages.
     */
    @Test
    public void test11() {
        String operacao = "Teste1";
        String recurso = "Teste2";
        String expResult = "O usuário não tem permissão para executar a ação Teste1 no recurso Teste2";
        String result = instance.accessCheckingPermission(operacao, recurso);
        assertEquals(expResult, result);

    }

    /**
     * Test of accessDenied method, of class DemoiselleSecurityMessages.
     */
    @Test
    public void test12() {
        String usuario = "Teste1";
        String operacao = "Teste2";
        String recurso = "Teste3";
        String expResult = "O usuário não possui permissão para executar a ação Teste1 no recurso Teste2";
        String result = instance.accessDenied(usuario, operacao, recurso);
        assertEquals(expResult, result);

    }

    /**
     * Test of userNotAuthenticated method, of class DemoiselleSecurityMessages.
     */
    @Test
    public void test13() {
        String expResult = "Usuário não autenticado";
        String result = instance.userNotAuthenticated();
        assertEquals(expResult, result);

    }

    /**
     * Test of invalidCredentials method, of class DemoiselleSecurityMessages.
     */
    @Test
    public void test14() {
        String expResult = "Usuário ou senha inválidos";
        String result = instance.invalidCredentials();
        assertEquals(expResult, result);

    }

    /**
     * Test of doesNotHaveRole method, of class DemoiselleSecurityMessages.
     */
    @Test
    public void test15() {
        String role = "Teste1";
        String expResult = "O Usuário não possui a role:Teste1";
        String result = instance.doesNotHaveRole(role);
        assertEquals(expResult, result);

    }

    /**
     * Test of doesNotHavePermission method, of class
     * DemoiselleSecurityMessages.
     */
    @Test
    public void test16() {
        String operacao = "Teste1";
        String recurso = "Teste2";
        String expResult = "O Usuário não possui a permissão para executar a ação Teste1 no recurso Teste2";
        String result = instance.doesNotHavePermission(operacao, recurso);
        assertEquals(expResult, result);

    }

}
