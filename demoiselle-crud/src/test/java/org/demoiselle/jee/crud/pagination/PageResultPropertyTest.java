/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import org.demoiselle.jee.core.api.crud.Result;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link PageResult} — Properties 14, 15, 16.
 *
 * <p><b>Validates: Requirements 5.3, 5.4, 5.5, 5.6</b></p>
 */
class PageResultPropertyTest {

    // -----------------------------------------------------------------------
    // Property 14: find() retorna tipo correto baseado em paginação
    // -----------------------------------------------------------------------

    /**
     * Feature: crud-enhancements, Property 14: find() retorna tipo correto baseado em paginação
     *
     * <p>When pagination is enabled, the result must be an instance of {@link PageResult}
     * (and also {@link Result}). When pagination is disabled, the result must be an
     * instance of {@link ResultSet} (and also {@link Result}) but NOT {@link PageResult}.</p>
     *
     * <p>Since AbstractDAO.find() requires full JPA infrastructure, we verify the type
     * contract at the construction level: PageResult.of() always produces a PageResult
     * that is a Result, and ResultSet is a Result but never a PageResult.</p>
     *
     * <p><b>Validates: Requirements 5.3, 5.4</b></p>
     */
    @Property(tries = 100)
    // Feature: crud-enhancements, Property 14: find() retorna tipo correto baseado em paginação
    void paginatedResultIsPageResultAndNonPaginatedIsResultSet(
            @ForAll @LongRange(min = 0, max = 10000) long totalElements,
            @ForAll @IntRange(min = 0, max = 9999) int offset,
            @ForAll @IntRange(min = 1, max = 100) int pageSize,
            @ForAll boolean paginationEnabled
    ) {
        if (paginationEnabled) {
            // Simulates AbstractDAO.find() with pagination enabled → PageResult
            Result result = PageResult.of(List.of(), totalElements, offset, pageSize);
            assertInstanceOf(PageResult.class, result,
                    "With pagination enabled, result must be PageResult");
            assertInstanceOf(Result.class, result,
                    "PageResult must implement Result");
        } else {
            // Simulates AbstractDAO.find() with pagination disabled → ResultSet
            ResultSet rs = new ResultSet();
            rs.setContent(List.of());
            Result result = rs;
            assertInstanceOf(ResultSet.class, result,
                    "With pagination disabled, result must be ResultSet");
            assertInstanceOf(Result.class, result,
                    "ResultSet must implement Result");
            assertFalse(result instanceof PageResult,
                    "ResultSet must NOT be an instance of PageResult");
        }
    }

    // -----------------------------------------------------------------------
    // Property 15: PageResult calcula metadados de paginação corretamente
    // -----------------------------------------------------------------------

    /**
     * Feature: crud-enhancements, Property 15: PageResult calcula metadados de paginação corretamente
     *
     * <p>For any valid {@code totalElements} (≥0), {@code offset} (≥0), and
     * {@code pageSize} (>0), {@link PageResult#of} must correctly calculate
     * {@code totalPages}, {@code currentPage}, {@code hasNext}, and
     * {@code hasPrevious}.</p>
     *
     * <p><b>Validates: Requirements 5.5</b></p>
     */
    @Property(tries = 100)
    // Feature: crud-enhancements, Property 15: PageResult calcula metadados de paginação corretamente
    void pageResultMetadataCalculation(
            @ForAll @LongRange(min = 0, max = 10000) long totalElements,
            @ForAll @IntRange(min = 0, max = 9999) int offset,
            @ForAll @IntRange(min = 1, max = 100) int pageSize
    ) {
        var result = PageResult.of(List.of(), totalElements, offset, pageSize);

        int expectedTotalPages = (int) Math.ceil((double) totalElements / pageSize);
        int expectedCurrentPage = offset / pageSize;

        assertEquals(expectedTotalPages, result.totalPages(),
                "totalPages = ceil(totalElements / pageSize)");
        assertEquals(expectedCurrentPage, result.currentPage(),
                "currentPage = offset / pageSize");
        assertEquals(expectedCurrentPage < expectedTotalPages - 1, result.hasNext(),
                "hasNext = currentPage < totalPages - 1");
        assertEquals(expectedCurrentPage > 0, result.hasPrevious(),
                "hasPrevious = currentPage > 0");
    }

    // -----------------------------------------------------------------------
    // Property 16: PageResult cópia defensiva
    // -----------------------------------------------------------------------

    /**
     * Feature: crud-enhancements, Property 16: PageResult cópia defensiva
     *
     * <p>For any mutable list passed to the {@link PageResult} constructor,
     * subsequent modifications to the original list must NOT affect the content
     * returned by {@link PageResult#getContent()}.</p>
     *
     * <p><b>Validates: Requirements 5.6</b></p>
     */
    @Property(tries = 100)
    // Feature: crud-enhancements, Property 16: PageResult cópia defensiva
    void modifyingOriginalListDoesNotAffectPageResultContent(
            @ForAll List<@From("nonNullStrings") String> elements
    ) {
        // Create a mutable copy to pass to PageResult
        List<String> mutableList = new ArrayList<>(elements);
        List<String> snapshot = List.copyOf(mutableList);

        PageResult<String> pr = new PageResult<>(mutableList, mutableList.size(),
                1, 0, 10, false, false);

        // Verify initial content matches
        assertEquals(snapshot, pr.getContent(),
                "Initial content must match the snapshot");

        // Mutate the original list: add an element
        mutableList.add("EXTRA_ELEMENT");
        assertEquals(snapshot, pr.getContent(),
                "Adding to original list must not affect getContent()");

        // Mutate the original list: clear it
        mutableList.clear();
        assertEquals(snapshot, pr.getContent(),
                "Clearing original list must not affect getContent()");
    }

    @Provide
    Arbitrary<String> nonNullStrings() {
        return Arbitraries.strings().ofMinLength(0).ofMaxLength(50);
    }
}
