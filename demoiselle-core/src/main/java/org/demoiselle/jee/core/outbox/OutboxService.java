/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.outbox;

import java.time.Clock;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.outbox.OutboxMessage;
import org.demoiselle.jee.core.api.outbox.OutboxPublisher;
import org.demoiselle.jee.core.api.outbox.OutboxStore;

/**
 * Coordinates the Transactional Outbox lifecycle.
 *
 * <p>
 * Callers {@link #stage(String, String, String, String) stage} a message as part
 * of their business work. Staging both persists the message via the
 * {@link OutboxStore} (participating in the surrounding transaction when the
 * store is transactional) and fires a CDI event. An observer of that event runs
 * with {@link TransactionPhase#AFTER_SUCCESS}, guaranteeing that the message is
 * only handed to the {@link OutboxPublisher} <strong>after the transaction
 * commits</strong>. If the transaction rolls back, the AFTER_SUCCESS observer is
 * never invoked, so nothing is published.
 * </p>
 *
 * <p>
 * Duplicate staging (same dedupe key) is suppressed by the store. Publication
 * failures mark the message {@code FAILED} for later retry via {@link #flush(int)},
 * which can be driven by a scheduler. Neither a database nor a broker is
 * required: the default store is in-memory and, absent a real
 * {@link OutboxPublisher} bean, a {@link NoOpOutboxPublisher} is used.
 * </p>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class OutboxService {

    /** CDI event wrapper marking a staged message ready for AFTER_SUCCESS delivery. */
    public record OutboxCommitted(OutboxMessage message, boolean alreadyStored) {
    }

    @Inject
    private OutboxStore store;

    @Inject
    private OutboxPublisher publisher;

    @Inject
    private Event<OutboxCommitted> committedEvent;

    private Clock clock = Clock.systemUTC();

    /** Package-visible setter for deterministic tests. */
    void setClock(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    /**
     * Stages a message. Returns the stored message, or the pre-existing one when
     * suppressed as a duplicate.
     *
     * @param aggregateId aggregate identifier
     * @param type        event type
     * @param payload     opaque serialized payload
     * @param dedupeKey   dedupe key (may be {@code null} to default to the id)
     * @return the staged (or duplicate) message
     */
    public OutboxMessage stage(String aggregateId, String type, String payload, String dedupeKey) {
        OutboxMessage message = OutboxMessage.pending(aggregateId, type, payload, dedupeKey, clock.instant());
        boolean alreadyStored = false;

        if (store.participatesInTransaction()) {
            if (!store.append(message)) {
                return store.findByDedupeKey(message.dedupeKey()).orElse(message);
            }
            alreadyStored = true;
        }

        if (committedEvent == null) {
            throw new IllegalStateException("Transactional Outbox requires a CDI Event dispatcher");
        }
        committedEvent.fire(new OutboxCommitted(message, alreadyStored));
        return store.findByDedupeKey(message.dedupeKey()).orElse(message);
    }

    /**
     * Publishes a single staged message after the transaction has committed.
     * Non-transactional stores are appended here, so a rollback leaves no
     * pending message that a later relay could publish. Transactional stores
     * append during {@link #stage(String, String, String, String)} and rely on
     * their transaction manager to roll the record back.
     *
     * @param committed the committed marker event
     */
    void onCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) OutboxCommitted committed) {
        OutboxMessage message = committed.message();
        if (!committed.alreadyStored() && !store.append(message)) {
            return;
        }
        publish(message.id());
    }

    /**
     * Attempts to publish all currently pending messages (e.g. retries). Safe to
     * call from a scheduler.
     *
     * @param batchSize maximum messages to process
     * @return the number successfully published in this pass
     */
    public int flush(int batchSize) {
        List<OutboxMessage> pending = store.pending(batchSize);
        int published = 0;
        for (OutboxMessage message : pending) {
            if (publish(message.id())) {
                published++;
            }
        }
        return published;
    }

    private boolean publish(String messageId) {
        OutboxMessage message = store.find(messageId).orElse(null);
        if (message == null || message.status() == OutboxMessage.Status.PUBLISHED) {
            return false;
        }
        try {
            publisher.publish(message);
            store.markPublished(messageId);
            return true;
        } catch (Exception e) {
            store.markFailed(messageId);
            return false;
        }
    }
}
