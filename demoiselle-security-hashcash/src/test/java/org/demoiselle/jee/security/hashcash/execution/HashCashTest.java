/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.execution;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.configuration.ConfigurationLoader;
import org.demoiselle.jee.security.hashcash.DemoiselleSecurityHashCashConfig;
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
    Generator.class,
    DemoiselleSecurityHashCashConfig.class,
    ConfigurationLoader.class,
    org.demoiselle.jee.configuration.message.ConfigurationMessage.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor.class
})
class HashCashTest {

    @Inject
    private Generator gera;

    @Test
    void testMintCash_String_int() throws Exception {
        String token = gera.token();
        assertNotNull(token);
    }

}
