/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.pagination;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Opaque keyset (a.k.a. cursor) describing the position of the last row of a
 * page, used to fetch the next page deterministically.
 *
 * <p>
 * A cursor carries the ordered set of sort-key <em>values</em> of the boundary
 * row (as strings), the {@link Direction} of traversal and an optional
 * expiration timestamp. Keyset pagination compares subsequent rows against these
 * values, which — unlike offset pagination — is stable under concurrent inserts
 * and deletes and preserves an existing offset/limit range additively.
 * </p>
 *
 * <p>
 * To guarantee a total order (and therefore a deterministic
 * "next page"), the sort keys must include a unique tie-breaker (typically the
 * primary key) as the last key. The cursor preserves insertion order of its
 * keys via a {@link LinkedHashMap} copy.
 * </p>
 *
 * @author SERPRO
 */
public final class Cursor {

    /** Traversal direction relative to the boundary row. */
    public enum Direction {
        /** Rows strictly after the boundary in sort order. */
        AFTER,
        /** Rows strictly before the boundary in sort order. */
        BEFORE
    }

    private final Map<String, String> keys;
    private final Direction direction;
    private final long expiresAtEpochSeconds; // 0 == no expiry

    /**
     * @param keys                  ordered map of sort field name → boundary
     *                              value (as string); the last entry should be a
     *                              unique tie-breaker
     * @param direction             traversal direction
     * @param expiresAtEpochSeconds absolute expiry in epoch seconds; {@code 0}
     *                              means "never expires"
     */
    public Cursor(Map<String, String> keys, Direction direction, long expiresAtEpochSeconds) {
        Objects.requireNonNull(keys, "keys");
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("cursor must have at least one key");
        }
        this.keys = new LinkedHashMap<>(keys);
        this.direction = Objects.requireNonNull(direction, "direction");
        this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    }

    /**
     * @return an immutable, order-preserving view of the keyset values
     */
    public Map<String, String> keys() {
        return Map.copyOf(keys);
    }

    /**
     * @return the ordered keyset values while preserving insertion order
     */
    public Map<String, String> orderedKeys() {
        return new LinkedHashMap<>(keys);
    }

    public Direction direction() {
        return direction;
    }

    public long expiresAtEpochSeconds() {
        return expiresAtEpochSeconds;
    }

    /**
     * @param nowEpochSeconds current time in epoch seconds
     * @return {@code true} if this cursor has an expiry and it is in the past
     */
    public boolean isExpired(long nowEpochSeconds) {
        return expiresAtEpochSeconds > 0 && nowEpochSeconds >= expiresAtEpochSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cursor other)) return false;
        return expiresAtEpochSeconds == other.expiresAtEpochSeconds
                && direction == other.direction
                && keys.equals(other.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys, direction, expiresAtEpochSeconds);
    }

    @Override
    public String toString() {
        return "Cursor{keys=" + keys + ", direction=" + direction
                + ", expiresAt=" + expiresAtEpochSeconds + '}';
    }
}
