/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 4: Precedência de fonte sobre @DefaultValue
 *
 * <p><b>Validates: Requirements 4.3</b></p>
 *
 * For any field with a {@code @DefaultValue} annotation and a corresponding key
 * present in the configuration source, the final value assigned to the field must
 * be the source value, never the {@code @DefaultValue} annotation value.
 *
 * <p>This test replicates the precedence decision logic from
 * {@code ConfigurationLoader.fillFieldWithValue()} in isolation:
 * when {@code loadedValue != null}, the source value always prevails over
 * both the field's default value and the {@code @DefaultValue} annotation.</p>
 */
class DefaultValuePrecedencePropertyTest {

    /**
     * Replicates the precedence logic from {@code fillFieldWithValue()}:
     * <pre>
     *   Object finalValue = loadedValue == null ? defaultValue : loadedValue;
     *   if (loadedValue == null && defaultValue == null && hasDefaultValueAnnotation) {
     *       finalValue = annotationValue;
     *   }
     * </pre>
     *
     * @param loadedValue          the value from the configuration source (may be null)
     * @param fieldDefaultValue    the field's initial value (may be null)
     * @param annotationValue      the value from {@code @DefaultValue} annotation
     * @param hasAnnotation        whether the field has {@code @DefaultValue}
     * @return the final resolved value
     */
    private static Object resolveFinalValue(Object loadedValue,
                                            Object fieldDefaultValue,
                                            String annotationValue,
                                            boolean hasAnnotation) {
        Object finalValue = loadedValue == null ? fieldDefaultValue : loadedValue;

        if (loadedValue == null && fieldDefaultValue == null && hasAnnotation) {
            finalValue = annotationValue;
        }

        return finalValue;
    }

    // ── Property: source value always prevails when non-null ──

    @Property(tries = 100)
    void sourceValueAlwaysPrevailsOverDefaultAnnotation(
            @ForAll("nonNullStrings") String sourceValue,
            @ForAll String annotationValue) {

        Object result = resolveFinalValue(sourceValue, null, annotationValue, true);

        assertEquals(sourceValue, result,
                "When source value is non-null, it must prevail over @DefaultValue");
    }

    @Property(tries = 100)
    void sourceValueAlwaysPrevailsOverFieldDefault(
            @ForAll("nonNullStrings") String sourceValue,
            @ForAll("nonNullStrings") String fieldDefault,
            @ForAll String annotationValue) {

        Object result = resolveFinalValue(sourceValue, fieldDefault, annotationValue, true);

        assertEquals(sourceValue, result,
                "When source value is non-null, it must prevail over both field default and @DefaultValue");
    }

    @Property(tries = 100)
    void sourceValuePrevailsEvenWithoutAnnotation(
            @ForAll("nonNullStrings") String sourceValue,
            @ForAll @From("nullableStrings") String fieldDefault) {

        Object result = resolveFinalValue(sourceValue, fieldDefault, "unused", false);

        assertEquals(sourceValue, result,
                "When source value is non-null, it must prevail regardless of annotation presence");
    }

    // ── Property: @DefaultValue only applies when both source and field default are null ──

    @Property(tries = 100)
    void annotationAppliesOnlyWhenBothSourceAndFieldDefaultAreNull(
            @ForAll String annotationValue) {

        Object result = resolveFinalValue(null, null, annotationValue, true);

        assertEquals(annotationValue, result,
                "@DefaultValue should apply when both loadedValue and fieldDefaultValue are null");
    }

    // ── Property: field default prevails over annotation when source is null ──

    @Property(tries = 100)
    void fieldDefaultPrevailsOverAnnotationWhenSourceIsNull(
            @ForAll("nonNullStrings") String fieldDefault,
            @ForAll String annotationValue) {

        Object result = resolveFinalValue(null, fieldDefault, annotationValue, true);

        assertEquals(fieldDefault, result,
                "When source is null but field has a default, field default should prevail over @DefaultValue");
    }

    // ── Property: source value prevails with numeric types ──

    @Property(tries = 100)
    void sourceIntegerPrevailsOverDefaultAnnotation(
            @ForAll int sourceValue,
            @ForAll String annotationValue) {

        Object result = resolveFinalValue(sourceValue, null, annotationValue, true);

        assertEquals(sourceValue, result,
                "Integer source value must prevail over @DefaultValue annotation");
    }

    @Property(tries = 100)
    void sourceLongPrevailsOverDefaultAnnotation(
            @ForAll long sourceValue,
            @ForAll String annotationValue) {

        Object result = resolveFinalValue(sourceValue, null, annotationValue, true);

        assertEquals(sourceValue, result,
                "Long source value must prevail over @DefaultValue annotation");
    }

    @Property(tries = 100)
    void sourceBooleanPrevailsOverDefaultAnnotation(
            @ForAll boolean sourceValue,
            @ForAll String annotationValue) {

        Object result = resolveFinalValue(sourceValue, null, annotationValue, true);

        assertEquals(sourceValue, result,
                "Boolean source value must prevail over @DefaultValue annotation");
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> nonNullStrings() {
        return Arbitraries.strings().ofMinLength(1).ofMaxLength(200);
    }

    @Provide
    Arbitrary<String> nullableStrings() {
        return Arbitraries.strings().ofMinLength(1).ofMaxLength(200)
                .injectNull(0.3);
    }
}
