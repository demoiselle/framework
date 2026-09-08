/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import org.demoiselle.jee.core.api.outbox.OutboxMessage;
import org.demoiselle.jee.core.api.outbox.OutboxStore;

/**
 * Default, in-memory {@link OutboxStore} requiring no external database.
 *
 * <p>
 * Messages are keyed by id in a {@link ConcurrentHashMap}; a second map tracks
 * dedupe keys so {@link #append(OutboxMessage)} can atomically reject
 * duplicates. Retention is time-based via
 * {@link #purgePublishedOlderThan(Duration)}.
 * </p>
 *
 * <p>
 * Marked as an {@link Alternative} with low priority so applications can supply
 * their own store without ambiguity.
 * </p>
 *
 * @author SERPRO
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class LocalOutboxStore implements OutboxStore {

    private final ConcurrentHashMap<String, OutboxMessage> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> dedupeToId = new ConcurrentHashMap<>();
    private final Clock clock;

    /** Default constructor using the system UTC clock. */
    public LocalOutboxStore() {
        this(Clock.systemUTC());
    }

    /**
     * Constructor with a custom clock for deterministic retention tests.
     *
     * @param clock the clock
     */
    public LocalOutboxStore(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Override
    public boolean append(OutboxMessage message) {
        // Reserve the dedupe key atomically; only the first writer proceeds.
        String previous = dedupeToId.putIfAbsent(message.dedupeKey(), message.id());
        if (previous != null) {
            return false;
        }
        byId.put(message.id(), message);
        return true;
    }

    @Override
    public List<OutboxMessage> pending(int limit) {
        List<OutboxMessage> pending = new ArrayList<>();
        for (OutboxMessage m : byId.values()) {
            // PENDING and FAILED are both retryable (not yet successfully published).
            if (m.status() == OutboxMessage.Status.PENDING
                    || m.status() == OutboxMessage.Status.FAILED) {
                pending.add(m);
            }
        }
        pending.sort(Comparator.comparing(OutboxMessage::createdAt).thenComparing(OutboxMessage::id));
        if (limit >= 0 && pending.size() > limit) {
            return List.copyOf(pending.subList(0, limit));
        }
        return List.copyOf(pending);
    }

    @Override
    public void markPublished(String id) {
        byId.computeIfPresent(id, (k, m) -> m.published(clock.instant()));
    }

    @Override
    public void markFailed(String id) {
        byId.computeIfPresent(id, (k, m) -> m.failed());
    }

    @Override
    public Optional<OutboxMessage> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }


    @Override
    public Optional<OutboxMessage> findByDedupeKey(String dedupeKey) {
        String id = dedupeToId.get(dedupeKey);
        return id == null ? Optional.empty() : find(id);
    }
    @Override
    public int purgePublishedOlderThan(Duration retention) {
        Instant threshold = clock.instant().minus(retention);
        int[] purged = { 0 };
        byId.values().removeIf(m -> {
            boolean expired = m.status() == OutboxMessage.Status.PUBLISHED
                    && m.publishedAt() != null
                    && m.publishedAt().isBefore(threshold);
            if (expired) {
                dedupeToId.remove(m.dedupeKey(), m.id());
                purged[0]++;
            }
            return expired;
        });
        return purged[0];
    }

    /**
     * @return total number of messages currently held (test/diagnostic helper)
     */
    public int size() {
        return byId.size();
    }
}
