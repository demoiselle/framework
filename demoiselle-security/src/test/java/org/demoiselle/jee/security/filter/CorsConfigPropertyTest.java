/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 11: maxAge padrão quando valor não-positivo
/**
 * Property-based test for {@link DemoiselleSecurityConfig#getCorsMaxAge()} default behavior.
 *
 * <p><b>Validates: Requirements 5.7</b></p>
 */
class CorsConfigPropertyTest {

    private static final int DEFAULT_MAX_AGE = 3600;

    /**
     * Sets the {@code corsMaxAge} field on a {@link DemoiselleSecurityConfig} instance via reflection.
     */
    private void setCorsMaxAge(DemoiselleSecurityConfig config, int value) throws Exception {
        Field field = DemoiselleSecurityConfig.class.getDeclaredField("corsMaxAge");
        field.setAccessible(true);
        field.setInt(config, value);
    }

    // Feature: security-enhancements, Property 11: maxAge padrão quando valor não-positivo
    /**
     * Property 11: For any integer value of corsMaxAge, if the value is less than or equal
     * to zero, getCorsMaxAge() must return 3600 (default). If the value is positive, it must
     * return the configured value.
     *
     * <p><b>Validates: Requirements 5.7</b></p>
     */
    @Property(tries = 100)
    void maxAgeDefaultsWhenNonPositive(@ForAll int corsMaxAge) throws Exception {
        DemoiselleSecurityConfig config = new DemoiselleSecurityConfig();
        setCorsMaxAge(config, corsMaxAge);

        int result = config.getCorsMaxAge();

        if (corsMaxAge > 0) {
            assertEquals(corsMaxAge, result,
                    "getCorsMaxAge() must return the configured value when positive. " +
                    "Configured: " + corsMaxAge + ", Got: " + result);
        } else {
            assertEquals(DEFAULT_MAX_AGE, result,
                    "getCorsMaxAge() must return 3600 (default) when configured value is non-positive. " +
                    "Configured: " + corsMaxAge + ", Got: " + result);
        }
    }
}
