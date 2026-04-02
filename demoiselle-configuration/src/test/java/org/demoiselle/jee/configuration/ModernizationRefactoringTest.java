/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.demoiselle.jee.configuration.annotation.ConfigurationSuppressLogger;
import org.demoiselle.jee.configuration.message.ConfigurationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying that Java 17+ modernization refactorings
 * preserve the original functional behavior.
 *
 * <p>Validates: Requirement 7.4</p>
 */
class ModernizationRefactoringTest {

    private ConfigurationLoader loader;

    /**
     * Stub ConfigurationMessage to avoid CDI dependency.
     */
    private static class StubConfigurationMessage implements ConfigurationMessage {
        @Override public String loadConfigurationClass(String name) { return "Loading " + name; }
        @Override public String configurationNameAttributeCantBeEmpty(String annotationName) { return "empty"; }
        @Override public String fileNotFound(String resource) { return "not found: " + resource; }
        @Override public String configurationDotAfterPrefix(String resource) { return "dot after prefix"; }
        @Override public String configurationKeyNotFoud(String keyNotFound) { return "key not found"; }
        @Override public String configurationFieldLoaded(String key, Object value) { return key + "=" + value; }
        @Override public String configurationNotConversion(String field, String type) { return "no conversion"; }
        @Override public String configurationGenericExtractionError(String typeField, String canonicalName) { return "extraction error"; }
        @Override public String configurationExtractorNotFound(String genericString, String valueExtractorClassName) { return "extractor not found"; }
        @Override public String ambigousStrategyResolution(String canonicalName, String string) { return "ambiguous"; }
        @Override public String configurationErrorGetValue(String fieldName, Object object) { return "get error"; }
        @Override public String configurationErrorSetValue(Object value, Object field, Object object) { return "set error"; }
        @Override public String failOnCreateApacheConfiguration(String message) { return "fail: " + message; }
        @Override public String configurationFieldSuppress(String key, String annotationName) { return "suppress"; }
        @Override public String cdiNotAlready() { return "CDI not ready"; }
        @Override public String profileResourceLoaded(String resource, String profile) { return "profile loaded: " + resource; }
        @Override public String profileResourceNotFound(String resource) { return "profile not found: " + resource; }
        @Override public String defaultValueConversionError(String fieldName, String value, String targetType) { return "conversion error"; }
    }

    @BeforeEach
    void setUp() throws Exception {
        loader = new ConfigurationLoader();

        // Inject stub message via reflection
        Field messageField = ConfigurationLoader.class.getDeclaredField("message");
        messageField.setAccessible(true);
        messageField.set(loader, new StubConfigurationMessage());
    }

    /**
     * Helper to invoke the private createConfiguration(ConfigurationType) method via reflection.
     */
    @SuppressWarnings("unchecked")
    private BasicConfigurationBuilder<? extends Configuration> invokeCreateConfiguration(ConfigurationType type) throws Exception {
        Method method = ConfigurationLoader.class.getDeclaredMethod("createConfiguration", ConfigurationType.class);
        method.setAccessible(true);
        return (BasicConfigurationBuilder<? extends Configuration>) method.invoke(loader, type);
    }

    // ── Switch expression tests ──

    @Test
    @DisplayName("createConfiguration(PROPERTIES) returns FileBasedConfigurationBuilder for PropertiesConfiguration")
    void createConfigurationPropertiesReturnsFileBasedBuilder() throws Exception {
        BasicConfigurationBuilder<? extends Configuration> builder = invokeCreateConfiguration(ConfigurationType.PROPERTIES);

        assertNotNull(builder, "Builder should not be null for PROPERTIES type");
        assertInstanceOf(FileBasedConfigurationBuilder.class, builder,
                "PROPERTIES should produce a FileBasedConfigurationBuilder");

        Configuration config = builder.getConfiguration();
        assertInstanceOf(PropertiesConfiguration.class, config,
                "PROPERTIES builder should produce a PropertiesConfiguration instance");
    }

    @Test
    @DisplayName("createConfiguration(XML) returns FileBasedConfigurationBuilder for XMLConfiguration")
    void createConfigurationXmlReturnsFileBasedBuilder() throws Exception {
        BasicConfigurationBuilder<? extends Configuration> builder = invokeCreateConfiguration(ConfigurationType.XML);

        assertNotNull(builder, "Builder should not be null for XML type");
        assertInstanceOf(FileBasedConfigurationBuilder.class, builder,
                "XML should produce a FileBasedConfigurationBuilder");

        Configuration config = builder.getConfiguration();
        assertInstanceOf(XMLConfiguration.class, config,
                "XML builder should produce an XMLConfiguration instance");
    }

    @Test
    @DisplayName("createConfiguration(SYSTEM) returns BasicConfigurationBuilder for SystemConfiguration")
    void createConfigurationSystemReturnsBasicBuilder() throws Exception {
        BasicConfigurationBuilder<? extends Configuration> builder = invokeCreateConfiguration(ConfigurationType.SYSTEM);

        assertNotNull(builder, "Builder should not be null for SYSTEM type");
        // SYSTEM should NOT be a FileBasedConfigurationBuilder
        assertFalse(builder instanceof FileBasedConfigurationBuilder,
                "SYSTEM should produce a BasicConfigurationBuilder, not FileBasedConfigurationBuilder");

        Configuration config = builder.getConfiguration();
        assertInstanceOf(SystemConfiguration.class, config,
                "SYSTEM builder should produce a SystemConfiguration instance");
    }

    // ── hasSuppressLogger() return type test ──

    @Test
    @DisplayName("hasSuppressLogger() returns boolean primitive (not Boolean wrapper)")
    void hasSuppressLoggerReturnsBooleanPrimitive() throws Exception {
        Method method = ConfigurationLoader.class.getDeclaredMethod("hasSuppressLogger");
        method.setAccessible(true);

        assertEquals(boolean.class, method.getReturnType(),
                "hasSuppressLogger() should return boolean primitive, not Boolean wrapper");
    }
}
