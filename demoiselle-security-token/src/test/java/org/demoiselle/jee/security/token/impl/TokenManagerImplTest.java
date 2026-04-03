/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.token.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;
import org.demoiselle.jee.security.token.test.TestTokenProducer;
import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

/**
 *
 * @author SERPRO
 */
@EnableAutoWeld
@ActivateScopes(RequestScoped.class)
@AddExtensions(org.demoiselle.jee.core.message.MessageBundleExtension.class)
@AddBeanClasses({
    TokenManagerImpl.class,
    DemoiselleUserImpl.class,
    TestTokenProducer.class,
    DemoiselleSecurityMessages.class
})
class TokenManagerImplTest {

    @Inject
    private DemoiselleUser dml;

    @Inject
    private Token token;

    @Inject
    private TokenManager instance;

    /**
     * Test of setUser method, of class TokenManagerImpl.
     */
    @Test
    void test20() {
        token.setKey("");
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        assertNotEquals("", token.getKey());
    }

    /**
     * Test of getUser method, of class TokenManagerImpl.
     */
    @Test
    void test21() {
        token.setKey("");
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        String localtoken = token.getKey();

        token.setKey(localtoken);
        String expResult = dml.getIdentity();
        String result = instance.getUser().getIdentity();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUser for existing user.
     */
    @Test
    void test22() {
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        String localtoken = token.getKey();
        instance.setUser(dml);
        assertEquals(localtoken, token.getKey());
    }

    /**
     * Test of validate method, of class TokenManagerImpl.
     */
    @Test
    void test23() {
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        instance.setUser(dml);
        boolean result = instance.validate();
        assertEquals(true, result);
    }

    /**
     * Test of validate method with empty token.
     */
    @Test
    void test25() {
        token.setKey("");
        boolean result = instance.validate();
        assertEquals(false, result);
    }

    /**
     * Test of removeToken method.
     */
    @Test
    void test26() {
        dml.setName("Teste");
        dml.setIdentity("1");
        instance.setUser(dml);
        ((TokenManagerImpl) instance).removeToken();
        assertNull(token.getKey());
    }

    /**
     * Test of removeUser method.
     */
    @Test
    void test27() {
        dml.setName("Teste2");
        dml.setIdentity("2");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        ((TokenManagerImpl) instance).removeUser(dml);
        DemoiselleUser result = instance.getUser();
        assertNull(result);
    }

}
