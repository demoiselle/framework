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

import org.demoiselle.jee.configuration.message.ConfigurationMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for configuration profile support in ConfigurationLoader.
 *
 * <p>Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.7</p>
 *
 * Tests the private methods {@code resolveProfile()} and
 * {@code buildProfileResource()} via reflection, and verifies that
 * SYSTEM type ignores profile resolution.
 */
class ConfigurationProfileTest {

    private ConfigurationLoader loader;
    private Method resolveProfileMethod;
    private Method buildProfileResourceMethod;

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

        // Get private methods via reflection
        resolveProfileMethod = ConfigurationLoader.class.getDeclaredMethod("resolveProfile");
        resolveProfileMethod.setAccessible(true);

        buildProfileResourceMethod = ConfigurationLoader.class.getDeclaredMethod(
                "buildProfileResource", String.class, String.class, ConfigurationType.class);
        buildProfileResourceMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        // Always clean up the system property to avoid test pollution
        System.clearProperty("demoiselle.profile");
    }

    // ── resolveProfile() tests ──

    @Test
    @DisplayName("resolveProfile() returns system property when set")
    void resolveProfileReturnsSystemPropertyWhenSet() throws Exception {
        System.setProperty("demoiselle.profile", "dev");

        String result = (String) resolveProfileMethod.invoke(loader);

        assertEquals("dev", result);
    }

    @Test
    @DisplayName("resolveProfile() returns null when neither system property nor env var is set")
    void resolveProfileReturnsNullWhenNeitherIsSet() throws Exception {
        System.clearProperty("demoiselle.profile");

        // Note: We cannot easily unset DEMOISELLE_PROFILE env var in tests.
        // This test assumes DEMOISELLE_PROFILE is not set in the test environment.
        // If it is set, this test may return the env var value instead of null.
        String result = (String) resolveProfileMethod.invoke(loader);

        // If DEMOISELLE_PROFILE is not set in the environment, result should be null
        String envProfile = System.getenv("DEMOISELLE_PROFILE");
        if (envProfile == null || envProfile.isBlank()) {
            assertNull(result, "resolveProfile() should return null when no profile is defined");
        } else {
            // Env var is set in this environment — verify it's returned as fallback
            assertEquals(envProfile.strip(), result);
        }
    }

    @Test
    @DisplayName("resolveProfile() returns null when system property is blank")
    void resolveProfileReturnsNullWhenSystemPropertyIsBlank() throws Exception {
        System.setProperty("demoiselle.profile", "   ");

        String result = (String) resolveProfileMethod.invoke(loader);

        // Blank system property should fall through to env var
        String envProfile = System.getenv("DEMOISELLE_PROFILE");
        if (envProfile == null || envProfile.isBlank()) {
            assertNull(result, "resolveProfile() should return null when system property is blank and no env var");
        } else {
            assertEquals(envProfile.strip(), result);
        }
    }

    @Test
    @DisplayName("resolveProfile() strips whitespace from system property value")
    void resolveProfileStripsWhitespace() throws Exception {
        System.setProperty("demoiselle.profile", "  test  ");

        String result = (String) resolveProfileMethod.invoke(loader);

        assertEquals("test", result, "resolveProfile() should strip whitespace from the profile value");
    }

    @Test
    @DisplayName("resolveProfile() system property takes precedence over env var")
    void resolveProfileSystemPropertyTakesPrecedence() throws Exception {
        // Set system property — this should always win regardless of env var
        System.setProperty("demoiselle.profile", "staging");

        String result = (String) resolveProfileMethod.invoke(loader);

        assertEquals("staging", result,
                "System property should take precedence over environment variable");
    }

    // ── buildProfileResource() tests ──

    @Test
    @DisplayName("buildProfileResource builds correct name for .properties file")
    void buildProfileResourceForProperties() throws Exception {
        String result = (String) buildProfileResourceMethod.invoke(
                loader, "demoiselle.properties", "dev", ConfigurationType.PROPERTIES);

        assertEquals("demoiselle-dev.properties", result);
    }

    @Test
    @DisplayName("buildProfileResource builds correct name for .xml file")
    void buildProfileResourceForXml() throws Exception {
        String result = (String) buildProfileResourceMethod.invoke(
                loader, "demoiselle.xml", "test", ConfigurationType.XML);

        assertEquals("demoiselle-test.xml", result);
    }

    @Test
    @DisplayName("buildProfileResource handles base resource without extension")
    void buildProfileResourceWithoutExtension() throws Exception {
        String result = (String) buildProfileResourceMethod.invoke(
                loader, "myconfig", "prod", ConfigurationType.PROPERTIES);

        assertEquals("myconfig-prod.properties", result);
    }

    @Test
    @DisplayName("buildProfileResource handles custom resource name with extension")
    void buildProfileResourceCustomName() throws Exception {
        String result = (String) buildProfileResourceMethod.invoke(
                loader, "application.properties", "qa", ConfigurationType.PROPERTIES);

        assertEquals("application-qa.properties", result);
    }

    @Test
    @DisplayName("buildProfileResource handles XML resource without extension")
    void buildProfileResourceXmlWithoutExtension() throws Exception {
        String result = (String) buildProfileResourceMethod.invoke(
                loader, "settings", "staging", ConfigurationType.XML);

        assertEquals("settings-staging.xml", result);
    }

    // ── SYSTEM type ignores profile ──

    @Test
    @DisplayName("SYSTEM type should not attempt profile resolution in loadConfigurationType")
    void systemTypeIgnoresProfile() throws Exception {
        System.setProperty("demoiselle.profile", "dev");

        // Initialize the loader internals via reflection (simulating @PostConstruct)
        Method initMethod = ConfigurationLoader.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(loader);

        // Create a ConfigSourceMeta for SYSTEM type
        var sourceMeta = new ConfigurationLoader.ConfigSourceMeta(
                ConfigurationType.SYSTEM, "", "");

        // Invoke loadConfigurationType — for SYSTEM, it should NOT try to resolve profile
        // and should just load SystemConfiguration directly
        Method loadConfigTypeMethod = ConfigurationLoader.class.getDeclaredMethod(
                "loadConfigurationType", ConfigurationLoader.ConfigSourceMeta.class);
        loadConfigTypeMethod.setAccessible(true);

        // This should not throw — SYSTEM type creates a BasicConfigurationBuilder
        // (not FileBasedConfigurationBuilder), so the profile branch is skipped
        assertDoesNotThrow(() -> {
            try {
                loadConfigTypeMethod.invoke(loader, sourceMeta);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        }, "SYSTEM type should load without attempting profile resolution");

        // Verify configurations list was populated (SYSTEM config should be added)
        Field configurationsField = ConfigurationLoader.class.getDeclaredField("configurations");
        configurationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<org.apache.commons.configuration2.Configuration> configs =
                (java.util.List<org.apache.commons.configuration2.Configuration>) configurationsField.get(loader);

        assertNotNull(configs, "Configurations list should not be null");
        assertFalse(configs.isEmpty(), "SYSTEM type should add a SystemConfiguration to the list");
    }
}
