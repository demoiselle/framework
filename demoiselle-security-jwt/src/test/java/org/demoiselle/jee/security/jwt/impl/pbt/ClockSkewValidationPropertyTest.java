/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl.pbt;

import java.lang.reflect.Field;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jwt-enhancements, Property 13: Non-negative clockSkewSeconds validation
 *
 * **Validates: Requirements 7.4**
 *
 * For any negative integer assigned to clockSkewSeconds, the config should
 * return the default of 60 seconds.
 */
class ClockSkewValidationPropertyTest {

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName + " not found in " + target.getClass().getName());
    }

    /**
     * P13: For any negative integer assigned to clockSkewSeconds,
     * the config should return the default of 60 seconds.
     */
    @Property(tries = 100)
    void negativeClockSkewShouldReturnDefault(
            @ForAll @IntRange(min = -10000, max = -1) int negativeValue) throws Exception {

        DemoiselleSecurityJWTConfig config = new DemoiselleSecurityJWTConfig();
        setField(config, "clockSkewSeconds", negativeValue);

        assertEquals(60, config.getClockSkewSeconds(),
                "Negative clockSkewSeconds (" + negativeValue + ") should fall back to default 60");
    }

    /**
     * Complementary: For any non-negative integer, the config should return
     * the configured value as-is.
     */
    @Property(tries = 100)
    void nonNegativeClockSkewShouldReturnConfiguredValue(
            @ForAll @IntRange(min = 0, max = 3600) int nonNegativeValue) throws Exception {

        DemoiselleSecurityJWTConfig config = new DemoiselleSecurityJWTConfig();
        setField(config, "clockSkewSeconds", nonNegativeValue);

        assertEquals(nonNegativeValue, config.getClockSkewSeconds(),
                "Non-negative clockSkewSeconds (" + nonNegativeValue + ") should be returned as-is");
    }
}
