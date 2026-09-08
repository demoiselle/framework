/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.outbox;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Service Provider Interface for Transactional Outbox persistence.
 *
 * <p>
 * Implementations decide where staged messages live (in-memory, a database
 * table participating in the business transaction, etc.). The framework ships a
 * local in-memory implementation that requires no external database. Duplicate
 * suppression is expressed through {@link #append(OutboxMessage)} returning
 * whether the message was actually stored.
 * </p>
 *
 * @author SERPRO
 */
public interface OutboxStore {

    /**
     * Appends a message to the outbox unless a message with the same
     * {@link OutboxMessage#dedupeKey()} already exists.
     *
     * @param message the message to stage
     * @return {@code true} if stored, {@code false} if suppressed as a duplicate
     */
    boolean append(OutboxMessage message);

    /**
     * Returns retryable messages (status {@code PENDING} or {@code FAILED}),
     * oldest first, up to {@code limit}.
     *
     * @param limit maximum number of messages to return
     * @return the retryable messages
     */
    List<OutboxMessage> pending(int limit);

    /**
     * Marks a message as published.
     *
     * @param id the message id
     */
    void markPublished(String id);

    /**
     * Marks a message as failed (eligible for retry).
     *
     * @param id the message id
     */
    void markFailed(String id);

    /**
     * Looks up a message by id.
     *
     * @param id the message id
     * @return the message, if present
     */
    Optional<OutboxMessage> find(String id);

    /**
     * Looks up a message by its deduplication key.
     *
     * @param dedupeKey deduplication key
     * @return the existing message, if present
     */
    default Optional<OutboxMessage> findByDedupeKey(String dedupeKey) {
        return Optional.empty();
    }

    /**
     * Indicates whether {@link #append(OutboxMessage)} participates in the
     * caller's transaction. Transactional database adapters should override
     * this method and return {@code true}. Non-transactional stores are appended
     * only by the framework's AFTER_SUCCESS observer, preventing publication or
     * retention after rollback.
     *
     * @return {@code true} when append is transactionally enlisted
     */
    default boolean participatesInTransaction() {
        return false;
    }

    /**
     * Removes published messages older than {@code retention}, returning the
     * number purged. This implements time-based retention so the outbox does not
     * grow unbounded.
     *
     * @param retention maximum age of a published message to keep
     * @return the number of purged messages
     */
    int purgePublishedOlderThan(Duration retention);
}
