/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.filter;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link FilterOp} sealed interface and its 7 record variants.
 *
 * Validates: Requirements 6.2, 6.3, 6.4, 6.5
 */
class FilterOpTest {

    // --- Equals ---

    @Test
    void equalsValidConstruction() {
        var op = new FilterOp.Equals("name", "John");
        assertEquals("name", op.key());
        assertEquals("John", op.value());
    }

    @Test
    void equalsNullKeyShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.Equals(null, "v"));
    }

    @Test
    void equalsNullValueShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.Equals("k", null));
    }

    // --- Like ---

    @Test
    void likeValidConstruction() {
        var op = new FilterOp.Like("name", "*John*");
        assertEquals("name", op.key());
        assertEquals("*John*", op.pattern());
    }

    @Test
    void likeNullKeyShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.Like(null, "*x*"));
    }

    @Test
    void likeNullPatternShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.Like("k", null));
    }

    // --- IsNull ---

    @Test
    void isNullValidConstruction() {
        var op = new FilterOp.IsNull("status");
        assertEquals("status", op.key());
    }

    @Test
    void isNullNullKeyShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.IsNull(null));
    }

    // --- IsTrue ---

    @Test
    void isTrueValidConstruction() {
        var op = new FilterOp.IsTrue("active");
        assertEquals("active", op.key());
    }

    @Test
    void isTrueNullKeyShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.IsTrue(null));
    }

    // --- IsFalse ---

    @Test
    void isFalseValidConstruction() {
        var op = new FilterOp.IsFalse("deleted");
        assertEquals("deleted", op.key());
    }

    @Test
    void isFalseNullKeyShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.IsFalse(null));
    }

    // --- EnumFilter ---

    @Test
    void enumFilterValidConstruction() {
        var op = new FilterOp.EnumFilter("status", "ACTIVE", 0);
        assertEquals("status", op.key());
        assertEquals("ACTIVE", op.value());
        assertEquals(0, op.ordinal());
    }

    @Test
    void enumFilterNullKeyShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.EnumFilter(null, "ACTIVE", 0));
    }

    @Test
    void enumFilterNullValueShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.EnumFilter("k", null, 0));
    }

    @Test
    void enumFilterNegativeOrdinalShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new FilterOp.EnumFilter("k", "ACTIVE", -1));
    }

    // --- UUIDFilter ---

    @Test
    void uuidFilterValidConstruction() {
        UUID id = UUID.randomUUID();
        var op = new FilterOp.UUIDFilter("id", id);
        assertEquals("id", op.key());
        assertEquals(id, op.value());
    }

    @Test
    void uuidFilterNullKeyShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.UUIDFilter(null, UUID.randomUUID()));
    }

    @Test
    void uuidFilterNullValueShouldThrow() {
        assertThrows(NullPointerException.class, () -> new FilterOp.UUIDFilter("id", null));
    }
}
