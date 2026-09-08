/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CrudLimitsConfig} defaults and null-safe getters.
 *
 * @author SERPRO
 */
class CrudLimitsConfigTest {

    @Test
    void defaultsAreApplied() {
        CrudLimitsConfig config = new CrudLimitsConfig();
        assertEquals(20, config.getMaxFilters());
        assertEquals(100, config.getMaxFilterValues());
        assertEquals(1024, config.getMaxFilterValueLength());
        assertEquals(10, config.getMaxSortFields());
    }

    @Test
    void gettersAlwaysReturnPositiveValues() {
        CrudLimitsConfig config = new CrudLimitsConfig();
        assertTrue(config.getMaxFilters() > 0);
        assertTrue(config.getMaxFilterValues() > 0);
        assertTrue(config.getMaxFilterValueLength() > 0);
        assertTrue(config.getMaxSortFields() > 0);
    }
}
