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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Link header relation generation from PageResult.
 *
 * <p><b>Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.7, 5.2, 5.3, 5.5</b></p>
 */
// Feature: rfc-standards-compliance, Property 10: Relações do header Link metamórficas com PageResult
class LinkHeaderRelationsPropertyTest {

    /** Regex to extract rel values from Link header entries like {@code <uri>; rel="xxx"}. */
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
     * For any PageResult with totalPages > 0 and pageSize > 0, parsing the
     * Link header relations produced by LinkHeaderBuilder.build() must yield
     * exactly the expected relations based on hasNext, hasPrevious and totalPages.
     *
     * <ul>
     *   <li>"next" is present iff hasNext == true</li>
     *   <li>"prev" is present iff hasPrevious == true</li>
     *   <li>"first" and "last" are present iff totalPages &gt; 1</li>
     *   <li>When totalPages &lt;= 1, the result is an empty string</li>
     * </ul>
     */
    @Property(tries = 100)
    // Feature: rfc-standards-compliance, Property 10: Relações do header Link metamórficas com PageResult
    void linkHeaderRelationsMatchPageResultMetadata(
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

        // Precondition: totalPages > 0 and pageSize > 0 (guaranteed by generators)
        assertTrue(pageResult.totalPages() > 0, "totalPages must be > 0");
        assertTrue(pageResult.pageSize() > 0, "pageSize must be > 0");

        String linkHeader = LinkHeaderBuilder.build("/api/resource", pageResult);

        if (pageResult.totalPages() <= 1) {
            // When totalPages <= 1, the result must be empty string
            assertEquals("", linkHeader,
                    "Link header must be empty when totalPages <= 1");
            return;
        }

        // totalPages > 1: parse relations
        Set<String> rels = parseRels(linkHeader);

        // "next" present iff hasNext == true
        assertEquals(pageResult.hasNext(), rels.contains("next"),
                "rel=\"next\" must be present iff hasNext is true");

        // "prev" present iff hasPrevious == true
        assertEquals(pageResult.hasPrevious(), rels.contains("prev"),
                "rel=\"prev\" must be present iff hasPrevious is true");

        // "first" and "last" always present when totalPages > 1
        assertTrue(rels.contains("first"),
                "rel=\"first\" must be present when totalPages > 1");
        assertTrue(rels.contains("last"),
                "rel=\"last\" must be present when totalPages > 1");
    }
}
