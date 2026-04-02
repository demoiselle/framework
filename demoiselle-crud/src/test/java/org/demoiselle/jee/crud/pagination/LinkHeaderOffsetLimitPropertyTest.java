/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for offset-limit consistency in Link header URIs.
 *
 * <p><b>Validates: Requirements 5.1, 5.4</b></p>
 */
// Feature: rfc-standards-compliance, Property 11: Invariante offset-limit consistente nas URIs do Link
class LinkHeaderOffsetLimitPropertyTest {

    /**
     * Pattern to match individual Link entries: {@code <uri>; rel="xxx"}.
     * Captures the URI (group 1) and the rel value (group 2).
     */
    private static final Pattern LINK_ENTRY_PATTERN =
            Pattern.compile("<([^>]+)>;\\s*rel=\"([^\"]+)\"");

    /**
     * Pattern to extract offset and limit from the range query parameter: {@code range=offset-limit}.
     */
    private static final Pattern RANGE_PATTERN =
            Pattern.compile("[?&]range=(\\d+)-(\\d+)");

    /**
     * Parses a Link header string into a map of rel → (offset, limit) pairs.
     */
    private static Map<String, int[]> parseRelOffsetLimit(String linkHeader) {
        Map<String, int[]> result = new HashMap<>();
        Matcher entryMatcher = LINK_ENTRY_PATTERN.matcher(linkHeader);
        while (entryMatcher.find()) {
            String uri = entryMatcher.group(1);
            String rel = entryMatcher.group(2);
            Matcher rangeMatcher = RANGE_PATTERN.matcher(uri);
            assertTrue(rangeMatcher.find(),
                    "Link entry for rel=\"" + rel + "\" must contain a range parameter, URI: " + uri);
            int offset = Integer.parseInt(rangeMatcher.group(1));
            int limit = Integer.parseInt(rangeMatcher.group(2));
            result.put(rel, new int[]{offset, limit});
        }
        return result;
    }

    /**
     * For any PageResult with totalPages > 1 and pageSize > 0, parsing the URIs
     * from the Link header and extracting offset/limit from the range parameter
     * must produce values consistent with the PageResult metadata:
     *
     * <ul>
     *   <li>For "first": offset must be 0</li>
     *   <li>For "last": offset must be (totalPages - 1) * pageSize</li>
     *   <li>For all relations: limit - offset + 1 must equal pageSize</li>
     *   <li>For "next": offset must be (currentPage + 1) * pageSize</li>
     *   <li>For "prev": offset must be (currentPage - 1) * pageSize</li>
     * </ul>
     */
    @Property(tries = 100)
    // Feature: rfc-standards-compliance, Property 11: Invariante offset-limit consistente nas URIs do Link
    void offsetLimitInLinkHeaderMustBeConsistentWithPageResult(
            @ForAll @LongRange(min = 2, max = 10_000) long totalElements,
            @ForAll @IntRange(min = 1, max = 200) int pageSize,
            @ForAll @IntRange(min = 0, max = 9_999) int rawOffset
    ) {
        // Ensure totalPages > 1 by requiring totalElements > pageSize
        Assume.that(totalElements > pageSize);

        // Clamp offset to a valid page boundary within totalElements
        int maxOffset = (int) Math.max(0, totalElements - 1);
        int offset = Math.min(rawOffset, maxOffset);
        // Align offset to page boundary
        offset = (offset / pageSize) * pageSize;

        PageResult<String> pageResult = PageResult.of(List.of("item"), totalElements, offset, pageSize);

        // Precondition: totalPages > 1
        Assume.that(pageResult.totalPages() > 1);

        String linkHeader = LinkHeaderBuilder.build("/api/resource", pageResult);

        // Must not be empty when totalPages > 1
        assertFalse(linkHeader.isEmpty(),
                "Link header must not be empty when totalPages > 1");

        Map<String, int[]> relOffsetLimit = parseRelOffsetLimit(linkHeader);

        int ps = pageResult.pageSize();
        int currentPage = pageResult.currentPage();
        int totalPages = pageResult.totalPages();

        // Verify "first" relation
        assertTrue(relOffsetLimit.containsKey("first"), "Must contain rel=\"first\"");
        int[] first = relOffsetLimit.get("first");
        assertEquals(0, first[0],
                "first: offset must be 0");
        assertEquals(ps, first[1] - first[0] + 1,
                "first: limit - offset + 1 must equal pageSize");

        // Verify "last" relation
        assertTrue(relOffsetLimit.containsKey("last"), "Must contain rel=\"last\"");
        int[] last = relOffsetLimit.get("last");
        assertEquals((totalPages - 1) * ps, last[0],
                "last: offset must be (totalPages - 1) * pageSize");
        assertEquals(ps, last[1] - last[0] + 1,
                "last: limit - offset + 1 must equal pageSize");

        // Verify "next" relation (if present)
        if (relOffsetLimit.containsKey("next")) {
            assertTrue(pageResult.hasNext(), "next present implies hasNext == true");
            int[] next = relOffsetLimit.get("next");
            assertEquals((currentPage + 1) * ps, next[0],
                    "next: offset must be (currentPage + 1) * pageSize");
            assertEquals(ps, next[1] - next[0] + 1,
                    "next: limit - offset + 1 must equal pageSize");
        }

        // Verify "prev" relation (if present)
        if (relOffsetLimit.containsKey("prev")) {
            assertTrue(pageResult.hasPrevious(), "prev present implies hasPrevious == true");
            int[] prev = relOffsetLimit.get("prev");
            assertEquals((currentPage - 1) * ps, prev[0],
                    "prev: offset must be (currentPage - 1) * pageSize");
            assertEquals(ps, prev[1] - prev[0] + 1,
                    "prev: limit - offset + 1 must equal pageSize");
        }
    }
}
