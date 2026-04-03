/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
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
class TokenManagerImplSlaveTest {

    @Inject
    private DemoiselleUser dml;

    @Inject
    private Token token;

    @Inject
    private TokenManager instance;

    private static final String VALID_TOKEN = "eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImV4cCI6MTAwMTQ4MjQ5NTI3MCwiYXVkIjoid2ViIiwianRpIjoiTmxvU0NFUnktd2xXdVhtaGZhVi1IUSIsImlhdCI6MTQ4MjQ5NTI3MSwibmJmIjoxNDgyNDk1MjExLCJpZGVudGl0eSI6IjEiLCJuYW1lIjoiVGVzdGUiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiTUFOQUdFUiJdLCJwZXJtaXNzaW9ucyI6eyJDYXRlZ29yaWEiOlsiQ29uc3VsdGFyIiwiQWx0ZXJhciIsIkluY2x1aXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIiwiRXhjbHVpciJdfSwicGFyYW1zIjp7ImZvbmUiOiI0MTM1OTM4MDAwIiwiZW5kZXJlY28iOiJydWEgY2FybG9zIHBpb2xpLCAxMzMiLCJlbWFpbCI6InVzZXJAZGVtb2lzZWxsZS5vcmcifX0.EV8L1OEFVMsuCgVSz3gyM2mJIEHczhHBvxSjTFGslHGKItlFtM32BUrzbzA9QECzSUkk-ITnUEtmm-ERTH529clymKX1-LGcboPSQlNAHv4SNRD5i8eJxjlCz_cMSTIdSZRSYOSJZHJHYf0kWEvo1vTthLGWcH_D--b9K_WYDR9hrVmljof46Dd4THXv5_VY9RJlYVHJ1bpIl69f0UDtVzDqfxNSTsBCm6tZXS40f9dh_qjEWATZeMJmjd_t2ZRzXDSLHHbJpLnNOGd2yOdp9H4tmGCxViguRa4Jck6C7cpMM6QIFB7ta67XzS4nl0NTqY64rNseKcyQS-TdAbPxAA";

    private static final String TAMPERED_TOKEN = "eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImV4cCI6MTAwMTQ4MjQ5NTI3MCwiYXVkIjoid2ViIiwianRpIjoiTmxvU0NFUnktd2xXdVhtaGZhVi1IUSIsImlhdCI6MTQ4MjQ5NTI3MSwibmJmIjoxNDgyNDk1MjExLCJpZGVudGl0eSI6IjEiLCJuYW1lIjoiVGVzdGUiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiTUFOQUdFUiJdLCJwZXJtaXNzaW9ucyI6eyJDYXRlZ29yaWEiOlsiQ29uc3VsdGFyIiwiQWx0ZXJhciIsIkluY2x1aXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIiwiRXhjbHVpciJdfSwicGFyYW1zIjp7ImZvbmUiOiI0MTM1OTM4MDAwIiwiZW5kZXJlY28iOiJydWEgY2FybG9zIHBpb2xpLCAxMzMiLCJlbWFpbCI6InVzZXJAZGVtb2lzZWxsZS5vcmcifX0.EV8L1OEFVMsuCgVSz3gyM2mJIEHczhHBvxSjTFGslHGKItlFtM32BUrzbzA9QECzSUkk-ITnUEtmm-ERTH529clymKX1-LGcboPSQlNAHv4SNRD5i8eJxjlCz_cMSTIdSZRSYOSJZHJHYf0kWEvo1vTthLGWcH_D--b9K_WYDR9hrVmljof46Dd4THXv5_VY9RJlYVHJ1bpIl69f0UDtVzDqfxNSTsBCm6tZXS40f9dh_qjEWATZeMJmjd_t2ZRzXDSLHHbJpLnNOGd2yOdp9H4tmGCxViguRa4Jck6C7cpMM6QIFB7ta67XzS4nl0NTqY64rNseKcyQS-TdAbPxaa";

    @Test
    void test21() {
        token.setKey(VALID_TOKEN);
        token.setType(TokenType.JWT);
        DemoiselleUser result = instance.getUser();
        assertEquals("1", result.getIdentity());
        assertEquals("Teste", result.getName());
    }

    @Test
    void test22() {
        token.setKey(VALID_TOKEN);
        token.setType(TokenType.JWT);
        boolean result = instance.validate();
        assertEquals(true, result);
    }

    @Test
    void test23() {
        token.setKey(TAMPERED_TOKEN);
        token.setType(TokenType.JWT);
        assertThrows(DemoiselleSecurityException.class, () -> {
            instance.getUser();
        });
    }

    @Test
    void test24() {
        token.setType(TokenType.JWT);
        boolean result = instance.validate();
        assertEquals(false, result);
    }

}

