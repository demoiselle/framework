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
    void testShowErrorDetailsDefaultIsTrue() {
        boolean expResult = instance.isShowErrorDetails();
        assertEquals(true, expResult);
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
}
