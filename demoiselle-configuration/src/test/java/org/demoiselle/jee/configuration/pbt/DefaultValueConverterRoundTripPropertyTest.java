/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import net.jqwik.api.*;

import org.demoiselle.jee.configuration.ConfigurationType;
import org.demoiselle.jee.configuration.DefaultValueConverter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 3: Round-trip de conversão DefaultValueConverter
 *
 * <p><b>Validates: Requirements 4.2</b></p>
 *
 * For any supported type and any valid value representable as a String,
 * {@code DefaultValueConverter.convert(value, type)} must produce an object
 * whose {@code toString()} is equivalent to the original input string.
 */
class DefaultValueConverterRoundTripPropertyTest {

    // ── String round-trip ──

    @Property(tries = 100)
    void stringRoundTrip(@ForAll String value) {
        Object result = DefaultValueConverter.convert(value, String.class);
        assertEquals(value, result.toString());
    }

    // ── int / Integer round-trip ──

    @Property(tries = 100)
    void intRoundTrip(@ForAll int value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, int.class);
        assertEquals(strValue, result.toString());
    }

    @Property(tries = 100)
    void integerWrapperRoundTrip(@ForAll int value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, Integer.class);
        assertEquals(strValue, result.toString());
    }

    // ── long / Long round-trip ──

    @Property(tries = 100)
    void longRoundTrip(@ForAll long value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, long.class);
        assertEquals(strValue, result.toString());
    }

    @Property(tries = 100)
    void longWrapperRoundTrip(@ForAll long value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, Long.class);
        assertEquals(strValue, result.toString());
    }

    // ── boolean / Boolean round-trip ──

    @Property(tries = 100)
    void booleanRoundTrip(@ForAll("booleanStrings") String boolStr) {
        Object result = DefaultValueConverter.convert(boolStr, boolean.class);
        assertEquals(boolStr, result.toString());
    }

    @Property(tries = 100)
    void booleanWrapperRoundTrip(@ForAll("booleanStrings") String boolStr) {
        Object result = DefaultValueConverter.convert(boolStr, Boolean.class);
        assertEquals(boolStr, result.toString());
    }

    @Provide
    Arbitrary<String> booleanStrings() {
        return Arbitraries.of("true", "false");
    }

    // ── double / Double round-trip ──

    @Property(tries = 100)
    void doubleRoundTrip(@ForAll("exactDoubles") double value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, double.class);
        assertEquals(strValue, result.toString());
    }

    @Property(tries = 100)
    void doubleWrapperRoundTrip(@ForAll("exactDoubles") double value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, Double.class);
        assertEquals(strValue, result.toString());
    }

    /**
     * Generates doubles that have exact string representations by using
     * integer values cast to double, avoiding floating-point precision issues.
     */
    @Provide
    Arbitrary<Double> exactDoubles() {
        return Arbitraries.integers().between(-1_000_000, 1_000_000)
                .map(i -> (double) i);
    }

    // ── float / Float round-trip ──

    @Property(tries = 100)
    void floatRoundTrip(@ForAll("exactFloats") float value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, float.class);
        assertEquals(strValue, result.toString());
    }

    @Property(tries = 100)
    void floatWrapperRoundTrip(@ForAll("exactFloats") float value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, Float.class);
        assertEquals(strValue, result.toString());
    }

    /**
     * Generates floats that have exact string representations by using
     * small integer values cast to float.
     */
    @Provide
    Arbitrary<Float> exactFloats() {
        return Arbitraries.integers().between(-100_000, 100_000)
                .map(i -> (float) i);
    }

    // ── short / Short round-trip ──

    @Property(tries = 100)
    void shortRoundTrip(@ForAll short value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, short.class);
        assertEquals(strValue, result.toString());
    }

    @Property(tries = 100)
    void shortWrapperRoundTrip(@ForAll short value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, Short.class);
        assertEquals(strValue, result.toString());
    }

    // ── byte / Byte round-trip ──

    @Property(tries = 100)
    void byteRoundTrip(@ForAll byte value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, byte.class);
        assertEquals(strValue, result.toString());
    }

    @Property(tries = 100)
    void byteWrapperRoundTrip(@ForAll byte value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, Byte.class);
        assertEquals(strValue, result.toString());
    }

    // ── char / Character round-trip ──

    @Property(tries = 100)
    void charRoundTrip(@ForAll char value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, char.class);
        assertEquals(strValue, result.toString());
    }

    @Property(tries = 100)
    void characterWrapperRoundTrip(@ForAll char value) {
        String strValue = String.valueOf(value);
        Object result = DefaultValueConverter.convert(strValue, Character.class);
        assertEquals(strValue, result.toString());
    }

    // ── Enum round-trip ──

    @Property(tries = 100)
    void enumRoundTrip(@ForAll("configurationTypes") ConfigurationType value) {
        String strValue = value.name();
        Object result = DefaultValueConverter.convert(strValue, ConfigurationType.class);
        assertEquals(strValue, result.toString());
    }

    @Provide
    Arbitrary<ConfigurationType> configurationTypes() {
        return Arbitraries.of(ConfigurationType.values());
    }
}
