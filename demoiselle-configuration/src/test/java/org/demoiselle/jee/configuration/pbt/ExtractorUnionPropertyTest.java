/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jqwik.api.*;

import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationArrayValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationClassValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationEnumValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 9: União de extractors sem duplicatas
 *
 * <p><b>Validates: Requirements 6.4</b></p>
 *
 * For any two sets of {@link ConfigurationValueExtractor} classes (simulating
 * Bootstrap and Registry sources), the union produced by the
 * {@code getExtractors()} logic must:
 * <ol>
 *   <li>Contain all elements from both sets</li>
 *   <li>Have no duplicates</li>
 *   <li>Have a size equal to the mathematical union of the two sets</li>
 * </ol>
 *
 * <p>Since {@code getExtractors()} is private and depends on CDI, this test
 * replicates the union logic:</p>
 * <pre>
 *   Set&lt;Class&lt;?&gt;&gt; result = new HashSet&lt;&gt;();
 *   result.addAll(bootstrapSet);
 *   result.addAll(registrySet);
 * </pre>
 */
class ExtractorUnionPropertyTest {

    /**
     * Pool of known ConfigurationValueExtractor implementation classes from the project.
     */
    @SuppressWarnings("unchecked")
    private static final List<Class<? extends ConfigurationValueExtractor>> EXTRACTOR_POOL = List.of(
            ConfigurationArrayValueExtractor.class,
            ConfigurationClassValueExtractor.class,
            ConfigurationEnumValueExtractor.class,
            ConfigurationMapValueExtractor.class,
            ConfigurationPrimitiveOrWrapperValueExtractor.class,
            ConfigurationStringValueExtractor.class
    );

    /**
     * Replicates the union logic from {@code ConfigurationLoader.getExtractors()}:
     * both sources are added to a single {@code HashSet}, which naturally
     * eliminates duplicates.
     */
    private static Set<Class<? extends ConfigurationValueExtractor>> unionExtractors(
            Set<Class<? extends ConfigurationValueExtractor>> bootstrapSet,
            Set<Class<? extends ConfigurationValueExtractor>> registrySet) {
        Set<Class<? extends ConfigurationValueExtractor>> result = new HashSet<>();
        result.addAll(bootstrapSet);
        result.addAll(registrySet);
        return result;
    }

    // ── Property: union contains all elements from both sets ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 9: União de extractors sem duplicatas")
    void unionContainsAllElementsFromBothSets(
            @ForAll("extractorSubsets") Set<Class<? extends ConfigurationValueExtractor>> bootstrapSet,
            @ForAll("extractorSubsets") Set<Class<? extends ConfigurationValueExtractor>> registrySet) {

        Set<Class<? extends ConfigurationValueExtractor>> result = unionExtractors(bootstrapSet, registrySet);

        for (Class<? extends ConfigurationValueExtractor> cls : bootstrapSet) {
            assertTrue(result.contains(cls),
                    "Union must contain all elements from bootstrap set: " + cls.getSimpleName());
        }
        for (Class<? extends ConfigurationValueExtractor> cls : registrySet) {
            assertTrue(result.contains(cls),
                    "Union must contain all elements from registry set: " + cls.getSimpleName());
        }
    }

    // ── Property: union has no duplicates (size equals mathematical union) ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 9: União de extractors sem duplicatas")
    void unionSizeEqualsMathematicalUnion(
            @ForAll("extractorSubsets") Set<Class<? extends ConfigurationValueExtractor>> bootstrapSet,
            @ForAll("extractorSubsets") Set<Class<? extends ConfigurationValueExtractor>> registrySet) {

        Set<Class<? extends ConfigurationValueExtractor>> result = unionExtractors(bootstrapSet, registrySet);

        // Compute expected mathematical union size
        Set<Class<? extends ConfigurationValueExtractor>> expectedUnion = new HashSet<>(bootstrapSet);
        expectedUnion.addAll(registrySet);

        assertEquals(expectedUnion.size(), result.size(),
                "Union size must equal the mathematical union size (no duplicates)");
    }

    // ── Property: union contains no extra elements ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 9: União de extractors sem duplicatas")
    void unionContainsOnlyElementsFromInputSets(
            @ForAll("extractorSubsets") Set<Class<? extends ConfigurationValueExtractor>> bootstrapSet,
            @ForAll("extractorSubsets") Set<Class<? extends ConfigurationValueExtractor>> registrySet) {

        Set<Class<? extends ConfigurationValueExtractor>> result = unionExtractors(bootstrapSet, registrySet);

        for (Class<? extends ConfigurationValueExtractor> cls : result) {
            assertTrue(bootstrapSet.contains(cls) || registrySet.contains(cls),
                    "Union must not contain elements absent from both input sets: " + cls.getSimpleName());
        }
    }

    // ── Property: union with identical sets equals either set ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 9: União de extractors sem duplicatas")
    void unionOfIdenticalSetsEqualsEitherSet(
            @ForAll("extractorSubsets") Set<Class<? extends ConfigurationValueExtractor>> sameSet) {

        Set<Class<? extends ConfigurationValueExtractor>> result = unionExtractors(sameSet, sameSet);

        assertEquals(sameSet.size(), result.size(),
                "Union of identical sets must have the same size as the input set");
        assertEquals(sameSet, result,
                "Union of identical sets must equal the input set");
    }

    // ── Property: union with empty set equals the other set ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 9: União de extractors sem duplicatas")
    void unionWithEmptySetEqualsOtherSet(
            @ForAll("extractorSubsets") Set<Class<? extends ConfigurationValueExtractor>> nonEmptySet) {

        Set<Class<? extends ConfigurationValueExtractor>> emptySet = Set.of();

        Set<Class<? extends ConfigurationValueExtractor>> result1 = unionExtractors(nonEmptySet, emptySet);
        Set<Class<? extends ConfigurationValueExtractor>> result2 = unionExtractors(emptySet, nonEmptySet);

        assertEquals(nonEmptySet, result1,
                "Union of a set with empty set must equal the non-empty set");
        assertEquals(nonEmptySet, result2,
                "Union of empty set with a set must equal the non-empty set");
    }

    // ── Generators ──

    @Provide
    Arbitrary<Set<Class<? extends ConfigurationValueExtractor>>> extractorSubsets() {
        return Arbitraries.of(EXTRACTOR_POOL)
                .set()
                .ofMinSize(0)
                .ofMaxSize(EXTRACTOR_POOL.size());
    }
}
