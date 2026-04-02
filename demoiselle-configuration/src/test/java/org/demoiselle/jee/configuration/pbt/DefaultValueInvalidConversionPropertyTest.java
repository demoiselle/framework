/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.StringLength;

import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.DefaultValueConverter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 5: Valores inválidos em @DefaultValue lançam exceção
 *
 * <p><b>Validates: Requirements 4.6</b></p>
 *
 * For any string that is not convertible to the target type (e.g., "abc" for int,
 * empty string for char, invalid name for enum),
 * {@code DefaultValueConverter.convert()} must throw an exception
 * (IllegalArgumentException or NumberFormatException).
 */
class DefaultValueInvalidConversionPropertyTest {

    // ── int / Integer: non-numeric strings ──

    @Property(tries = 100)
    void invalidIntThrows(@ForAll("nonNumericStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, int.class));
    }

    @Property(tries = 100)
    void invalidIntegerWrapperThrows(@ForAll("nonNumericStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, Integer.class));
    }

    // ── long / Long: non-numeric strings ──

    @Property(tries = 100)
    void invalidLongThrows(@ForAll("nonNumericStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, long.class));
    }

    @Property(tries = 100)
    void invalidLongWrapperThrows(@ForAll("nonNumericStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, Long.class));
    }

    // ── double / Double: non-numeric strings ──

    @Property(tries = 100)
    void invalidDoubleThrows(@ForAll("nonDoubleStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, double.class));
    }

    @Property(tries = 100)
    void invalidDoubleWrapperThrows(@ForAll("nonDoubleStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, Double.class));
    }

    // ── float / Float: non-numeric strings ──

    @Property(tries = 100)
    void invalidFloatThrows(@ForAll("nonDoubleStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, float.class));
    }

    @Property(tries = 100)
    void invalidFloatWrapperThrows(@ForAll("nonDoubleStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, Float.class));
    }

    // ── short / Short: non-numeric strings and out-of-range numbers ──

    @Property(tries = 100)
    void invalidShortThrows(@ForAll("invalidShortStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, short.class));
    }

    @Property(tries = 100)
    void invalidShortWrapperThrows(@ForAll("invalidShortStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, Short.class));
    }

    // ── byte / Byte: non-numeric strings and out-of-range numbers ──

    @Property(tries = 100)
    void invalidByteThrows(@ForAll("invalidByteStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, byte.class));
    }

    @Property(tries = 100)
    void invalidByteWrapperThrows(@ForAll("invalidByteStrings") String value) {
        assertThrows(NumberFormatException.class,
                () -> DefaultValueConverter.convert(value, Byte.class));
    }

    // ── char / Character: strings with length != 1 ──

    @Property(tries = 100)
    void invalidCharEmptyStringThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DefaultValueConverter.convert("", char.class));
    }

    @Property(tries = 100)
    void invalidCharMultiCharThrows(@ForAll("multiCharStrings") String value) {
        assertThrows(IllegalArgumentException.class,
                () -> DefaultValueConverter.convert(value, char.class));
    }

    @Property(tries = 100)
    void invalidCharacterWrapperMultiCharThrows(@ForAll("multiCharStrings") String value) {
        assertThrows(IllegalArgumentException.class,
                () -> DefaultValueConverter.convert(value, Character.class));
    }

    // ── Enum: invalid constant names ──

    @Property(tries = 100)
    void invalidEnumThrows(@ForAll("invalidEnumNames") String value) {
        assertThrows(IllegalArgumentException.class,
                () -> DefaultValueConverter.convert(value, ConfigurationType.class));
    }

    // ── Unsupported type ──

    @Property(tries = 100)
    void unsupportedTypeThrows(@ForAll String value) {
        assertThrows(IllegalArgumentException.class,
                () -> DefaultValueConverter.convert(value, java.util.List.class));
    }

    // ═══════════════════════════════════════════════════════════════
    // Generators
    // ═══════════════════════════════════════════════════════════════

    /**
     * Generates strings that are definitely not parseable as integers or longs.
     * Includes alphabetic strings, strings with decimal points, empty strings,
     * and strings with special characters.
     */
    @Provide
    Arbitrary<String> nonNumericStrings() {
        return Arbitraries.oneOf(
                // purely alphabetic
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
                // decimal numbers (invalid for int/long)
                Arbitraries.doubles().between(-1000, 1000)
                        .filter(d -> d != Math.floor(d))
                        .map(String::valueOf),
                // empty string
                Arbitraries.just(""),
                // strings with spaces
                Arbitraries.of("  ", " 42 ", "1 2"),
                // special characters
                Arbitraries.of("abc", "12.5", "1e2x", "#$%", "null", "NaN!")
        );
    }

    /**
     * Generates strings that are not parseable as double/float.
     * Excludes values like "NaN" and "Infinity" which are valid for Double/Float.
     */
    @Provide
    Arbitrary<String> nonDoubleStrings() {
        return Arbitraries.oneOf(
                // purely alphabetic (excluding NaN, Infinity, -Infinity)
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                        .filter(s -> !s.equals("NaN") && !s.equals("Infinity")),
                // empty string
                Arbitraries.just(""),
                // strings with invalid characters
                Arbitraries.of("abc", "12.5.6", "#$%", "null", "1,5", "1e2x")
        );
    }

    /**
     * Generates strings invalid for short: non-numeric strings and
     * numbers outside the short range [-32768, 32767].
     */
    @Provide
    Arbitrary<String> invalidShortStrings() {
        return Arbitraries.oneOf(
                // non-numeric
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                Arbitraries.just(""),
                // out-of-range integers
                Arbitraries.integers().between(Short.MAX_VALUE + 1, Short.MAX_VALUE + 10000)
                        .map(String::valueOf),
                Arbitraries.integers().between(Short.MIN_VALUE - 10000, Short.MIN_VALUE - 1)
                        .map(String::valueOf)
        );
    }

    /**
     * Generates strings invalid for byte: non-numeric strings and
     * numbers outside the byte range [-128, 127].
     */
    @Provide
    Arbitrary<String> invalidByteStrings() {
        return Arbitraries.oneOf(
                // non-numeric
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                Arbitraries.just(""),
                // out-of-range integers
                Arbitraries.integers().between(Byte.MAX_VALUE + 1, Byte.MAX_VALUE + 1000)
                        .map(String::valueOf),
                Arbitraries.integers().between(Byte.MIN_VALUE - 1000, Byte.MIN_VALUE - 1)
                        .map(String::valueOf)
        );
    }

    /**
     * Generates strings with length >= 2 (multi-character), invalid for char conversion.
     */
    @Provide
    Arbitrary<String> multiCharStrings() {
        return Arbitraries.strings().ofMinLength(2).ofMaxLength(10);
    }

    /**
     * Generates strings that are not valid ConfigurationType enum constant names.
     * Valid names are: PROPERTIES, XML, SYSTEM.
     */
    @Provide
    Arbitrary<String> invalidEnumNames() {
        return Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                        .filter(s -> !s.equals("PROPERTIES") && !s.equals("XML") && !s.equals("SYSTEM")),
                Arbitraries.of("properties", "xml", "system", "Dev", "INVALID", ""),
                Arbitraries.just("unknown")
        );
    }
}
