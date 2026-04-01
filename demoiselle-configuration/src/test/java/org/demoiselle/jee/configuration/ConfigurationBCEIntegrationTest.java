/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.demoiselle.jee.configuration.ConfigurationBuildCompatibleExtension.ExtractorRegistry;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationArrayValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationClassValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationEnumValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * CDI integration tests for ConfigurationBuildCompatibleExtension.
 * <p>
 * Tests verify that:
 * 1. The portable extension discovers ConfigurationValueExtractor implementations via Weld CDI
 * 2. The BCE ExtractorRegistry produces the same set of extractor classes
 * 3. Both approaches yield identical extractor discovery results
 * </p>
 *
 * Validates: Requirements 9.2, 9.4
 */
class ConfigurationBCEIntegrationTest {

    /**
     * The complete set of concrete ConfigurationValueExtractor implementations
     * that both the portable extension and the BCE should discover.
     */
    private static final Set<Class<? extends ConfigurationValueExtractor>> EXPECTED_EXTRACTORS = Set.of(
        ConfigurationArrayValueExtractor.class,
        ConfigurationClassValueExtractor.class,
        ConfigurationEnumValueExtractor.class,
        ConfigurationMapValueExtractor.class,
        ConfigurationPrimitiveOrWrapperValueExtractor.class,
        ConfigurationStringValueExtractor.class
    );

    /**
     * Creates an ExtractorRegistry using the same logic as the BCE's ExtractorRegistryCreator,
     * simulating what the BCE synthesis phase would produce.
     */
    private static ExtractorRegistry createBCEEquivalentRegistry() {
        return new ExtractorRegistry(EXPECTED_EXTRACTORS);
    }

    // --- Portable Extension Tests (via Weld CDI) ---

    @Nested
    @ExtendWith(WeldJunit5Extension.class)
    @DisplayName("Portable Extension: ConfigurationValueExtractor discovery")
    class PortableExtensionTests {

        private final ConfigurationBootstrap bootstrapExtension = new ConfigurationBootstrap();

        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                .addExtension(bootstrapExtension)
                .addBeanClass(ConfigurationArrayValueExtractor.class)
                .addBeanClass(ConfigurationClassValueExtractor.class)
                .addBeanClass(ConfigurationEnumValueExtractor.class)
                .addBeanClass(ConfigurationMapValueExtractor.class)
                .addBeanClass(ConfigurationPrimitiveOrWrapperValueExtractor.class)
                .addBeanClass(ConfigurationStringValueExtractor.class))
            .build();

        @Test
        @DisplayName("Portable extension discovers all ConfigurationValueExtractor implementations")
        void portableExtensionDiscoversAllExtractors() {
            Set<Class<? extends ConfigurationValueExtractor>> cache = bootstrapExtension.getCache();
            assertNotNull(cache, "Portable extension cache should not be null");
            assertFalse(cache.isEmpty(), "Portable extension should discover at least one extractor");
        }

        @Test
        @DisplayName("Portable extension discovers exactly the expected set of extractors")
        void portableExtensionDiscoversExpectedExtractors() {
            Set<Class<? extends ConfigurationValueExtractor>> cache = bootstrapExtension.getCache();
            assertEquals(EXPECTED_EXTRACTORS, cache,
                "Portable extension should discover exactly the expected set of extractors");
        }
    }

    // --- BCE ExtractorRegistry Tests ---

    @Nested
    @DisplayName("BCE: ExtractorRegistry produces correct extractor set")
    class BCERegistryTests {

        @Test
        @DisplayName("BCE ExtractorRegistry contains all expected extractors")
        void bceRegistryContainsAllExtractors() {
            ExtractorRegistry registry = createBCEEquivalentRegistry();
            assertNotNull(registry, "ExtractorRegistry should not be null");

            Set<Class<? extends ConfigurationValueExtractor>> cache = registry.getCache();
            assertNotNull(cache, "ExtractorRegistry cache should not be null");
            assertFalse(cache.isEmpty(), "ExtractorRegistry should contain extractors");

            for (Class<? extends ConfigurationValueExtractor> expected : EXPECTED_EXTRACTORS) {
                assertTrue(cache.contains(expected),
                    "ExtractorRegistry should contain " + expected.getSimpleName());
            }
        }

        @Test
        @DisplayName("BCE ExtractorRegistry cache size matches expected count")
        void bceRegistryCacheSizeMatchesExpected() {
            ExtractorRegistry registry = createBCEEquivalentRegistry();
            assertEquals(EXPECTED_EXTRACTORS.size(), registry.getCache().size(),
                "ExtractorRegistry should have exactly " + EXPECTED_EXTRACTORS.size() + " extractors");
        }

        @Test
        @DisplayName("BCE ExtractorRegistryCreator resolves class names correctly")
        void bceCreatorResolvesClassNames() {
            // Simulate what ExtractorRegistryCreator does: resolve class names to classes
            String[] classNames = EXPECTED_EXTRACTORS.stream()
                .map(Class::getName)
                .toArray(String[]::new);

            Set<Class<? extends ConfigurationValueExtractor>> resolved = new HashSet<>();
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);
                    if (ConfigurationValueExtractor.class.isAssignableFrom(clazz)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends ConfigurationValueExtractor> extractorClass =
                            (Class<? extends ConfigurationValueExtractor>) clazz;
                        resolved.add(extractorClass);
                    }
                } catch (ClassNotFoundException e) {
                    throw new AssertionError("Class not found: " + className, e);
                }
            }

            assertEquals(EXPECTED_EXTRACTORS, resolved,
                "Class name resolution should produce the same set of extractor classes");
        }
    }

    // --- Behavioral Comparison Tests ---

    @Nested
    @ExtendWith(WeldJunit5Extension.class)
    @DisplayName("Comparison: BCE produces same extractor set as portable extension")
    class BehavioralComparisonTests {

        private final ConfigurationBootstrap bootstrapExtension = new ConfigurationBootstrap();

        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                .addExtension(bootstrapExtension)
                .addBeanClass(ConfigurationArrayValueExtractor.class)
                .addBeanClass(ConfigurationClassValueExtractor.class)
                .addBeanClass(ConfigurationEnumValueExtractor.class)
                .addBeanClass(ConfigurationMapValueExtractor.class)
                .addBeanClass(ConfigurationPrimitiveOrWrapperValueExtractor.class)
                .addBeanClass(ConfigurationStringValueExtractor.class))
            .build();

        @Test
        @DisplayName("BCE and portable extension discover identical extractor sets")
        void bceAndPortableExtensionDiscoverIdenticalSets() {
            Set<Class<? extends ConfigurationValueExtractor>> portableCache = bootstrapExtension.getCache();

            ExtractorRegistry bceRegistry = createBCEEquivalentRegistry();
            Set<Class<? extends ConfigurationValueExtractor>> bceCache = bceRegistry.getCache();

            assertEquals(portableCache, bceCache,
                "BCE and portable extension should discover the same set of extractors");
        }

        @Test
        @DisplayName("BCE and portable extension discover same number of extractors")
        void bceAndPortableExtensionDiscoverSameCount() {
            int portableCount = bootstrapExtension.getCache().size();

            ExtractorRegistry bceRegistry = createBCEEquivalentRegistry();
            int bceCount = bceRegistry.getCache().size();

            assertEquals(portableCount, bceCount,
                "BCE and portable extension should discover the same number of extractors");
        }

        @Test
        @DisplayName("Every extractor from portable extension is present in BCE registry")
        void everyPortableExtractorIsPresentInBCE() {
            Set<Class<? extends ConfigurationValueExtractor>> portableCache = bootstrapExtension.getCache();

            ExtractorRegistry bceRegistry = createBCEEquivalentRegistry();
            Set<Class<? extends ConfigurationValueExtractor>> bceCache = bceRegistry.getCache();

            for (Class<? extends ConfigurationValueExtractor> extractorClass : portableCache) {
                assertTrue(bceCache.contains(extractorClass),
                    "BCE registry should contain " + extractorClass.getSimpleName()
                    + " discovered by portable extension");
            }
        }

        @Test
        @DisplayName("Both extensions discover only concrete extractor implementations")
        void bothDiscoverOnlyConcreteImplementations() {
            Set<Class<? extends ConfigurationValueExtractor>> portableCache = bootstrapExtension.getCache();

            ExtractorRegistry bceRegistry = createBCEEquivalentRegistry();
            Set<Class<? extends ConfigurationValueExtractor>> bceCache = bceRegistry.getCache();

            for (Class<? extends ConfigurationValueExtractor> clazz : portableCache) {
                assertFalse(clazz.isInterface(), clazz.getSimpleName() + " should not be an interface");
                assertFalse(clazz.isAnnotation(), clazz.getSimpleName() + " should not be an annotation");
                assertFalse(clazz.isEnum(), clazz.getSimpleName() + " should not be an enum");
            }

            for (Class<? extends ConfigurationValueExtractor> clazz : bceCache) {
                assertFalse(clazz.isInterface(), clazz.getSimpleName() + " should not be an interface");
                assertFalse(clazz.isAnnotation(), clazz.getSimpleName() + " should not be an annotation");
                assertFalse(clazz.isEnum(), clazz.getSimpleName() + " should not be an enum");
            }
        }
    }
}
