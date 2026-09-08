/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.demoiselle.jee.core.api.pagination.Cursor;
import org.demoiselle.jee.crud.sort.CrudSort;
import org.demoiselle.jee.crud.sort.SortModel;
import org.junit.jupiter.api.Test;

class KeysetConcurrentInsertionTest {

    private record Row(long id) {
    }

    @Test
    void insertionBetweenPagesDoesNotDuplicateOrLoseExistingRows() {
        List<Row> data = new ArrayList<>();
        for (long id = 1; id <= 100; id++) {
            data.add(new Row(id));
        }

        List<Long> visited = new ArrayList<>();
        long boundary = 0;
        int page = 0;
        while (true) {
            long currentBoundary = boundary;
            List<Row> slice = data.stream()
                    .filter(row -> row.id() > currentBoundary)
                    .sorted(java.util.Comparator.comparingLong(Row::id))
                    .limit(10)
                    .toList();
            if (slice.isEmpty()) {
                break;
            }
            visited.addAll(slice.stream().map(Row::id).toList());
            Cursor cursor = KeysetPagination.toCursor(
                    List.of(new SortModel(CrudSort.ASC, "id")),
                    slice.get(slice.size() - 1),
                    field -> Long.toString(slice.get(slice.size() - 1).id()),
                    60, 1_000);
            boundary = Long.parseLong(cursor.orderedKeys().get("id"));
            if (page++ == 0) {
                data.add(new Row(1_000));
            }
        }

        Set<Long> unique = new HashSet<>(visited);
        assertEquals(visited.size(), unique.size(), "no row may be duplicated");
        for (long id = 1; id <= 100; id++) {
            long expectedId = id;
            assertEquals(1, visited.stream().filter(value -> value == expectedId).count(),
                    "every row present before pagination must be visited exactly once");
        }
        assertEquals(1, visited.stream().filter(value -> value == 1_000).count(),
                "a later insertion after the boundary is observed once");
    }
}
