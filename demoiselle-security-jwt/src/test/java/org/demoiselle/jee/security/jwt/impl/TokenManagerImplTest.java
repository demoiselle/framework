/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.impl.DemoiselleUserImpl;
import org.demoiselle.jee.security.jwt.test.TestTokenProducer;
import org.demoiselle.jee.security.message.DemoiselleSecurityJWTMessages;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;
import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddEnabledInterceptors;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

/**
 *
 * @author SERPRO
 */
@EnableAutoWeld
@ActivateScopes(RequestScoped.class)
@AddExtensions({
    org.demoiselle.jee.configuration.ConfigurationBootstrap.class,
    org.demoiselle.jee.core.message.MessageBundleExtension.class
})
@AddEnabledInterceptors(org.demoiselle.jee.configuration.ConfigurationInterceptor.class)
@AddBeanClasses({
    TokenManagerImpl.class,
    JwtTokenValidatorImpl.class,
    KeyPairHolder.class,
    KeyRotationManager.class,
    TokenBlacklist.class,
    DemoiselleSecurityJWTConfig.class,
    DemoiselleUserImpl.class,
    TestTokenProducer.class,
    DemoiselleSecurityJWTMessages.class,
    DemoiselleSecurityMessages.class,
    org.demoiselle.jee.configuration.ConfigurationLoader.class,
    org.demoiselle.jee.configuration.message.ConfigurationMessage.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor.class
})
class TokenManagerImplTest {

    @Inject
    private DemoiselleUser dml;

    @Inject
    private Token token;

    @Inject
    private TokenManager instance;

    @Test
    void test20() {
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
        assertNotEquals("", token.getKey());
    }

    @Test
    void test21() {
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
        String localtoken = token.getKey();

        // Reset and re-read
        token.setKey(localtoken);
        token.setType(TokenType.JWT);
        DemoiselleUser result = instance.getUser();
        assertEquals(dml.getIdentity(), result.getIdentity());
        assertEquals(dml.getName(), result.getName());
    }

    @Test
    void test22() {
        token.setKey("");
        token.setType(TokenType.JWT);
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Produto", "Excluir");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        String localtoken = token.getKey();

        token.setKey(localtoken);
        token.setType(TokenType.JWT);
        boolean result = instance.validate();
        assertEquals(true, result);
    }

    @Test
    void test24() {
        token.setKey("");
        token.setType(TokenType.JWT);
        boolean result = instance.validate();
        assertEquals(false, result);
    }

    @Test
    void test25() {
        instance.removeUser(dml);
        assertNull(token.getKey());
    }

}

