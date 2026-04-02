package org.demoiselle.jee.observability.metrics;

/**
 * Abstração que encapsula o acesso ao MicroProfile Metrics.
 * Quando a API não está no classpath, a implementação NoopMetricsAdapter é usada.
 */
public interface MetricsAdapter {
    void increment(String counterName);
    long getCount(String counterName);
}
