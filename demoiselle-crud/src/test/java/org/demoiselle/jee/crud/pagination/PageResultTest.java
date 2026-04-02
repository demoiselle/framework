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
 * Unit tests for {@link PageResult}.
 *
 * Validates: Requirements 5.1, 5.2, 5.5, 5.6
 */
class PageResultTest {

    // --- Constructor & defensive copy ---

    @Test
    void nullContentShouldBecomeEmptyList() {
        PageResult<String> pr = new PageResult<>(null, 0, 0, 0, 10, false, false);
        assertNotNull(pr.content());
        assertTrue(pr.content().isEmpty());
    }

    @Test
    void constructorShouldMakeDefensiveCopy() {
        List<String> original = new ArrayList<>(List.of("a", "b"));
        PageResult<String> pr = new PageResult<>(original, 2, 1, 0, 10, false, false);

        original.add("c");
        assertEquals(2, pr.content().size(), "Mutation of original must not affect PageResult");
    }

    @Test
    void contentShouldBeImmutable() {
        PageResult<String> pr = new PageResult<>(List.of("x"), 1, 1, 0, 10, false, false);
        assertThrows(UnsupportedOperationException.class, () -> pr.content().add("y"));
    }

    // --- Result interface ---

    @Test
    void getContentShouldDelegateToContent() {
        PageResult<Integer> pr = new PageResult<>(List.of(1, 2, 3), 3, 1, 0, 10, false, false);
        assertEquals(List.of(1, 2, 3), pr.getContent());
        assertSame(pr.content(), pr.getContent());
    }

    @Test
    void setContentShouldThrowUnsupportedOperationException() {
        PageResult<String> pr = new PageResult<>(List.of(), 0, 0, 0, 10, false, false);
        assertThrows(UnsupportedOperationException.class, () -> pr.setContent(List.of("a")));
    }

    // --- Factory method of() metadata calculations ---

    @Test
    void ofShouldCalculateTotalPages() {
        // 100 elements, pageSize 25 → 4 pages
        PageResult<String> pr = PageResult.of(List.of("a"), 100, 0, 25);
        assertEquals(4, pr.totalPages());
    }

    @Test
    void ofShouldCalculateTotalPagesWithRemainder() {
        // 101 elements, pageSize 25 → 5 pages (ceil)
        PageResult<String> pr = PageResult.of(List.of("a"), 101, 0, 25);
        assertEquals(5, pr.totalPages());
    }

    @Test
    void ofShouldCalculateCurrentPage() {
        // offset 50, pageSize 25 → page 2
        PageResult<String> pr = PageResult.of(List.of("a"), 100, 50, 25);
        assertEquals(2, pr.currentPage());
    }

    @Test
    void ofShouldSetHasNextTrue() {
        // page 0 of 4 → hasNext = true
        PageResult<String> pr = PageResult.of(List.of("a"), 100, 0, 25);
        assertTrue(pr.hasNext());
    }

    @Test
    void ofShouldSetHasNextFalseOnLastPage() {
        // page 3 (last) of 4 → hasNext = false
        PageResult<String> pr = PageResult.of(List.of("a"), 100, 75, 25);
        assertFalse(pr.hasNext());
    }

    @Test
    void ofShouldSetHasPreviousFalseOnFirstPage() {
        PageResult<String> pr = PageResult.of(List.of("a"), 100, 0, 25);
        assertFalse(pr.hasPrevious());
    }

    @Test
    void ofShouldSetHasPreviousTrueOnSecondPage() {
        PageResult<String> pr = PageResult.of(List.of("a"), 100, 25, 25);
        assertTrue(pr.hasPrevious());
    }

    @Test
    void ofWithZeroPageSizeShouldReturnZeroMetadata() {
        PageResult<String> pr = PageResult.of(List.of(), 0, 0, 0);
        assertEquals(0, pr.totalPages());
        assertEquals(0, pr.currentPage());
        assertFalse(pr.hasNext());
        assertFalse(pr.hasPrevious());
    }

    @Test
    void ofShouldPreserveContentAndTotalElements() {
        List<String> items = List.of("x", "y", "z");
        PageResult<String> pr = PageResult.of(items, 30, 0, 10);
        assertEquals(items, pr.content());
        assertEquals(30, pr.totalElements());
        assertEquals(10, pr.pageSize());
    }
}
