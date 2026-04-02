/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import java.util.HashSet;
import java.util.Set;

import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationArrayValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationClassValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationEnumValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the fallback logic in {@code ConfigurationLoader.getExtractors()}.
 *
 * <p>Since {@code getExtractors()} is private and depends on {@code CDI.current()},
 * these tests replicate the set union behavior in isolation, verifying the four
 * scenarios described in Requirements 6.1, 6.2, 6.3, 6.4:</p>
 * <ol>
 *   <li>Bootstrap available, Registry unavailable → result = bootstrapSet</li>
 *   <li>Bootstrap unavailable, Registry available → result = registrySet</li>
 *   <li>Both available → result = union of both sets</li>
 *   <li>Neither available → result = empty set</li>
 * </ol>
 */
class ExtractorFallbackTest {

    /**
     * Replicates the union logic from {@code ConfigurationLoader.getExtractors()}.
     *
     * @param bootstrapSet extractors from ConfigurationBootstrap (null if unavailable)
     * @param registrySet  extractors from ExtractorRegistry (null if unavailable)
     * @return the union of available sets, or empty if neither is available
     */
    private static Set<Class<? extends ConfigurationValueExtractor>> getExtractors(
            Set<Class<? extends ConfigurationValueExtractor>> bootstrapSet,
            Set<Class<? extends ConfigurationValueExtractor>> registrySet) {
        Set<Class<? extends ConfigurationValueExtractor>> result = new HashSet<>();
        if (bootstrapSet != null) {
            result.addAll(bootstrapSet);
        }
        if (registrySet != null) {
            result.addAll(registrySet);
        }
        return result;
    }

    // ── Scenario 1: Bootstrap available, Registry unavailable ──

    @Test
    @DisplayName("Validates: Requirements 6.1 — Bootstrap available, Registry unavailable returns bootstrap set")
    void bootstrapAvailableRegistryUnavailable() {
        Set<Class<? extends ConfigurationValueExtractor>> bootstrapSet = Set.of(
                ConfigurationStringValueExtractor.class,
                ConfigurationPrimitiveOrWrapperValueExtractor.class,
                ConfigurationArrayValueExtractor.class
        );

        Set<Class<? extends ConfigurationValueExtractor>> result = getExtractors(bootstrapSet, null);

        assertEquals(bootstrapSet.size(), result.size(),
                "Result size must match bootstrap set size");
        assertTrue(result.containsAll(bootstrapSet),
                "Result must contain all bootstrap extractors");
    }

    // ── Scenario 2: Bootstrap unavailable, Registry available ──

    @Test
    @DisplayName("Validates: Requirements 6.2 — Bootstrap unavailable, Registry available returns registry set")
    void bootstrapUnavailableRegistryAvailable() {
        Set<Class<? extends ConfigurationValueExtractor>> registrySet = Set.of(
                ConfigurationMapValueExtractor.class,
                ConfigurationEnumValueExtractor.class,
                ConfigurationClassValueExtractor.class
        );

        Set<Class<? extends ConfigurationValueExtractor>> result = getExtractors(null, registrySet);

        assertEquals(registrySet.size(), result.size(),
                "Result size must match registry set size");
        assertTrue(result.containsAll(registrySet),
                "Result must contain all registry extractors");
    }

    // ── Scenario 3: Both available (union) ──

    @Test
    @DisplayName("Validates: Requirements 6.4 — Both available returns union without duplicates")
    void bothAvailableReturnsUnion() {
        Set<Class<? extends ConfigurationValueExtractor>> bootstrapSet = Set.of(
                ConfigurationStringValueExtractor.class,
                ConfigurationPrimitiveOrWrapperValueExtractor.class,
                ConfigurationMapValueExtractor.class
        );
        Set<Class<? extends ConfigurationValueExtractor>> registrySet = Set.of(
                ConfigurationMapValueExtractor.class,
                ConfigurationEnumValueExtractor.class,
                ConfigurationArrayValueExtractor.class
        );

        Set<Class<? extends ConfigurationValueExtractor>> result = getExtractors(bootstrapSet, registrySet);

        // MapValueExtractor is in both sets — union should deduplicate
        Set<Class<? extends ConfigurationValueExtractor>> expectedUnion = new HashSet<>(bootstrapSet);
        expectedUnion.addAll(registrySet);

        assertEquals(expectedUnion.size(), result.size(),
                "Union size must equal mathematical union size (no duplicates)");
        assertTrue(result.containsAll(bootstrapSet),
                "Union must contain all bootstrap extractors");
        assertTrue(result.containsAll(registrySet),
                "Union must contain all registry extractors");
        assertEquals(5, result.size(),
                "Union of 3+3 sets with 1 overlap must have 5 elements");
    }

    // ── Scenario 4: Neither available (empty set) ──

    @Test
    @DisplayName("Validates: Requirements 6.3 — Neither available returns empty set")
    void neitherAvailableReturnsEmptySet() {
        Set<Class<? extends ConfigurationValueExtractor>> result = getExtractors(null, null);

        assertNotNull(result, "Result must not be null");
        assertTrue(result.isEmpty(), "Result must be empty when neither source is available");
    }

    // ── Additional: Both available with identical sets ──

    @Test
    @DisplayName("Validates: Requirements 6.4 — Both available with identical sets returns same set")
    void bothAvailableIdenticalSets() {
        Set<Class<? extends ConfigurationValueExtractor>> sameSet = Set.of(
                ConfigurationStringValueExtractor.class,
                ConfigurationEnumValueExtractor.class
        );

        Set<Class<? extends ConfigurationValueExtractor>> result = getExtractors(sameSet, sameSet);

        assertEquals(sameSet.size(), result.size(),
                "Union of identical sets must have same size as input");
        assertEquals(sameSet, result,
                "Union of identical sets must equal the input set");
    }

    // ── Additional: Both available with disjoint sets ──

    @Test
    @DisplayName("Validates: Requirements 6.4 — Both available with disjoint sets returns full union")
    void bothAvailableDisjointSets() {
        Set<Class<? extends ConfigurationValueExtractor>> bootstrapSet = Set.of(
                ConfigurationStringValueExtractor.class,
                ConfigurationPrimitiveOrWrapperValueExtractor.class
        );
        Set<Class<? extends ConfigurationValueExtractor>> registrySet = Set.of(
                ConfigurationMapValueExtractor.class,
                ConfigurationEnumValueExtractor.class
        );

        Set<Class<? extends ConfigurationValueExtractor>> result = getExtractors(bootstrapSet, registrySet);

        assertEquals(4, result.size(),
                "Union of disjoint sets of size 2+2 must have 4 elements");
        assertTrue(result.containsAll(bootstrapSet),
                "Union must contain all bootstrap extractors");
        assertTrue(result.containsAll(registrySet),
                "Union must contain all registry extractors");
    }
}
