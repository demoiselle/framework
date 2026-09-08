/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.outbox.OutboxMessage;
import org.demoiselle.jee.core.api.outbox.OutboxPublisher;
import org.demoiselle.jee.core.api.outbox.OutboxStore;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Lifecycle tests for {@link OutboxService} using a real CDI container.
 *
 * <p>
 * Verifies that staging a message causes the recording publisher to receive it
 * via the AFTER_SUCCESS observer, and that duplicate staging is suppressed so
 * the publisher is only invoked once. Weld delivers a transactional-phase
 * observer synchronously when no JTA transaction is active, which models a
 * successful commit.
 * </p>
 *
 * <p>Validates Requirement (4): publish only AFTER_SUCCESS; dedupe; no external
 * broker/database.</p>
 */
@EnableAutoWeld
@AddBeanClasses({
        OutboxService.class,
        LocalOutboxStore.class,
        NoOpOutboxPublisher.class,
        OutboxServiceLifecycleTest.RecordingPublisher.class
})
class OutboxServiceLifecycleTest {

    /**
     * Records published messages. {@code @Specializes} the no-op publisher so it
     * wins bean resolution over {@link NoOpOutboxPublisher}.
     */
    @Specializes
    @ApplicationScoped
    static class RecordingPublisher extends NoOpOutboxPublisher {
        private final List<OutboxMessage> published = new CopyOnWriteArrayList<>();

        @Override
        public void publish(OutboxMessage message) {
            published.add(message);
        }

        List<OutboxMessage> recorded() {
            return published;
        }
    }

    @Inject
    OutboxService service;

    @Inject
    RecordingPublisher publisher;

    @Inject
    OutboxStore store;

    @Test
    @DisplayName("Staging publishes exactly once after commit")
    void stagePublishesAfterCommit() {
        OutboxMessage m = service.stage("order-1", "OrderCreated", "{\"id\":1}", "order-1-created");

        assertEquals(1, publisher.recorded().size(), "message should be published after commit");
        assertEquals(m.id(), publisher.recorded().get(0).id());
        assertEquals(OutboxMessage.Status.PUBLISHED, store.find(m.id()).orElseThrow().status());
    }

    @Test
    @DisplayName("Duplicate dedupe key is staged and published only once")
    void duplicateSuppressed() {
        OutboxMessage first = service.stage("order-2", "OrderCreated", "{}", "dup-key");
        OutboxMessage duplicate = service.stage("order-2", "OrderCreated", "{}", "dup-key");

        assertEquals(first.id(), duplicate.id(), "duplicate must resolve to the existing message");
        long created = publisher.recorded().stream()
                .filter(m -> "dup-key".equals(m.dedupeKey()))
                .count();
        assertEquals(1, created, "duplicate must be suppressed");
    }

    @Test
    @DisplayName("flush retries pending messages")
    void flushRetries() {
        // Stage then simulate a failure by marking failed, then flush.
        OutboxMessage m = service.stage("order-3", "OrderCreated", "{}", "retry-key");
        store.markFailed(m.id()); // pretend the first delivery failed
        assertTrue(store.find(m.id()).isPresent());

        int publishedNow = service.flush(10);
        assertEquals(1, publishedNow, "flush should re-publish the failed message");
        assertEquals(OutboxMessage.Status.PUBLISHED, store.find(m.id()).orElseThrow().status());
    }
}
