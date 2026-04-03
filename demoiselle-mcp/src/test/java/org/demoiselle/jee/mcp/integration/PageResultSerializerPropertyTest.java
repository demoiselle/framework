/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import net.jqwik.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link PageResultSerializer}.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Property 17: Serialização de PageResult com metadados de paginação</li>
 *   <li>Property 25: PageResult como objeto simples sem demoiselle-crud</li>
 * </ul>
 */
class PageResultSerializerPropertyTest {

    // ── Mock PageResult for testing ──

    /**
     * Simple mock that mimics a PageResult with getter methods,
     * usable via reflection by PageResultSerializer.
     */
    static class MockPageResult {
        private final List<String> content;
        private final long totalElements;
        private final int totalPages;
        private final int currentPage;
        private final int pageSize;
        private final boolean hasNext;
        private final boolean hasPrevious;

        MockPageResult(List<String> content, long totalElements, int totalPages,
                       int currentPage, int pageSize, boolean hasNext, boolean hasPrevious) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        public List<String> getContent() { return content; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
        public boolean isHasNext() { return hasNext; }
        public boolean isHasPrevious() { return hasPrevious; }
    }

    /**
     * A plain POJO without pagination methods — simulates an object that
     * is NOT a PageResult.
     */
    static class PlainPojo {
        private final String name;
        private final int value;

        PlainPojo(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    // ── Arbitraries ──

    @Provide
    Arbitrary<MockPageResult> pageResults() {
        return Combinators.combine(
                Arbitraries.integers().between(0, 50),   // content size
                Arbitraries.longs().between(0, 10000),   // totalElements
                Arbitraries.integers().between(1, 100),  // totalPages
                Arbitraries.integers().between(0, 99),   // currentPage
                Arbitraries.integers().between(1, 50),   // pageSize
                Arbitraries.of(true, false),             // hasNext
                Arbitraries.of(true, false)              // hasPrevious
        ).as((contentSize, totalElements, totalPages, currentPage, pageSize, hasNext, hasPrevious) -> {
            List<String> content = new ArrayList<>();
            for (int i = 0; i < contentSize; i++) {
                content.add("item-" + i);
            }
            return new MockPageResult(content, totalElements, totalPages,
                    currentPage, pageSize, hasNext, hasPrevious);
        });
    }

    @Provide
    Arbitrary<MockPageResult> pageResultsWithHasNext() {
        return Combinators.combine(
                Arbitraries.integers().between(1, 20),
                Arbitraries.longs().between(10, 10000),
                Arbitraries.integers().between(2, 100),
                Arbitraries.integers().between(0, 98),
                Arbitraries.integers().between(1, 50)
        ).as((contentSize, totalElements, totalPages, currentPage, pageSize) -> {
            List<String> content = new ArrayList<>();
            for (int i = 0; i < contentSize; i++) {
                content.add("item-" + i);
            }
            return new MockPageResult(content, totalElements, totalPages,
                    currentPage, pageSize, true, currentPage > 0);
        });
    }

    @Provide
    Arbitrary<MockPageResult> pageResultsWithoutHasNext() {
        return Combinators.combine(
                Arbitraries.integers().between(0, 20),
                Arbitraries.longs().between(0, 10000),
                Arbitraries.integers().between(1, 100),
                Arbitraries.integers().between(0, 99),
                Arbitraries.integers().between(1, 50)
        ).as((contentSize, totalElements, totalPages, currentPage, pageSize) -> {
            List<String> content = new ArrayList<>();
            for (int i = 0; i < contentSize; i++) {
                content.add("item-" + i);
            }
            return new MockPageResult(content, totalElements, totalPages,
                    currentPage, pageSize, false, currentPage > 0);
        });
    }

    // -----------------------------------------------------------------------
    // Property 17: Serialização de PageResult com metadados de paginação
    // -----------------------------------------------------------------------

    /**
     * For any PageResult, the serialized output must contain content as an array
     * and all pagination metadata fields.
     *
     * <p><b>Validates: Requirements 11.1, 11.2</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 17: Serialização de PageResult com metadados de paginação
    void serializedPageResultContainsContentAndMetadata(
            @ForAll("pageResults") MockPageResult pageResult) {

        PageResultSerializer serializer = new PageResultSerializer();
        Map<String, Object> result = serializer.serialize(pageResult);

        // Content must be present as a list
        assertNotNull(result.get("content"), "content must be present");
        assertTrue(result.get("content") instanceof List,
                "content must be a List");

        @SuppressWarnings("unchecked")
        List<Object> content = (List<Object>) result.get("content");
        assertEquals(pageResult.getContent().size(), content.size(),
                "content size must match original");

        // Pagination metadata must be present
        assertEquals(pageResult.getTotalElements(), result.get("totalElements"),
                "totalElements must match");
        assertEquals(pageResult.getTotalPages(), result.get("totalPages"),
                "totalPages must match");
        assertEquals(pageResult.getCurrentPage(), result.get("currentPage"),
                "currentPage must match");
        assertEquals(pageResult.getPageSize(), result.get("pageSize"),
                "pageSize must match");
        assertEquals(pageResult.isHasNext(), result.get("hasNext"),
                "hasNext must match");
        assertEquals(pageResult.isHasPrevious(), result.get("hasPrevious"),
                "hasPrevious must match");
    }

    /**
     * When hasNext is true, the serialized output must include a nextCursor field.
     *
     * <p><b>Validates: Requirements 11.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 17: Serialização de PageResult com metadados de paginação
    void nextCursorPresentWhenHasNextIsTrue(
            @ForAll("pageResultsWithHasNext") MockPageResult pageResult) {

        PageResultSerializer serializer = new PageResultSerializer();
        Map<String, Object> result = serializer.serialize(pageResult);

        assertTrue((Boolean) result.get("hasNext"), "hasNext must be true");
        assertNotNull(result.get("nextCursor"),
                "nextCursor must be present when hasNext is true");

        // nextCursor should represent the next page
        String expectedCursor = String.valueOf(pageResult.getCurrentPage() + 1);
        assertEquals(expectedCursor, result.get("nextCursor"),
                "nextCursor must be currentPage + 1");
    }

    /**
     * When hasNext is false, the serialized output must NOT include a nextCursor field.
     *
     * <p><b>Validates: Requirements 11.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 17: Serialização de PageResult com metadados de paginação
    void nextCursorAbsentWhenHasNextIsFalse(
            @ForAll("pageResultsWithoutHasNext") MockPageResult pageResult) {

        PageResultSerializer serializer = new PageResultSerializer();
        Map<String, Object> result = serializer.serialize(pageResult);

        assertFalse((Boolean) result.get("hasNext"), "hasNext must be false");
        assertNull(result.get("nextCursor"),
                "nextCursor must NOT be present when hasNext is false");
    }

    // -----------------------------------------------------------------------
    // Property 25: PageResult como objeto simples sem demoiselle-crud
    // -----------------------------------------------------------------------

    /**
     * When an object is NOT a PageResult (no pagination methods), the serializer's
     * isPageResult check must return false, indicating it should be treated as
     * a plain POJO without special pagination formatting.
     *
     * <p><b>Validates: Requirements 19.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 25: PageResult como objeto simples sem demoiselle-crud
    void plainPojoIsNotRecognizedAsPageResult(
            @ForAll("plainPojos") PlainPojo pojo) {

        PageResultSerializer serializer = new PageResultSerializer();
        assertFalse(serializer.isPageResult(pojo),
                "Plain POJO must not be recognized as PageResult");
    }

    /**
     * A MockPageResult (with pagination methods) must be recognized as a page result.
     *
     * <p><b>Validates: Requirements 19.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 25: PageResult como objeto simples sem demoiselle-crud
    void mockPageResultIsRecognizedAsPageResult(
            @ForAll("pageResults") MockPageResult pageResult) {

        PageResultSerializer serializer = new PageResultSerializer();
        assertTrue(serializer.isPageResult(pageResult),
                "MockPageResult must be recognized as PageResult");
    }

    @Provide
    Arbitrary<PlainPojo> plainPojos() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
                Arbitraries.integers().between(0, 1000)
        ).as(PlainPojo::new);
    }
}
