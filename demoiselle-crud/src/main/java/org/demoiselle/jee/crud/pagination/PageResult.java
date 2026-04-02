/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.List;

import org.demoiselle.jee.core.api.crud.Result;

/**
 * Immutable, typed pagination result that implements {@link Result}.
 * <p>
 * Use the factory method {@link #of(List, long, int, int)} to create instances
 * with automatically calculated pagination metadata.
 *
 * @param <T> the entity type
 */
public record PageResult<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious
) implements Result<T> {

    /**
     * Compact constructor — defensive copy of content.
     */
    public PageResult {
        content = content == null ? List.of() : List.copyOf(content);
    }

    /**
     * Factory method that calculates pagination metadata from raw values.
     *
     * @param content       the page content
     * @param totalElements total number of elements across all pages
     * @param offset        zero-based offset of the first element in this page
     * @param pageSize      maximum number of elements per page
     * @param <T>           the entity type
     * @return a new {@code PageResult} with computed metadata
     */
    public static <T> PageResult<T> of(List<T> content, long totalElements, int offset, int pageSize) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        int currentPage = pageSize > 0 ? offset / pageSize : 0;
        boolean hasNext = currentPage < totalPages - 1;
        boolean hasPrevious = currentPage > 0;
        return new PageResult<>(content, totalElements, totalPages, currentPage, pageSize, hasNext, hasPrevious);
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public void setContent(List<T> content) {
        throw new UnsupportedOperationException("PageResult é imutável");
    }
}
