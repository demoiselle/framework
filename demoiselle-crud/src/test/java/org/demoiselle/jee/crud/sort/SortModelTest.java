/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link SortModel} record.
 *
 * Validates: Requirements 1.2, 1.3, 1.4, 1.5
 */
class SortModelTest {

    @Test
    void validConstructionWithAsc() {
        SortModel model = new SortModel(CrudSort.ASC, "name");
        assertEquals(CrudSort.ASC, model.type());
        assertEquals("name", model.field());
    }

    @Test
    void validConstructionWithDesc() {
        SortModel model = new SortModel(CrudSort.DESC, "id");
        assertEquals(CrudSort.DESC, model.type());
        assertEquals("id", model.field());
    }

    @Test
    void nullTypeShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortModel(null, "name"));
    }

    @Test
    void nullFieldShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortModel(CrudSort.ASC, null));
    }

    @Test
    void emptyFieldShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SortModel(CrudSort.ASC, ""));
    }

    @Test
    void blankFieldShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SortModel(CrudSort.ASC, "   "));
    }

    @Test
    void recordsWithSameValuesShouldBeEqual() {
        SortModel a = new SortModel(CrudSort.ASC, "name");
        SortModel b = new SortModel(CrudSort.ASC, "name");
        assertEquals(a, b);
    }

    @Test
    void recordsWithSameValuesShouldHaveSameHashCode() {
        SortModel a = new SortModel(CrudSort.DESC, "id");
        SortModel b = new SortModel(CrudSort.DESC, "id");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void recordsWithDifferentValuesShouldNotBeEqual() {
        SortModel a = new SortModel(CrudSort.ASC, "name");
        SortModel b = new SortModel(CrudSort.DESC, "name");
        assertNotEquals(a, b);

        SortModel c = new SortModel(CrudSort.ASC, "id");
        assertNotEquals(a, c);
    }
}
