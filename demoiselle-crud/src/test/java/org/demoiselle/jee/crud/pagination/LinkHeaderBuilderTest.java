/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LinkHeaderBuilder}.
 */
class LinkHeaderBuilderTest {

    @Test
    void shouldReturnEmptyStringWhenBaseUriIsNull() {
        PageResult<String> pr = PageResult.of(List.of("a"), 20, 0, 10);
        assertEquals("", LinkHeaderBuilder.build(null, pr));
    }

    @Test
    void shouldReturnEmptyStringWhenBaseUriIsEmpty() {
        PageResult<String> pr = PageResult.of(List.of("a"), 20, 0, 10);
        assertEquals("", LinkHeaderBuilder.build("", pr));
    }

    @Test
    void shouldReturnEmptyStringWhenSinglePage() {
        PageResult<String> pr = PageResult.of(List.of("a"), 5, 0, 10);
        assertEquals("", LinkHeaderBuilder.build("/api/resource", pr));
    }

    @Test
    void shouldReturnEmptyStringWhenPageSizeIsZero() {
        PageResult<String> pr = new PageResult<>(List.of(), 0, 0, 0, 0, false, false);
        assertEquals("", LinkHeaderBuilder.build("/api/resource", pr));
    }

    @Test
    void shouldGenerateFirstAndLastOnFirstPage() {
        // 30 elements, page size 10, offset 0 → page 0 of 3
        PageResult<String> pr = PageResult.of(List.of("a"), 30, 0, 10);
        String link = LinkHeaderBuilder.build("/api/resource", pr);

        assertTrue(link.contains("rel=\"first\""));
        assertTrue(link.contains("rel=\"last\""));
        assertTrue(link.contains("rel=\"next\""));
        assertFalse(link.contains("rel=\"prev\""));
        assertTrue(link.contains("range=0-9"));    // first
        assertTrue(link.contains("range=10-19"));   // next
        assertTrue(link.contains("range=20-29"));   // last
    }

    @Test
    void shouldGenerateAllRelationsOnMiddlePage() {
        // 30 elements, page size 10, offset 10 → page 1 of 3
        PageResult<String> pr = PageResult.of(List.of("a"), 30, 10, 10);
        String link = LinkHeaderBuilder.build("/api/resource", pr);

        assertTrue(link.contains("rel=\"first\""));
        assertTrue(link.contains("rel=\"last\""));
        assertTrue(link.contains("rel=\"next\""));
        assertTrue(link.contains("rel=\"prev\""));
        assertTrue(link.contains("range=0-9"));    // first / prev
        assertTrue(link.contains("range=20-29"));   // next / last
    }

    @Test
    void shouldGenerateFirstLastAndPrevOnLastPage() {
        // 30 elements, page size 10, offset 20 → page 2 of 3 (last)
        PageResult<String> pr = PageResult.of(List.of("a"), 30, 20, 10);
        String link = LinkHeaderBuilder.build("/api/resource", pr);

        assertTrue(link.contains("rel=\"first\""));
        assertTrue(link.contains("rel=\"last\""));
        assertFalse(link.contains("rel=\"next\""));
        assertTrue(link.contains("rel=\"prev\""));
    }

    @Test
    void shouldPreserveExistingQueryParameters() {
        PageResult<String> pr = PageResult.of(List.of("a"), 30, 0, 10);
        String link = LinkHeaderBuilder.build("/api/resource?filter=active&sort=name", pr);

        // All link URIs should contain the original query params
        assertTrue(link.contains("filter=active&sort=name&range="));
    }

    @Test
    void shouldSeparateRelationsWithComma() {
        PageResult<String> pr = PageResult.of(List.of("a"), 30, 10, 10);
        String link = LinkHeaderBuilder.build("/api/resource", pr);

        // Should have commas separating relations
        String[] parts = link.split(", ");
        assertEquals(4, parts.length); // first, prev, next, last
    }

    @Test
    void shouldBuildPageUriWithoutExistingParams() {
        String uri = LinkHeaderBuilder.buildPageUri("/api/resource", 10, 19);
        assertEquals("/api/resource?range=10-19", uri);
    }

    @Test
    void shouldBuildPageUriWithExistingParams() {
        String uri = LinkHeaderBuilder.buildPageUri("/api/resource?sort=name", 10, 19);
        assertEquals("/api/resource?sort=name&range=10-19", uri);
    }

    @Test
    void shouldHandleTwoPages() {
        // 20 elements, page size 10, offset 0 → page 0 of 2
        PageResult<String> pr = PageResult.of(List.of("a"), 20, 0, 10);
        String link = LinkHeaderBuilder.build("/api/resource", pr);

        assertTrue(link.contains("rel=\"first\""));
        assertTrue(link.contains("rel=\"last\""));
        assertTrue(link.contains("rel=\"next\""));
        assertFalse(link.contains("rel=\"prev\""));
        assertTrue(link.contains("range=0-9"));    // first
        assertTrue(link.contains("range=10-19"));   // next and last
    }
}
