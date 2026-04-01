/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ResultSet} with List.copyOf() immutability.
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4
 */
class ResultSetTest {

    @Test
    void newResultSetShouldReturnEmptyList() {
        ResultSet rs = new ResultSet();
        assertNotNull(rs.getContent());
        assertTrue(rs.getContent().isEmpty());
    }

    @Test
    void setContentWithNullShouldResultInEmptyList() {
        ResultSet rs = new ResultSet();
        rs.setContent(null);
        assertNotNull(rs.getContent());
        assertTrue(rs.getContent().isEmpty());
    }

    @Test
    void setContentShouldStoreDefensiveCopy() {
        List<String> original = new ArrayList<>(List.of("a", "b", "c"));
        ResultSet rs = new ResultSet();
        rs.setContent(original);

        assertEquals(3, rs.getContent().size());

        // Modify the original list
        original.add("d");
        original.remove(0);

        // ResultSet content should be unaffected
        assertEquals(3, rs.getContent().size());
        assertEquals(List.of("a", "b", "c"), rs.getContent());
    }

    @Test
    void getContentShouldReturnImmutableList() {
        ResultSet rs = new ResultSet();
        rs.setContent(List.of("x", "y"));

        List<?> content = rs.getContent();
        assertThrows(UnsupportedOperationException.class, () -> {
            @SuppressWarnings("unchecked")
            List<Object> mutable = (List<Object>) content;
            mutable.add("z");
        });
    }

    @Test
    void setContentWithValidListShouldPreserveElements() {
        ResultSet rs = new ResultSet();
        rs.setContent(List.of(1, 2, 3));

        assertEquals(3, rs.getContent().size());
        assertEquals(List.of(1, 2, 3), rs.getContent());
    }

    @Test
    void setContentWithEmptyListShouldResultInEmptyList() {
        ResultSet rs = new ResultSet();
        rs.setContent(List.of());
        assertNotNull(rs.getContent());
        assertTrue(rs.getContent().isEmpty());
    }
}
