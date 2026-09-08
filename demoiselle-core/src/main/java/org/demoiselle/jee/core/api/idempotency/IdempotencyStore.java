/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.idempotency;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service Provider Interface for idempotency persistence.
 *
 * <p>
 * Implementations decide <em>where</em> idempotency records live — an in-memory
 * map, a database table, a distributed cache, etc. The framework ships a local,
 * atomic, TTL-based in-memory implementation
 * ({@code org.demoiselle.jee.core.idempotency.LocalIdempotencyStore}); replace it
 * by providing an alternative CDI bean.
 * </p>
 *
 * <p>
 * The contract is deliberately small and expressed in terms of atomic
 * operations so that a single request can safely claim a fingerprint without a
 * check-then-act race.
 * </p>
 *
 * @author SERPRO
 */
public interface IdempotencyStore {

    /**
     * Atomically registers an {@link IdempotencyRecord.Status#IN_PROGRESS} record
     * for the given fingerprint if — and only if — no live record already exists.
     *
     * <ul>
     *   <li>If there was no record (or it had expired), a new in-progress record
     *       is stored and an <strong>empty</strong> {@link Optional} is returned,
     *       signalling the caller <em>won</em> the race and should process the
     *       request.</li>
     *   <li>If a live record already exists (in-progress or completed), it is
     *       returned so the caller can reply with a conflict or a replay.</li>
     * </ul>
     *
     * @param fingerprint the operation fingerprint
     * @param ttlSeconds  time-to-live for a freshly created record
     * @return empty if the caller acquired the slot; otherwise the existing
     *         live record
     */
    Optional<IdempotencyRecord> begin(String fingerprint, long ttlSeconds);

    /**
     * Marks the fingerprint's record as completed, capturing the response so it
     * can be replayed. No-op if the record is absent or expired.
     *
     * @param fingerprint the operation fingerprint
     * @param httpStatus  the response status code
     * @param body        the response body (may be {@code null})
     * @param headers     the response headers
     */
    void complete(String fingerprint, int httpStatus, byte[] body, Map<String, List<String>> headers);

    /**
     * Removes an in-progress record, e.g. when processing failed and the client
     * should be allowed to retry. No-op if absent.
     *
     * @param fingerprint the operation fingerprint
     */
    void abort(String fingerprint);

    /**
     * Returns the live (non-expired) record for the fingerprint, if any.
     *
     * @param fingerprint the operation fingerprint
     * @return the record, or empty if absent/expired
     */
    Optional<IdempotencyRecord> find(String fingerprint);
}
