/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.outbox;

/**
 * Service Provider Interface for delivering an {@link OutboxMessage} to a broker.
 *
 * <p>
 * This is the only broker-specific extension point. Applications supply a CDI
 * bean implementing this interface to bridge the outbox to Kafka, RabbitMQ, an
 * HTTP webhook, etc. The framework does not depend on any messaging API. If no
 * implementation is provided, a no-op publisher is used so the outbox lifecycle
 * still functions (messages are staged and marked published) without an external
 * broker.
 * </p>
 *
 * @author SERPRO
 */
@FunctionalInterface
public interface OutboxPublisher {

    /**
     * Publishes the given message. Throwing an exception marks the message as
     * failed and leaves it eligible for retry.
     *
     * @param message the message to publish
     * @throws Exception if delivery fails
     */
    void publish(OutboxMessage message) throws Exception;
}
