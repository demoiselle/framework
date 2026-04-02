/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for query parameter preservation in Link header URIs.
 *
 * <p><b>Validates: Requirements 5.6</b></p>
 */
// Feature: rfc-standards-compliance, Property 12: Preservação de query parameters existentes no LinkHeaderBuilder
class LinkHeaderQueryParamsPropertyTest {

    /** Pattern to extract URIs from Link header entries: {@code <uri>; rel="xxx"}. */
    private static final Pattern LINK_URI_PATTERN = Pattern.compile("<([^>]+)>");

    /** Allowed characters for query parameter keys and values. */
    private static final String PARAM_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Extracts all URIs from a Link header string.
     */
    private static List<String> extractUris(String linkHeader) {
        List<String> uris = new ArrayList<>();
        Matcher matcher = LINK_URI_PATTERN.matcher(linkHeader);
        while (matcher.find()) {
            uris.add(matcher.group(1));
        }
        return uris;
    }

    /**
     * Provides an arbitrary query parameter key (alphabetic, 2-8 chars).
     */
    @Provide
    Arbitrary<String> paramKey() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(8);
    }

    /**
     * Provides an arbitrary query parameter value (alphanumeric, 1-10 chars).
     */
    @Provide
    Arbitrary<String> paramValue() {
        return Arbitraries.strings()
                .withChars(PARAM_CHARS.toCharArray())
                .ofMinLength(1)
                .ofMaxLength(10);
    }

    /**
     * Provides a list of 1-3 query parameter pairs as "key=value" strings.
     * Keys are guaranteed to not be "range" to avoid collision with the pagination parameter.
     */
    @Provide
    Arbitrary<List<String>> queryParams() {
        Arbitrary<String> param = Combinators.combine(paramKey(), paramValue())
                .as((k, v) -> k + "=" + v)
                .filter(p -> !p.startsWith("range="));
        return param.list().ofMinSize(1).ofMaxSize(3);
    }

    /**
     * For any base URI with 1-3 arbitrary query parameters and any valid PageResult
     * with totalPages > 1, all URIs in the Link header must contain the original
     * query parameters in addition to the range parameter.
     */
    @Property(tries = 100)
    // Feature: rfc-standards-compliance, Property 12: Preservação de query parameters existentes no LinkHeaderBuilder
    void linkHeaderPreservesOriginalQueryParameters(
            @ForAll("queryParams") List<String> params,
            @ForAll @LongRange(min = 2, max = 10_000) long totalElements,
            @ForAll @IntRange(min = 1, max = 200) int pageSize,
            @ForAll @IntRange(min = 0, max = 9_999) int rawOffset
    ) {
        // Ensure totalPages > 1
        Assume.that(totalElements > pageSize);

        // Clamp offset to a valid page boundary
        int maxOffset = (int) Math.max(0, totalElements - 1);
        int offset = Math.min(rawOffset, maxOffset);
        offset = (offset / pageSize) * pageSize;

        PageResult<String> pageResult = PageResult.of(List.of("item"), totalElements, offset, pageSize);
        Assume.that(pageResult.totalPages() > 1);

        // Build base URI with query parameters: /api/resource?key1=val1&key2=val2
        String queryString = String.join("&", params);
        String baseUri = "/api/resource?" + queryString;

        String linkHeader = LinkHeaderBuilder.build(baseUri, pageResult);

        // Link header must not be empty when totalPages > 1
        assertFalse(linkHeader.isEmpty(),
                "Link header must not be empty when totalPages > 1");

        // Extract all URIs from the Link header
        List<String> uris = extractUris(linkHeader);
        assertFalse(uris.isEmpty(), "Must have at least one Link entry");

        // Each URI must contain all original query parameters and the range parameter
        for (String uri : uris) {
            for (String param : params) {
                assertTrue(uri.contains(param),
                        "URI '" + uri + "' must contain original query parameter '" + param + "'");
            }
            assertTrue(uri.contains("range="),
                    "URI '" + uri + "' must contain the range parameter");
        }
    }
}
