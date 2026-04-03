package org.demoiselle.jee.observability.metrics;

import jakarta.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;

/**
 * Implementação real que delega para MicroProfile Metrics.
 * <p>
 * Registrada programaticamente pela {@code ObservabilityExtension} via
 * {@code AfterBeanDiscovery.addBean()}. Não possui anotação de escopo CDI
 * para evitar ambiguidade (WELD-001409) com o bean sintético.
 */
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
