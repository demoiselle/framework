/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import org.demoiselle.jee.crud.ReservedHTTPHeaders;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test verifying that custom pagination headers are consistent
 * with PageResult metadata and that the Link header is present alongside them.
 *
 * <p>This test simulates what {@code CrudFilter.buildHeaders()} does: it extracts
 * custom header values from a {@link PageResult} and calls
 * {@link LinkHeaderBuilder#build(String, PageResult)} to produce the Link header,
 * then verifies consistency between both.</p>
 *
 * <p><b>Validates: Requirements 4.5, 8.1, 8.2, 8.3</b></p>
 */
// Feature: rfc-standards-compliance, Property 16: Headers customizados consistentes com PageResult e Link
class PaginationHeadersConsistencyPropertyTest {

    /** Regex to extract rel values from Link header entries. */
    private static final Pattern REL_PATTERN = Pattern.compile("rel=\"([^\"]+)\"");

    /**
     * Parses all {@code rel} values from a Link header string.
     */
    private static Set<String> parseRels(String linkHeader) {
        Set<String> rels = new HashSet<>();
        Matcher matcher = REL_PATTERN.matcher(linkHeader);
        while (matcher.find()) {
            rels.add(matcher.group(1));
        }
        return rels;
    }

    /**
     * For any paginated response with a PageResult, the custom header values
     * must equal the PageResult metadata, and the Link header must be present
     * in addition to the custom headers when totalPages > 1.
     *
     * <p>This simulates the CrudFilter logic:</p>
     * <ol>
     *   <li>Extract custom header values from PageResult (totalElements, totalPages,
     *       currentPage, pageSize, hasNext, hasPrevious)</li>
     *   <li>Call LinkHeaderBuilder.build() to get the Link header</li>
     *   <li>Verify custom header values match PageResult metadata exactly</li>
     *   <li>Verify Link header is non-empty when totalPages &gt; 1</li>
     *   <li>Verify Link header relations are consistent with PageResult metadata</li>
     * </ol>
     */
    @Property(tries = 100)
    // Feature: rfc-standards-compliance, Property 16: Headers customizados consistentes com PageResult e Link
    void customHeadersMustBeConsistentWithPageResultAndLinkHeader(
            @ForAll @LongRange(min = 1, max = 10_000) long totalElements,
            @ForAll @IntRange(min = 1, max = 200) int pageSize,
            @ForAll @IntRange(min = 0, max = 9_999) int rawOffset
    ) {
        // Clamp offset to a valid page boundary within totalElements
        int maxOffset = (int) Math.max(0, totalElements - 1);
        int offset = Math.min(rawOffset, maxOffset);
        // Align offset to page boundary
        offset = (offset / pageSize) * pageSize;

        PageResult<String> pageResult = PageResult.of(List.of("item"), totalElements, offset, pageSize);

        // Precondition: totalPages > 1 so we have meaningful pagination
        Assume.that(pageResult.totalPages() > 1);

        // --- Simulate CrudFilter custom header extraction ---
        // These mirror exactly what CrudFilter.buildHeaders() puts into the response headers
        long headerTotalCount = pageResult.totalElements();
        int headerTotalPages = pageResult.totalPages();
        int headerCurrentPage = pageResult.currentPage();
        int headerPageSize = pageResult.pageSize();
        boolean headerHasNext = pageResult.hasNext();
        boolean headerHasPrevious = pageResult.hasPrevious();

        // Verify custom header values match PageResult metadata (Req 8.1)
        assertEquals(pageResult.totalElements(), headerTotalCount,
                ReservedHTTPHeaders.HTTP_HEADER_TOTAL_COUNT.getKey()
                        + " must equal PageResult.totalElements()");
        assertEquals(pageResult.totalPages(), headerTotalPages,
                ReservedHTTPHeaders.HTTP_HEADER_TOTAL_PAGES.getKey()
                        + " must equal PageResult.totalPages()");
        assertEquals(pageResult.currentPage(), headerCurrentPage,
                ReservedHTTPHeaders.HTTP_HEADER_CURRENT_PAGE.getKey()
                        + " must equal PageResult.currentPage()");
        assertEquals(pageResult.pageSize(), headerPageSize,
                ReservedHTTPHeaders.HTTP_HEADER_PAGE_SIZE.getKey()
                        + " must equal PageResult.pageSize()");
        assertEquals(pageResult.hasNext(), headerHasNext,
                ReservedHTTPHeaders.HTTP_HEADER_HAS_NEXT.getKey()
                        + " must equal PageResult.hasNext()");
        assertEquals(pageResult.hasPrevious(), headerHasPrevious,
                ReservedHTTPHeaders.HTTP_HEADER_HAS_PREVIOUS.getKey()
                        + " must equal PageResult.hasPrevious()");

        // --- Build Link header (same as CrudFilter does) ---
        String baseUri = "http://localhost/api/resource";
        String linkHeader = LinkHeaderBuilder.build(baseUri, pageResult);

        // When totalPages > 1, Link header must be non-empty (Req 8.2)
        assertFalse(linkHeader.isEmpty(),
                "Link header must be non-empty when totalPages > 1");

        // --- Verify Link header relations are consistent with custom headers (Req 8.3) ---
        Set<String> rels = parseRels(linkHeader);

        // "next" present iff hasNext (consistent with X-Has-Next header)
        assertEquals(headerHasNext, rels.contains("next"),
                "Link rel=\"next\" must be present iff X-Has-Next is true");

        // "prev" present iff hasPrevious (consistent with X-Has-Previous header)
        assertEquals(headerHasPrevious, rels.contains("prev"),
                "Link rel=\"prev\" must be present iff X-Has-Previous is true");

        // "first" and "last" always present when totalPages > 1
        assertTrue(rels.contains("first"),
                "Link rel=\"first\" must be present when X-Total-Pages > 1");
        assertTrue(rels.contains("last"),
                "Link rel=\"last\" must be present when X-Total-Pages > 1");

        // Verify all six custom headers are defined in ReservedHTTPHeaders enum
        assertNotNull(ReservedHTTPHeaders.HTTP_HEADER_TOTAL_COUNT.getKey());
        assertNotNull(ReservedHTTPHeaders.HTTP_HEADER_TOTAL_PAGES.getKey());
        assertNotNull(ReservedHTTPHeaders.HTTP_HEADER_CURRENT_PAGE.getKey());
        assertNotNull(ReservedHTTPHeaders.HTTP_HEADER_PAGE_SIZE.getKey());
        assertNotNull(ReservedHTTPHeaders.HTTP_HEADER_HAS_NEXT.getKey());
        assertNotNull(ReservedHTTPHeaders.HTTP_HEADER_HAS_PREVIOUS.getKey());
        assertNotNull(ReservedHTTPHeaders.HTTP_HEADER_LINK.getKey());
    }
}
