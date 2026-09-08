/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.idempotency;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of a stored idempotent operation.
 *
 * <p>
 * A record is created in the {@link Status#IN_PROGRESS} state when a request is
 * first seen and transitions to {@link Status#COMPLETED} once the response is
 * known. Completed records carry the response so it can be replayed for
 * subsequent requests with the same key.
 * </p>
 *
 * @param fingerprint the logical fingerprint of the operation
 *                    (principal + method + path + payload hash)
 * @param status      the lifecycle status
 * @param httpStatus  the captured HTTP status code (only meaningful when
 *                    {@link Status#COMPLETED})
 * @param body        the captured response body (may be {@code null})
 * @param headers     the captured response headers (never {@code null})
 * @param createdAt   creation instant (used for TTL expiry)
 * @param expiresAt   expiry instant; a record is considered expired once
 *                    {@link Instant#now()} is after this value
 * @author SERPRO
 */
public record IdempotencyRecord(
        String fingerprint,
        Status status,
        int httpStatus,
        byte[] body,
        Map<String, List<String>> headers,
        Instant createdAt,
        Instant expiresAt) {

    /** Lifecycle status of an idempotency record. */
    public enum Status {
        /** A request is currently being processed for this fingerprint. */
        IN_PROGRESS,
        /** The response for this fingerprint has been captured and can be replayed. */
        COMPLETED
    }

    /**
     * Compact constructor: normalises {@code headers} to an immutable map and
     * defensively copies the body.
     */
    public IdempotencyRecord {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body == null ? null : body.clone();
    }

    /**
     * Returns a defensive copy of the body, or {@code null} when there is none.
     *
     * @return the body bytes or {@code null}
     */
    @Override
    public byte[] body() {
        return body == null ? null : body.clone();
    }

    /**
     * Creates an {@code IN_PROGRESS} record.
     *
     * @param fingerprint the operation fingerprint
     * @param ttlSeconds  time-to-live in seconds
     * @param now         the current instant
     * @return a new in-progress record
     */
    public static IdempotencyRecord inProgress(String fingerprint, long ttlSeconds, Instant now) {
        return new IdempotencyRecord(fingerprint, Status.IN_PROGRESS, 0, null, Map.of(),
                now, now.plusSeconds(ttlSeconds));
    }

    /**
     * Returns a completed copy of this record carrying the given response,
     * preserving the original creation and expiry instants.
     *
     * @param httpStatus the response status code
     * @param body       the response body (may be {@code null})
     * @param headers    the response headers
     * @return a completed record
     */
    public IdempotencyRecord completeWith(int httpStatus, byte[] body, Map<String, List<String>> headers) {
        return new IdempotencyRecord(fingerprint, Status.COMPLETED, httpStatus, body, headers,
                createdAt, expiresAt);
    }

    /**
     * @param now the current instant
     * @return {@code true} if this record has expired
     */
    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
