/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

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
    org.demoiselle.jee.configuration.ConfigurationBootstrap.class,
    org.demoiselle.jee.core.message.MessageBundleExtension.class
})
@AddEnabledInterceptors(org.demoiselle.jee.configuration.ConfigurationInterceptor.class)
@AddBeanClasses({
    DemoiselleRestConfig.class,
    ConfigurationLoader.class,
    org.demoiselle.jee.configuration.message.ConfigurationMessage.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor.class
})
class DemoiselleRestConfigTest {

    @Inject
    private DemoiselleRestConfig instance;

    /**
     * Test of isErrorDetails method, of class DemoiselleRestConfig.
     */
    @Test
    void testShowErrorDetailsDefaultIsFalse() {
        boolean expResult = instance.isShowErrorDetails();
        assertEquals(false, expResult);
    }

    /**
     * Test of setShowErrorDetails method, of class DemoiselleRestConfig.
     */
    @Test
    void testSetShowErrorDetails() {
        instance.setShowErrorDetails(false);
        boolean expResult = instance.isShowErrorDetails();
        assertEquals(false, expResult);
    }

    @Test
    void testErrorFormatDefaultIsLegacy() {
        assertEquals("legacy", instance.getErrorFormat());
    }

    @Test
    void testSetErrorFormatRfc9457() {
        instance.setErrorFormat("rfc9457");
        assertEquals("rfc9457", instance.getErrorFormat());
    }

    @Test
    void testSetErrorFormatLegacy() {
        instance.setErrorFormat("legacy");
        assertEquals("legacy", instance.getErrorFormat());
    }

    @Test
    void testSetErrorFormatInvalidNormalizesToLegacy() {
        instance.setErrorFormat("unknown");
        assertEquals("legacy", instance.getErrorFormat());
    }

    @Test
    void testSetErrorFormatNullNormalizesToLegacy() {
        instance.setErrorFormat(null);
        assertEquals("legacy", instance.getErrorFormat());
    }

    @Test
    void testIsRfc9457ReturnsTrueWhenRfc9457() {
        instance.setErrorFormat("rfc9457");
        assertTrue(instance.isRfc9457());
    }

    @Test
    void testIsRfc9457ReturnsFalseWhenLegacy() {
        instance.setErrorFormat("legacy");
        assertFalse(instance.isRfc9457());
    }

    @Test
    void testIsRfc9457ReturnsFalseByDefault() {
        assertFalse(new DemoiselleRestConfig().isRfc9457());
    }

    @Test
    void testSecurityHeadersEnabledDefaultIsTrue() {
        assertTrue(new DemoiselleRestConfig().isSecurityHeadersEnabled());
    }

    @Test
    void testExposeFrameworkVersionDefaultIsFalse() {
        assertFalse(new DemoiselleRestConfig().isExposeFrameworkVersion());
    }

    @Test
    void testSecurityHeadersDefaults() {
        DemoiselleRestConfig config = new DemoiselleRestConfig();
        java.util.Map<String, String> headers = config.getSecurityHeaders();
        assertEquals("nosniff", headers.get("X-Content-Type-Options"));
        assertEquals("DENY", headers.get("X-Frame-Options"));
        assertEquals("no-referrer", headers.get("Referrer-Policy"));
        assertEquals("camera=(), microphone=(), geolocation=()", headers.get("Permissions-Policy"));
    }

    @Test
    void testSecurityHeadersHasNoHstsOrCspByDefault() {
        DemoiselleRestConfig config = new DemoiselleRestConfig();
        java.util.Map<String, String> headers = config.getSecurityHeaders();
        assertFalse(headers.containsKey("Strict-Transport-Security"));
        assertFalse(headers.containsKey("Content-Security-Policy"));
    }

    @Test
    void testSetSecurityHeadersNullResetsToDefaults() {
        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setSecurityHeaders(null);
        assertEquals("nosniff", config.getSecurityHeaders().get("X-Content-Type-Options"));
    }

    @Test
    void testSetSecurityHeadersOverridesMap() {
        DemoiselleRestConfig config = new DemoiselleRestConfig();
        java.util.Map<String, String> custom = new java.util.HashMap<>();
        custom.put("X-Frame-Options", "SAMEORIGIN");
        config.setSecurityHeaders(custom);
        assertEquals("SAMEORIGIN", config.getSecurityHeaders().get("X-Frame-Options"));
        assertFalse(config.getSecurityHeaders().containsKey("X-Content-Type-Options"));
    }

    @Test
    void testSetSecurityHeadersEnabledFlag() {
        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setSecurityHeadersEnabled(false);
        assertFalse(config.isSecurityHeadersEnabled());
    }

    @Test
    void testSetExposeFrameworkVersionFlag() {
        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setExposeFrameworkVersion(true);
        assertTrue(config.isExposeFrameworkVersion());
    }
}
