package org.demoiselle.jee.observability.metrics;

/**
 * Implementação noop usada quando MicroProfile Metrics não está no classpath.
 * <p>
 * Registrada programaticamente pela {@code ObservabilityExtension} via
 * {@code AfterBeanDiscovery.addBean()}. Não possui anotação de escopo CDI
 * para evitar ambiguidade (WELD-001409) com o bean sintético.
 */
public class NoopMetricsAdapter implements MetricsAdapter {
    @Override
    public void increment(String counterName) { /* noop */ }

    @Override
    public long getCount(String counterName) { return 0L; }
}
