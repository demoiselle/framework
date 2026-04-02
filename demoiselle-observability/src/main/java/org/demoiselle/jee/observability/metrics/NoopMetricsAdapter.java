package org.demoiselle.jee.observability.metrics;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementação noop usada quando MicroProfile Metrics não está no classpath.
 */
@ApplicationScoped
public class NoopMetricsAdapter implements MetricsAdapter {
    @Override
    public void increment(String counterName) { /* noop */ }

    @Override
    public long getCount(String counterName) { return 0L; }
}
