package org.demoiselle.jee.observability.metrics;

import jakarta.enterprise.inject.Vetoed;

/**
 * Implementação noop usada quando MicroProfile Metrics não está no classpath.
 * <p>
 * Registrada programaticamente pela {@code ObservabilityExtension} via
 * {@code AfterBeanDiscovery.addBean()}. Marcada como {@code @Vetoed} para
 * impedir descoberta automática pelo Weld com {@code bean-discovery-mode="all"},
 * evitando ambiguidade (WELD-001409) com o bean sintético.
 */
@Vetoed
public class NoopMetricsAdapter implements MetricsAdapter {
    @Override
    public void increment(String counterName) { /* noop */ }

    @Override
    public long getCount(String counterName) { return 0L; }
}
