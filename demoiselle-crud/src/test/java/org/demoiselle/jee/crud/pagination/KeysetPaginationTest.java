/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.demoiselle.jee.core.api.pagination.Cursor;
import org.demoiselle.jee.crud.sort.CrudSort;
import org.demoiselle.jee.crud.sort.SortModel;
import org.demoiselle.jee.crud.specification.Specification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KeysetPagination}.
 *
 * <p>Validates Requirement (5): deterministic tie-breaker keyset predicate and
 * cursor extraction; additive (does not alter offset/range flow).</p>
 */
class KeysetPaginationTest {

    @Test
    @DisplayName("toCursor builds an ordered keyset with tie-breaker last and TTL expiry")
    void toCursorProducesOrderedKeysWithExpiry() {
        List<SortModel> sort = List.of(
                new SortModel(CrudSort.ASC, "createdAt"),
                new SortModel(CrudSort.ASC, "id"));

        Map<String, String> row = Map.of("createdAt", "2025-01-01", "id", "7");
        Cursor cursor = KeysetPagination.toCursor(sort, row, row::get, 60, 1000);

        List<String> keyOrder = new ArrayList<>(cursor.orderedKeys().keySet());
        assertEquals(List.of("createdAt", "id"), keyOrder, "tie-breaker 'id' must be last");
        assertEquals("2025-01-01", cursor.orderedKeys().get("createdAt"));
        assertEquals("7", cursor.orderedKeys().get("id"));
        assertEquals(Cursor.Direction.AFTER, cursor.direction());
        assertEquals(1060, cursor.expiresAtEpochSeconds());
    }

    @Test
    @DisplayName("empty sort keys are rejected (no total order → non-deterministic)")
    void emptySortRejected() {
        Cursor cursor = new Cursor(Map.of("id", "1"), Cursor.Direction.AFTER, 0);
        assertThrows(IllegalArgumentException.class,
                () -> KeysetPagination.toSpecification(cursor, List.of(), e -> e.getValue()));
        assertThrows(IllegalArgumentException.class,
                () -> KeysetPagination.toCursor(List.of(), Map.of(), s -> "", 0, 0));
    }

    @Test
    @DisplayName("AFTER + ASC keys generate the lexicographic (>, = AND >) predicate")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void specificationBuildsLexicographicPredicate() {
        List<SortModel> sort = List.of(
                new SortModel(CrudSort.ASC, "createdAt"),
                new SortModel(CrudSort.ASC, "id"));
        java.util.LinkedHashMap<String, String> boundary = new java.util.LinkedHashMap<>();
        boundary.put("createdAt", "2025-01-01");
        boundary.put("id", "7");
        Cursor cursor = new Cursor(boundary, Cursor.Direction.AFTER, 0);

        Specification<Object> spec = KeysetPagination.toSpecification(
                cursor, sort, e -> (Comparable) e.getValue());

        Root<Object> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.greaterThan(any(jakarta.persistence.criteria.Expression.class), any(Comparable.class)))
                .thenReturn(predicate);
        when(cb.lessThan(any(jakarta.persistence.criteria.Expression.class), any(Comparable.class)))
                .thenReturn(predicate);
        when(cb.equal(any(), any(Comparable.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = spec.toPredicate(root, query, cb);

        assertTrue(result != null);
        // Two keys → two OR terms: (createdAt > v) OR (createdAt = v AND id > v).
        // Ascending → greaterThan used twice (one per strict key), equal at least once.
        verify(cb, times(2)).greaterThan(any(jakarta.persistence.criteria.Expression.class), any(Comparable.class));
        verify(cb, atLeastOnce()).equal(any(), any(Comparable.class));
        verify(cb, atLeastOnce()).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("BEFORE direction inverts the strict comparison to lessThan")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void beforeDirectionUsesLessThan() {
        List<SortModel> sort = List.of(new SortModel(CrudSort.ASC, "id"));
        Cursor cursor = new Cursor(Map.of("id", "7"), Cursor.Direction.BEFORE, 0);

        Specification<Object> spec = KeysetPagination.toSpecification(
                cursor, sort, e -> (Comparable) e.getValue());

        Root<Object> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        when(root.get(anyString())).thenReturn(path);
        when(cb.lessThan(any(jakarta.persistence.criteria.Expression.class), any(Comparable.class)))
                .thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);

        spec.toPredicate(root, mock(CriteriaQuery.class), cb);

        verify(cb, times(1)).lessThan(any(jakarta.persistence.criteria.Expression.class), any(Comparable.class));
    }
}
