/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.inject.Inject;

import org.demoiselle.jee.configuration.ConfigurationBootstrap;
import org.demoiselle.jee.configuration.ConfigurationLoader;
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
@AddExtensions({
    ConfigurationBootstrap.class,
    org.demoiselle.jee.core.message.MessageBundleExtension.class
})
@AddEnabledInterceptors(org.demoiselle.jee.configuration.ConfigurationInterceptor.class)
@AddBeanClasses({
    DemoiselleSecurityConfig.class,
    ConfigurationLoader.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor.class,
    org.demoiselle.jee.configuration.message.ConfigurationMessage.class
})
class DemoiselleSecurityConfigTest {

    @Inject
    private DemoiselleSecurityConfig instance;

    /**
     * Test of isCorsEnabled method, of class DemoiselleSecurityConfig.
     */
    @Test
    void test11() {
        boolean expResult = true;
        boolean result = instance.isCorsEnabled();
        assertEquals(expResult, result);
    }

    @Test
    void corsAllowedOriginsDefaultsToWildcard() {
        List<String> origins = instance.getCorsAllowedOrigins();
        assertEquals(List.of("*"), origins);
    }

    @Test
    void corsAllowedMethodsDefaultsToStandardMethods() {
        List<String> methods = instance.getCorsAllowedMethods();
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"), methods);
    }

    @Test
    void corsAllowedHeadersDefaultsToContentTypeAndAuthorization() {
        List<String> headers = instance.getCorsAllowedHeaders();
        assertEquals(List.of("Content-Type", "Authorization"), headers);
    }

    @Test
    void corsMaxAgeDefaultsTo3600() {
        assertEquals(3600, instance.getCorsMaxAge());
    }

    @Test
    void corsMaxAgeReturns3600WhenFieldIsZero() throws Exception {
        DemoiselleSecurityConfig config = new DemoiselleSecurityConfig();
        // Use reflection to set corsMaxAge to 0
        var field = DemoiselleSecurityConfig.class.getDeclaredField("corsMaxAge");
        field.setAccessible(true);
        field.setInt(config, 0);
        assertEquals(3600, config.getCorsMaxAge());
    }

    @Test
    void corsMaxAgeReturns3600WhenFieldIsNegative() throws Exception {
        DemoiselleSecurityConfig config = new DemoiselleSecurityConfig();
        var field = DemoiselleSecurityConfig.class.getDeclaredField("corsMaxAge");
        field.setAccessible(true);
        field.setInt(config, -10);
        assertEquals(3600, config.getCorsMaxAge());
    }

    @Test
    void corsMaxAgeReturnsConfiguredValueWhenPositive() throws Exception {
        DemoiselleSecurityConfig config = new DemoiselleSecurityConfig();
        var field = DemoiselleSecurityConfig.class.getDeclaredField("corsMaxAge");
        field.setAccessible(true);
        field.setInt(config, 7200);
        assertEquals(7200, config.getCorsMaxAge());
    }

}
