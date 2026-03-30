/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */

package org.demoiselle.jee.security.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

/**
 *
 * @author SERPRO
 */
@EnableAutoWeld
@AddExtensions(org.demoiselle.jee.core.message.MessageBundleExtension.class)
@AddBeanClasses(DemoiselleSecurityMessages.class)
class DemoiselleSecurityMessagesTest {

    @Inject
    private DemoiselleSecurityMessages instance;

    /**
     * Test of accessCheckingPermission method, of class
     * DemoiselleSecurityMessages.
     */
    @Test
    void test11() {
        String operacao = "Teste1";
        String recurso = "Teste2";
        String expResult = "O usu\u00e1rio n\u00e3o tem permiss\u00e3o para executar a a\u00e7\u00e3o Teste1 no recurso Teste2";
        String result = instance.accessCheckingPermission(operacao, recurso);
        assertEquals(expResult, result);
    }

    /**
     * Test of accessDenied method, of class DemoiselleSecurityMessages.
     */
    @Test
    void test12() {
        String usuario = "Teste1";
        String operacao = "Teste2";
        String recurso = "Teste3";
        String expResult = "O usu\u00e1rio n\u00e3o possui permiss\u00e3o para executar a a\u00e7\u00e3o Teste1 no recurso Teste2";
        String result = instance.accessDenied(usuario, operacao, recurso);
        assertEquals(expResult, result);
    }

    /**
     * Test of userNotAuthenticated method, of class DemoiselleSecurityMessages.
     */
    @Test
    void test13() {
        String expResult = "Usu\u00e1rio n\u00e3o autenticado";
        String result = instance.userNotAuthenticated();
        assertEquals(expResult, result);
    }

    /**
     * Test of invalidCredentials method, of class DemoiselleSecurityMessages.
     */
    @Test
    void test14() {
        String expResult = "Usu\u00e1rio ou senha inv\u00e1lidos";
        String result = instance.invalidCredentials();
        assertEquals(expResult, result);
    }

    /**
     * Test of doesNotHaveRole method, of class DemoiselleSecurityMessages.
     */
    @Test
    void test15() {
        String role = "Teste1";
        String expResult = "O Usu\u00e1rio n\u00e3o possui a role:Teste1";
        String result = instance.doesNotHaveRole(role);
        assertEquals(expResult, result);
    }

    /**
     * Test of doesNotHavePermission method, of class
     * DemoiselleSecurityMessages.
     */
    @Test
    void test16() {
        String operacao = "Teste1";
        String recurso = "Teste2";
        String expResult = "O Usu\u00e1rio n\u00e3o possui a permiss\u00e3o para executar a a\u00e7\u00e3o Teste1 no recurso Teste2";
        String result = instance.doesNotHavePermission(operacao, recurso);
        assertEquals(expResult, result);
    }

    /**
     * Test of cloneError method, of class DemoiselleSecurityMessages.
     */
    @Test
    void testCloneError() {
        String expResult = "Erro ao clonar";
        String result = instance.cloneError();
        assertEquals(expResult, result);
    }

}
