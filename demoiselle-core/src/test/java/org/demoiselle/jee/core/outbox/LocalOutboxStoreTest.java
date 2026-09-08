/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.demoiselle.jee.core.api.outbox.OutboxMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LocalOutboxStore}: dedupe, pending ordering, retention.
 *
 * <p>Validates Requirement (4): broker-independent store; dedupe; retention.</p>
 */
class LocalOutboxStoreTest {

    private static final class MutableClock extends Clock {
        private Instant now;
        MutableClock(Instant start) { this.now = start; }
        void advance(Duration d) { now = now.plus(d); }
        @Override public Instant instant() { return now; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    @Test
    @DisplayName("append suppresses duplicate dedupe keys")
    void appendDedupes() {
        LocalOutboxStore store = new LocalOutboxStore();
        OutboxMessage a = OutboxMessage.pending("agg", "Created", "{}", "dk-1", Instant.now());
        OutboxMessage b = OutboxMessage.pending("agg", "Created", "{}", "dk-1", Instant.now());

        assertTrue(store.append(a), "first append stores");
        assertFalse(store.append(b), "duplicate dedupe key is suppressed");
        assertEquals(1, store.size());
    }

    @Test
    @DisplayName("pending returns oldest-first up to the limit")
    void pendingOrderedByAge() {
        MutableClock clock = new MutableClock(Instant.parse("2025-01-01T00:00:00Z"));
        LocalOutboxStore store = new LocalOutboxStore(clock);

        OutboxMessage first = OutboxMessage.pending("a", "T", "1", "k1", clock.instant());
        clock.advance(Duration.ofSeconds(1));
        OutboxMessage second = OutboxMessage.pending("a", "T", "2", "k2", clock.instant());
        clock.advance(Duration.ofSeconds(1));
        OutboxMessage third = OutboxMessage.pending("a", "T", "3", "k3", clock.instant());

        store.append(third);
        store.append(first);
        store.append(second);

        List<OutboxMessage> pending = store.pending(2);
        assertEquals(2, pending.size());
        assertEquals(first.id(), pending.get(0).id());
        assertEquals(second.id(), pending.get(1).id());
    }

    @Test
    @DisplayName("retention purges only published messages older than the window")
    void retentionPurge() {
        MutableClock clock = new MutableClock(Instant.parse("2025-01-01T00:00:00Z"));
        LocalOutboxStore store = new LocalOutboxStore(clock);

        OutboxMessage m = OutboxMessage.pending("a", "T", "1", "k1", clock.instant());
        store.append(m);
        store.markPublished(m.id());
        OutboxMessage pendingMsg = OutboxMessage.pending("a", "T", "2", "k2", clock.instant());
        store.append(pendingMsg);

        clock.advance(Duration.ofHours(2));

        int purged = store.purgePublishedOlderThan(Duration.ofHours(1));
        assertEquals(1, purged, "only the published message is purged");
        assertTrue(store.find(m.id()).isEmpty());
        assertTrue(store.find(pendingMsg.id()).isPresent(), "pending message is retained");
    }

    @Test
    @DisplayName("purge frees the dedupe key for re-use")
    void purgeReleasesDedupeKey() {
        MutableClock clock = new MutableClock(Instant.parse("2025-01-01T00:00:00Z"));
        LocalOutboxStore store = new LocalOutboxStore(clock);

        OutboxMessage m = OutboxMessage.pending("a", "T", "1", "same-key", clock.instant());
        store.append(m);
        store.markPublished(m.id());
        clock.advance(Duration.ofHours(2));
        store.purgePublishedOlderThan(Duration.ofHours(1));

        OutboxMessage again = OutboxMessage.pending("a", "T", "1", "same-key", clock.instant());
        assertTrue(store.append(again), "dedupe key should be reusable after purge");
    }
}
