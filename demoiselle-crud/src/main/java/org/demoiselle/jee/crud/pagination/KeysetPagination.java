/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.demoiselle.jee.core.api.pagination.Cursor;
import org.demoiselle.jee.crud.sort.CrudSort;
import org.demoiselle.jee.crud.sort.SortModel;
import org.demoiselle.jee.crud.specification.Specification;

/**
 * Additive keyset (cursor) pagination helper for CRUD queries.
 *
 * <p>
 * This helper is <strong>additive</strong>: it does not replace the existing
 * offset/limit ({@code range}) machinery. It builds a JPA {@link Specification}
 * that restricts a query to rows strictly after (or before) the boundary row
 * described by a {@link Cursor}, so an application can offer cursor pagination on
 * top of the existing sort/filter/range pipeline without changing default
 * behaviour.
 * </p>
 *
 * <h2>Determinism</h2>
 * <p>
 * Keyset pagination requires a <em>total order</em>. Callers must supply a list
 * of {@link SortModel} whose last entry is a unique tie-breaker (typically the
 * primary key). This helper validates that at least one sort key is present and
 * builds the lexicographic keyset predicate accordingly, guaranteeing a
 * deterministic "next page".
 * </p>
 *
 * <p>
 * Values are carried as strings in the {@link Cursor} and converted back to the
 * comparable field type by a caller-supplied {@code valueConverter}, keeping the
 * codec fully broker/JPA-agnostic.
 * </p>
 *
 * @author SERPRO
 */
public final class KeysetPagination {

    private KeysetPagination() {
    }

    /**
     * Builds the keyset predicate for the given cursor and sort keys.
     *
     * <p>
     * For ascending keys {@code (k1, k2, ..., kn)} and an {@code AFTER} cursor
     * with boundary values {@code (v1, ..., vn)} the generated predicate is the
     * standard lexicographic comparison:
     * </p>
     * <pre>{@code
     * (k1 > v1)
     *   OR (k1 = v1 AND k2 > v2)
     *   OR (k1 = v1 AND k2 = v2 AND k3 > v3)
     *   ...
     * }</pre>
     * Descending keys invert the strict comparison; {@code BEFORE} inverts the
     * whole direction.
     *
     * @param cursor         the decoded cursor
     * @param sortKeys       the ordered sort keys (last one must be unique)
     * @param valueConverter converts the string cursor value for a field into a
     *                       {@link Comparable} of the field's type
     * @param <T>            the entity type
     * @return a {@link Specification} implementing the keyset comparison
     */
    public static <T> Specification<T> toSpecification(
            Cursor cursor,
            List<SortModel> sortKeys,
            Function<Map.Entry<String, String>, Comparable<?>> valueConverter) {

        Objects.requireNonNull(cursor, "cursor");
        Objects.requireNonNull(sortKeys, "sortKeys");
        Objects.requireNonNull(valueConverter, "valueConverter");
        if (sortKeys.isEmpty()) {
            throw new IllegalArgumentException("keyset pagination requires at least one sort key");
        }

        // Snapshot ordered boundary values aligned with the sort keys.
        Map<String, String> boundary = cursor.orderedKeys();
        List<SortModel> keys = new ArrayList<>(sortKeys);

        return (root, query, cb) -> buildPredicate(cursor, boundary, keys, valueConverter, root, cb);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> Predicate buildPredicate(
            Cursor cursor,
            Map<String, String> boundary,
            List<SortModel> keys,
            Function<Map.Entry<String, String>, Comparable<?>> valueConverter,
            Root<T> root,
            CriteriaBuilder cb) {

        boolean reverse = cursor.direction() == Cursor.Direction.BEFORE;
        List<Predicate> orTerms = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++) {
            List<Predicate> andTerms = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                SortModel eqKey = keys.get(j);
                andTerms.add(cb.equal(path(root, eqKey.field()), value(eqKey, boundary, valueConverter)));
            }
            SortModel strictKey = keys.get(i);
            boolean ascending = (strictKey.type() == CrudSort.ASC) ^ reverse;
            Expression<Comparable> expr = (Expression<Comparable>) (Expression<?>) path(root, strictKey.field());
            Comparable strictValue = (Comparable) value(strictKey, boundary, valueConverter);
            andTerms.add(ascending
                    ? cb.greaterThan(expr, strictValue)
                    : cb.lessThan(expr, strictValue));
            orTerms.add(cb.and(andTerms.toArray(new Predicate[0])));
        }

        return cb.or(orTerms.toArray(new Predicate[0]));
    }

    private static <T> Path<?> path(Root<T> root, String field) {
        Path<?> path = root;
        for (String segment : field.split("\\.")) {
            path = path.get(segment);
        }
        return path;
    }

    private static Comparable<?> value(SortModel key, Map<String, String> boundary,
            Function<Map.Entry<String, String>, Comparable<?>> valueConverter) {
        String raw = boundary.get(key.field());
        return valueConverter.apply(Map.entry(key.field(), raw == null ? "" : raw));
    }

    /**
     * Extracts a next-page {@link Cursor} from the boundary (typically last) row
     * of a page, using the sort keys to build the keyset. The returned cursor
     * uses {@link Cursor.Direction#AFTER}.
     *
     * @param sortKeys       the ordered sort keys (last must be unique)
     * @param row            the boundary row
     * @param valueExtractor extracts the string value of a field from the row
     * @param ttlSeconds     cursor TTL in seconds ({@code 0} for no expiry)
     * @param nowEpochSeconds current time in epoch seconds
     * @param <R>            the row type
     * @return the next-page cursor
     */
    public static <R> Cursor toCursor(
            List<SortModel> sortKeys,
            R row,
            Function<String, String> valueExtractor,
            long ttlSeconds,
            long nowEpochSeconds) {

        Objects.requireNonNull(sortKeys, "sortKeys");
        Objects.requireNonNull(row, "row");
        Objects.requireNonNull(valueExtractor, "valueExtractor");
        if (sortKeys.isEmpty()) {
            throw new IllegalArgumentException("keyset pagination requires at least one sort key");
        }

        Map<String, String> keys = new LinkedHashMap<>();
        for (SortModel key : sortKeys) {
            keys.put(key.field(), valueExtractor.apply(key.field()));
        }
        long expiresAt = ttlSeconds > 0 ? nowEpochSeconds + ttlSeconds : 0L;
        return new Cursor(keys, Cursor.Direction.AFTER, expiresAt);
    }
}
