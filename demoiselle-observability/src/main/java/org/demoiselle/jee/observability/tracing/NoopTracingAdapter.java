package org.demoiselle.jee.observability.tracing;

import jakarta.enterprise.inject.Vetoed;

/**
 * Implementação noop usada quando OpenTelemetry não está no classpath.
 * Executa o callable diretamente sem criar spans.
 * <p>
 * Registrada programaticamente pela {@code ObservabilityExtension} via
 * {@code AfterBeanDiscovery.addBean()}. Marcada como {@code @Vetoed} para
 * impedir descoberta automática pelo Weld com {@code bean-discovery-mode="all"},
 * evitando ambiguidade (WELD-001409) com o bean sintético.
 */
@Vetoed
public class NoopTracingAdapter implements TracingAdapter {

    @Override
    public <T> T executeInSpan(String module, String operation, SpanCallable<T> callable) throws Exception {
        return callable.call();
    }
}
