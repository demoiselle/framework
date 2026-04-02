/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds RFC 8288 compliant {@code Link} headers from {@link PageResult} metadata.
 * <p>
 * This utility class generates pagination link relations ({@code first}, {@code last},
 * {@code next}, {@code prev}) using the {@code range} query parameter format
 * ({@code offset-limit}).
 * <p>
 * Multiple relations are separated by comma as specified in RFC 8288 §3.
 */
public final class LinkHeaderBuilder {

    private LinkHeaderBuilder() {
        // utility class
    }

    /**
     * Builds the Link header value from a base URI and page result metadata.
     *
     * @param baseUri    the base URI of the request (may contain existing query parameters)
     * @param pageResult pagination metadata
     * @return the Link header value, or empty string when not applicable
     */
    public static String build(String baseUri, PageResult<?> pageResult) {
        if (baseUri == null || baseUri.isEmpty()) {
            return "";
        }
        if (pageResult.totalPages() <= 1 || pageResult.pageSize() == 0) {
            return "";
        }

        int pageSize = pageResult.pageSize();
        int currentPage = pageResult.currentPage();
        int totalPages = pageResult.totalPages();

        List<String> links = new ArrayList<>();

        // rel="first" — always present when totalPages > 1
        links.add(formatLink(buildPageUri(baseUri, 0, pageSize - 1), "first"));

        // rel="prev" — present when hasPrevious
        if (pageResult.hasPrevious()) {
            int prevOffset = (currentPage - 1) * pageSize;
            int prevLimit = currentPage * pageSize - 1;
            links.add(formatLink(buildPageUri(baseUri, prevOffset, prevLimit), "prev"));
        }

        // rel="next" — present when hasNext
        if (pageResult.hasNext()) {
            int nextOffset = (currentPage + 1) * pageSize;
            int nextLimit = (currentPage + 2) * pageSize - 1;
            links.add(formatLink(buildPageUri(baseUri, nextOffset, nextLimit), "next"));
        }

        // rel="last" — always present when totalPages > 1
        int lastOffset = (totalPages - 1) * pageSize;
        int lastLimit = totalPages * pageSize - 1;
        links.add(formatLink(buildPageUri(baseUri, lastOffset, lastLimit), "last"));

        return String.join(", ", links);
    }

    /**
     * Builds a pagination URI by appending the {@code range} parameter to the base URI.
     * Existing query parameters in the base URI are preserved.
     *
     * @param baseUri the base URI (may contain existing query parameters)
     * @param offset  the range start value
     * @param limit   the range end value
     * @return the URI string with the range parameter appended
     */
    static String buildPageUri(String baseUri, int offset, int limit) {
        String separator = baseUri.contains("?") ? "&" : "?";
        return baseUri + separator + "range=" + offset + "-" + limit;
    }

    private static String formatLink(String uri, String rel) {
        return "<" + uri + ">; rel=\"" + rel + "\"";
    }
}
