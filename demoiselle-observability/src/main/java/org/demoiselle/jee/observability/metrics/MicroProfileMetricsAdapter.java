package org.demoiselle.jee.observability.metrics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;

/**
 * Implementação real que delega para MicroProfile Metrics.
 * Registrada condicionalmente pela ObservabilityExtension.
 */
@ApplicationScoped
public class MicroProfileMetricsAdapter implements MetricsAdapter {

    @Inject
    private MetricRegistry registry;

    @Override
    public void increment(String counterName) {
        Counter counter = registry.counter(counterName);
        counter.inc();
    }

    @Override
    public long getCount(String counterName) {
        Counter counter = registry.counter(counterName);
        return counter.getCount();
    }
}
