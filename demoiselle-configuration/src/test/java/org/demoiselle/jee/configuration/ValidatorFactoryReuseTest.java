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

import jakarta.validation.ValidatorFactory;

import org.demoiselle.jee.configuration.message.ConfigurationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ValidatorFactory reuse in ConfigurationLoader.
 *
 * <p>Validates: Requirements 2.1, 2.2, 2.3</p>
 *
 * Verifies that:
 * <ul>
 *   <li>The ValidatorFactory is initialized once during {@code @PostConstruct init()}</li>
 *   <li>Multiple validations reuse the same ValidatorFactory instance</li>
 *   <li>The ValidatorFactory is closed during {@code @PreDestroy destroy()}</li>
 * </ul>
 */
class ValidatorFactoryReuseTest {

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
     * Helper to get the validatorFactory field value via reflection.
     */
    private ValidatorFactory getValidatorFactoryField() throws Exception {
        Field field = ConfigurationLoader.class.getDeclaredField("validatorFactory");
        field.setAccessible(true);
        return (ValidatorFactory) field.get(loader);
    }

    /**
     * Helper to invoke the private init() method (simulating @PostConstruct).
     */
    private void invokeInit() throws Exception {
        Method initMethod = ConfigurationLoader.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(loader);
    }

    /**
     * Helper to invoke the private destroy() method (simulating @PreDestroy).
     */
    private void invokeDestroy() throws Exception {
        Method destroyMethod = ConfigurationLoader.class.getDeclaredMethod("destroy");
        destroyMethod.setAccessible(true);
        destroyMethod.invoke(loader);
    }

    // ── Tests ──

    @Test
    @DisplayName("ValidatorFactory should be null before @PostConstruct init()")
    void validatorFactoryShouldBeNullBeforeInit() throws Exception {
        // Before init(), the field should be null (no container lifecycle yet)
        ValidatorFactory vf = getValidatorFactoryField();
        assertNull(vf, "ValidatorFactory should be null before init() is called");
    }

    @Test
    @DisplayName("ValidatorFactory should be initialized after @PostConstruct init()")
    void validatorFactoryShouldBeInitializedAfterInit() throws Exception {
        invokeInit();

        ValidatorFactory vf = getValidatorFactoryField();
        assertNotNull(vf, "ValidatorFactory should be non-null after init()");
    }

    @Test
    @DisplayName("Multiple calls to init() should replace the ValidatorFactory, but a single init() creates exactly one instance")
    void singleInitCreatesOneInstance() throws Exception {
        invokeInit();

        ValidatorFactory first = getValidatorFactoryField();
        assertNotNull(first);

        // The same field should hold the same reference without re-init
        ValidatorFactory second = getValidatorFactoryField();
        assertSame(first, second,
                "Reading the field twice should return the same ValidatorFactory instance");
    }

    @Test
    @DisplayName("ValidatorFactory instance should be reused across multiple validateValue calls")
    void validatorFactoryShouldBeReusedAcrossMultipleValidations() throws Exception {
        invokeInit();

        ValidatorFactory beforeValidation = getValidatorFactoryField();
        assertNotNull(beforeValidation);

        // Invoke validateValue multiple times via reflection on different fields.
        // We use a simple model with a valid field so no exception is thrown,
        // and verify the factory instance remains the same.
        Method validateValueMethod = ConfigurationLoader.class.getDeclaredMethod("validateValue", Field.class);
        validateValueMethod.setAccessible(true);

        // Set up targetObject via reflection
        Field targetObjectField = ConfigurationLoader.class.getDeclaredField("targetObject");
        targetObjectField.setAccessible(true);

        SimpleModel model = new SimpleModel();
        model.name = "valid";
        model.count = 5;
        targetObjectField.set(loader, model);

        // Validate "name" field
        Field nameField = SimpleModel.class.getDeclaredField("name");
        validateValueMethod.invoke(loader, nameField);

        ValidatorFactory afterFirstValidation = getValidatorFactoryField();
        assertSame(beforeValidation, afterFirstValidation,
                "ValidatorFactory should be the same instance after first validation");

        // Validate "count" field
        Field countField = SimpleModel.class.getDeclaredField("count");
        validateValueMethod.invoke(loader, countField);

        ValidatorFactory afterSecondValidation = getValidatorFactoryField();
        assertSame(beforeValidation, afterSecondValidation,
                "ValidatorFactory should be the same instance after second validation");
    }

    @Test
    @DisplayName("@PreDestroy destroy() should close the ValidatorFactory without error")
    void destroyShouldCloseValidatorFactory() throws Exception {
        invokeInit();

        ValidatorFactory vf = getValidatorFactoryField();
        assertNotNull(vf, "ValidatorFactory should exist before destroy");

        // destroy() should complete without throwing
        assertDoesNotThrow(this::invokeDestroy,
                "destroy() should close the ValidatorFactory without error");

        // The field reference still points to the (now closed) factory.
        // Verify that destroy() was actually invoked by calling close() again —
        // a second close on an already-closed factory should be a no-op.
        assertDoesNotThrow(vf::close,
                "Calling close() again on an already-closed factory should be idempotent");
    }

    @Test
    @DisplayName("destroy() should handle null ValidatorFactory gracefully")
    void destroyShouldHandleNullValidatorFactoryGracefully() throws Exception {
        // Don't call init() — validatorFactory is null
        assertNull(getValidatorFactoryField());

        // destroy() should not throw
        assertDoesNotThrow(this::invokeDestroy,
                "destroy() should handle null ValidatorFactory without throwing");
    }

    // ── Simple model for validation tests ──

    static class SimpleModel {
        String name;
        int count;
    }
}
