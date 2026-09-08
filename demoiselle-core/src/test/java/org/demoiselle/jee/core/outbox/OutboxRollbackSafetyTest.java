/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import jakarta.enterprise.event.Event;

import org.demoiselle.jee.core.api.outbox.OutboxMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OutboxRollbackSafetyTest {

    @Test
    @SuppressWarnings("unchecked")
    void nonTransactionalStoreRemainsEmptyUntilAfterSuccessObserverRuns() throws Exception {
        LocalOutboxStore store = new LocalOutboxStore();
        Event<OutboxService.OutboxCommitted> event = mock(Event.class);
        OutboxService service = new OutboxService();
        inject(service, "store", store);
        inject(service, "publisher", new NoOpOutboxPublisher());
        inject(service, "committedEvent", event);

        service.stage("order-rollback", "OrderCreated", "{}", "rollback-key");

        assertEquals(0, store.size(),
                "a non-transactional store must remain empty before AFTER_SUCCESS");
        ArgumentCaptor<OutboxService.OutboxCommitted> committed =
                ArgumentCaptor.forClass(OutboxService.OutboxCommitted.class);
        verify(event).fire(committed.capture());

        service.onCommit(committed.getValue());
        OutboxMessage persisted = store.findByDedupeKey("rollback-key").orElseThrow();
        assertTrue(persisted.status() == OutboxMessage.Status.PUBLISHED);
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field declared = target.getClass().getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(target, value);
    }
}
