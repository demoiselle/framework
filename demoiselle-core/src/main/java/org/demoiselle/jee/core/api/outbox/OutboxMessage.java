/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.outbox;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable message staged in a Transactional Outbox.
 *
 * <p>
 * The outbox pattern decouples "changing state" from "publishing an event": a
 * message is recorded as part of the business work and only handed to a broker
 * <em>after</em> the surrounding transaction succeeds. This record is the unit of
 * that staging and is intentionally broker-agnostic — {@code payload} is an
 * opaque string (e.g. JSON) and there is no dependency on any messaging API.
 * </p>
 *
 * @param id          globally unique message id
 * @param aggregateId identifier of the aggregate/entity the event relates to
 * @param type        logical event type (topic/routing hint)
 * @param payload     opaque serialized payload
 * @param dedupeKey   key used to suppress duplicate publications; defaults to
 *                    {@code id} when not supplied
 * @param status      lifecycle status
 * @param createdAt   creation instant (used for retention)
 * @param publishedAt publication instant, or {@code null} while pending
 * @author SERPRO
 */
public record OutboxMessage(
        String id,
        String aggregateId,
        String type,
        String payload,
        String dedupeKey,
        Status status,
        Instant createdAt,
        Instant publishedAt) {

    /** Lifecycle status of an outbox message. */
    public enum Status {
        /** Staged, awaiting a successful transaction and publication. */
        PENDING,
        /** Successfully handed to the broker. */
        PUBLISHED,
        /** Publication failed and the message may be retried. */
        FAILED
    }

    /** Compact constructor: validates required fields and defaults the dedupe key. */
    public OutboxMessage {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        if (dedupeKey == null || dedupeKey.isBlank()) {
            dedupeKey = id;
        }
    }

    /**
     * Creates a new {@link Status#PENDING} message with a generated id.
     *
     * @param aggregateId aggregate identifier
     * @param type        event type
     * @param payload     opaque payload
     * @param dedupeKey   dedupe key (may be {@code null})
     * @param now         creation instant
     * @return a new pending message
     */
    public static OutboxMessage pending(String aggregateId, String type, String payload,
            String dedupeKey, Instant now) {
        return new OutboxMessage(UUID.randomUUID().toString(), aggregateId, type, payload,
                dedupeKey, Status.PENDING, now, null);
    }

    /**
     * @param now publication instant
     * @return a copy marked {@link Status#PUBLISHED}
     */
    public OutboxMessage published(Instant now) {
        return new OutboxMessage(id, aggregateId, type, payload, dedupeKey, Status.PUBLISHED, createdAt, now);
    }

    /**
     * @return a copy marked {@link Status#FAILED}
     */
    public OutboxMessage failed() {
        return new OutboxMessage(id, aggregateId, type, payload, dedupeKey, Status.FAILED, createdAt, publishedAt);
    }
}
