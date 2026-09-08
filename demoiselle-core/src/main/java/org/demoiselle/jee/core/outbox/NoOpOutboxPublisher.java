/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.outbox;

import jakarta.enterprise.context.ApplicationScoped;

import org.demoiselle.jee.core.api.outbox.OutboxMessage;
import org.demoiselle.jee.core.api.outbox.OutboxPublisher;

/**
 * Default {@link OutboxPublisher} that discards messages.
 *
 * <p>
 * It lets the outbox lifecycle run end-to-end (stage → AFTER_SUCCESS → mark
 * published) without any external broker. Applications override it by providing
 * their own {@link OutboxPublisher} CDI bean, which — being a non-default bean —
 * takes precedence over this one.
 * </p>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class NoOpOutboxPublisher implements OutboxPublisher {

    @Override
    public void publish(OutboxMessage message) {
        // Intentionally does nothing.
    }
}
