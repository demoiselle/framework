package org.demoiselle.jee.observability.metrics;

import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;

/**
 * Implementação real que delega para MicroProfile Metrics.
 * <p>
 * Registrada programaticamente pela {@code ObservabilityExtension} via
 * {@code AfterBeanDiscovery.addBean()}. Marcada como {@code @Vetoed} para
 * impedir descoberta automática pelo Weld com {@code bean-discovery-mode="all"},
 * evitando ambiguidade (WELD-001409) com o bean sintético.
 */
@Vetoed
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
